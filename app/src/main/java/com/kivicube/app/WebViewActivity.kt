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
    private var currentPermissionDescription: String = "权限"
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
                                permissionDescription += "相机、"
                            }
                            PermissionRequest.RESOURCE_AUDIO_CAPTURE -> {
                                permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
                                permissionDescription += "麦克风、"
                            }
                            else -> {
                                // 对于其他权限，也添加到请求列表中
                                permissionsToRequest.add(Manifest.permission.CAMERA) // 默认使用相机权限作为占位
                                permissionDescription += "其他权限、"
                            }
                        }
                    }

                    if (permissionsToRequest.isNotEmpty()) {
                        // 移除最后一个顿号
                        permissionDescription = permissionDescription.trimEnd('、')
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

                // 设置 User-Agent，增加Kivicube版本标识（从应用动态获取版本号）
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

        // 存储权限描述以便后续使用
        this.currentPermissionDescription = permissionDescription
        this.currentPermission = permission
        this.hasRequestedPermission = false

        // 检查权限状态
        when (ContextCompat.checkSelfPermission(this, permission)) {
            PackageManager.PERMISSION_GRANTED -> {
                request.grant(request.resources)
                pendingRequest = null
            }
            else -> {
                // 首次请求权限
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
        // 当用户从设置界面返回时，检查权限状态
        checkPendingPermission()
    }

    private fun checkPendingPermission() {
        // 如果有待处理的权限请求，检查当前是否已授予权限
        pendingRequest?.let { request ->
            when (ContextCompat.checkSelfPermission(this, currentPermission)) {
                PackageManager.PERMISSION_GRANTED -> {
                    // 权限已授予，直接授权给网页
                    request.grant(request.resources)
                    pendingRequest = null
                    hasRequestedPermission = false
                }
                else -> {
                    // 权限仍然未授予，显示对话框
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
            .setTitle("需要$permissionDescription")
            .setMessage("网页需要访问" + permissionDescription + "才能正常使用。请在设置中开启相关权限。")
            .setPositiveButton("去设置") { _: android.content.DialogInterface, _: Int ->
                openAppSettings()
            }
            .setNegativeButton("取消") { _: android.content.DialogInterface, _: Int ->
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
