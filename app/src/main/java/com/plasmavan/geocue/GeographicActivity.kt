package com.plasmavan.geocue

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.plasmavan.geocue.ui.theme.GeocueTheme

class GeographicActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val latitude: String = intent.getStringExtra("latitude").toString()
        val longitude: String = intent.getStringExtra("longitude").toString()
        val name: String = intent.getStringExtra("selectedName").toString()

        // Create the URL with dynamic coordinates and zoom level
        val url: String = "https://maps.gsi.go.jp/#10/$latitude/$longitude/&base=english&ls=english"

        setContent {
            GeocueTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GeographicalUI(url, name)
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if(keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
            super.onKeyDown(keyCode, event)
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    private fun GeographicalUI(url: String, name: String) {
        var webView by remember { mutableStateOf<WebView?>(null) }
        var isLoading by remember { mutableStateOf(true) }

        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),

                        // This API is NOT recommended to use.
                        onClick = {
                            webView?.loadUrl(url)
                        }
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(16.dp),
                            text = name,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(
                        modifier = Modifier
                            .width(16.dp)
                    )
                    IconButton(
                        onClick = {
                            webView?.reload()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        webView?.visibility = View.INVISIBLE
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(60.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                AndroidView(
                    modifier = Modifier
                        .fillMaxSize(),
                    factory = {
                        WebView(it).apply {
                            settings.javaScriptEnabled = true
                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    isLoading = true
                                    webView?.visibility = View.INVISIBLE
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isLoading = false
                                    webView?.visibility = View.VISIBLE
                                }

                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    return super.shouldOverrideUrlLoading(view, request)
                                }
                            }
                            loadUrl(url)
                        }
                    },
                    update = {
                        webView = it
                        it.loadUrl(url)
                    }
                )
            }
        }
    }
}