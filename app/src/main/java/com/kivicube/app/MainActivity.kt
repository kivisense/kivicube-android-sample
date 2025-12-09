package com.kivicube.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kivicube.app.ui.theme.KivicubeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KivicubeTheme {
                MainScreen(
                    onOpenKivicubeScene = { sceneId ->
                        openWebView("https://www.kivicube.com/scenes/$sceneId")
                    },
                    onOpenKivicubeCollection = { collectionId ->
                        openWebView("https://www.kivicube.com/collections/$collectionId")
                    }
                )
            }
        }
    }


    private fun openWebView(url: String) {
        val intent = Intent(this, WebViewActivity::class.java).apply {
            putExtra(WebViewActivity.EXTRA_URL, url)
        }
        startActivity(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onOpenKivicubeScene: (String) -> Unit,
    onOpenKivicubeCollection: (String) -> Unit
) {
    var sceneId by remember { mutableStateOf("zIUkUZDrNp2HDDf562A2zS2v85uCcWJE") }
    var errorText by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kivicube",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var collectionId by remember { mutableStateOf("hw5xqx") }
            var collectionErrorText by remember { mutableStateOf<String?>(null) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Kivicube 合辑",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = collectionId,
                        onValueChange = {
                            collectionId = it
                            collectionErrorText = null
                        },
                        label = { Text("合辑ID (6位字符)") },
                        placeholder = { Text("输入6位合辑ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Ascii
                        ),
                        isError = collectionErrorText != null,
                        trailingIcon = {
                            if (collectionId.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        collectionId = ""
                                        collectionErrorText = null
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "清除"
                                    )
                                }
                            }
                        }
                    )

                    if (collectionErrorText != null) {
                        Text(
                            text = collectionErrorText!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (collectionId.isEmpty()) {
                                collectionErrorText = "合辑ID不能为空"
                            } else if (collectionId.length != 6) {
                                collectionErrorText = "合辑ID必须为6位字符"
                            } else {
                                onOpenKivicubeCollection(collectionId)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("打开合辑")
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Kivicube 场景",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = sceneId,
                        onValueChange = {
                            sceneId = it
                            errorText = null
                        },
                        label = { Text("场景ID (32位字符)") },
                        placeholder = { Text("输入32位场景ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Ascii
                        ),
                        isError = errorText != null,
                        trailingIcon = {
                            if (sceneId.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        sceneId = ""
                                        errorText = null
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "清除"
                                    )
                                }
                            }
                        }
                    )

                    if (errorText != null) {
                        Text(
                            text = errorText!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (sceneId.isEmpty()) {
                                errorText = "场景ID不能为空"
                            } else if (sceneId.length != 32) {
                                errorText = "场景ID必须为32位字符"
                            } else {
                                onOpenKivicubeScene(sceneId)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("打开场景")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    KivicubeTheme {
        MainScreen(
            onOpenKivicubeScene = {},
            onOpenKivicubeCollection = {}
        )
    }
}
