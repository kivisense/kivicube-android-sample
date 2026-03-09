package com.kivicube.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class WebViewActivity : ComponentActivity() {
    companion object {
        const val EXTRA_URL = "extra_url"
    }

    private lateinit var webView: WebView
    private var pendingRequest: PermissionRequest? = null
    private var hasRequestedPermission = false
    private var currentPermissionDescription: String = "Permission"
    private var currentPermission: String = Manifest.permission.CAMERA

    private val devicePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        handlePermissionResult(isGranted)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val url = intent.getStringExtra(EXTRA_URL) ?: run {
            finish()
            return
        }

        // Create WebView directly instead of using Compose
        webView = WebView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )

            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest?) {
                    val resources = request?.resources ?: return

                    val permissionsToRequest = mutableListOf<String>()
                    var permissionDescription = ""

                    for (resource in resources) {
                        when (resource) {
                            PermissionRequest.RESOURCE_VIDEO_CAPTURE -> {
                                permissionsToRequest.add(Manifest.permission.CAMERA)
                                permissionDescription += "Camera, "
                            }
                            PermissionRequest.RESOURCE_AUDIO_CAPTURE -> {
                                permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
                                permissionDescription += "Microphone, "
                            }
                            else -> {
                                // For other permissions, also add to request list
                                permissionsToRequest.add(Manifest.permission.CAMERA) // Default to camera permission as placeholder
                                permissionDescription += "Other permissions, "
                            }
                        }
                    }

                    if (permissionsToRequest.isNotEmpty()) {
                        // Remove last comma
                        permissionDescription = permissionDescription.trimEnd(' ', ',')
                        handlePermissionRequest(request, permissionsToRequest.first(), permissionDescription)
                    } else {
                        request.deny()
                    }
                }
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    return false
                }
            }

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true

                // Enable video autoplay
                mediaPlaybackRequiresUserGesture = false

                // Set User-Agent with Kivicube version identifier (dynamically get version from app)
                val versionName = try {
                    packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0"
                } catch (e: Exception) {
                    "1.0"
                }
                userAgentString = "$userAgentString Kivicube/$versionName"
            }
        }

        setContentView(webView)

        // Delay loading URL to ensure WebView is fully initialized
        webView.post {
            webView.loadUrl(url)
        }
    }

    private fun handlePermissionRequest(request: PermissionRequest, permission: String, permissionDescription: String) {
        pendingRequest = request

        // Store permission description for later use
        this.currentPermissionDescription = permissionDescription
        this.currentPermission = permission
        this.hasRequestedPermission = false

        // Check permission status
        when (ContextCompat.checkSelfPermission(this, permission)) {
            PackageManager.PERMISSION_GRANTED -> {
                request.grant(request.resources)
                pendingRequest = null
            }
            else -> {
                // First permission request
                devicePermissionLauncher.launch(permission)
            }
        }
    }

    private fun handlePermissionResult(isGranted: Boolean) {
        if (isGranted) {
            pendingRequest?.grant(pendingRequest?.resources)
        } else {
            pendingRequest?.deny()
            showPermissionDialog()
        }
        pendingRequest = null
    }

    override fun onResume() {
        super.onResume()
        // Check permission status when user returns from settings
        checkPendingPermission()
    }

    private fun checkPendingPermission() {
        // If there's a pending permission request, check if currently granted
        pendingRequest?.let { request ->
            when (ContextCompat.checkSelfPermission(this, currentPermission)) {
                PackageManager.PERMISSION_GRANTED -> {
                    // Permission granted, authorize to webpage
                    request.grant(request.resources)
                    pendingRequest = null
                    hasRequestedPermission = false
                }
                else -> {
                    // Permission still not granted, show dialog
                    if (hasRequestedPermission) {
                        showPermissionDialog()
                    }
                }
            }
        }
    }

    private fun showPermissionDialog(permissionDescription: String = this.currentPermissionDescription) {
        this.currentPermissionDescription = permissionDescription

        android.app.AlertDialog.Builder(this)
            .setTitle("$permissionDescription Required")
            .setMessage("The webpage needs access to $permissionDescription to function properly. Please enable the permission in settings.")
            .setPositiveButton("Go to Settings") { _: android.content.DialogInterface, _: Int ->
                openAppSettings()
            }
            .setNegativeButton("Cancel") { _: android.content.DialogInterface, _: Int ->
                pendingRequest?.deny()
                pendingRequest = null
            }
            .setCancelable(false)
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }
}
