package com.aistudio.kamipaperbox

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.viewinterop.AndroidView
import okio.ByteString.Companion.encodeUtf8
import okio.ByteString.Companion.toByteString

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun PixivLoginWebView(
    onAuthCodeReceived: (String, String) -> Unit,
    onCookieReceived: (String) -> Unit
) {
    val verifier = remember {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9') + listOf('-', '.', '_', '~')
        (1..43).map { allowedChars.random() }.joinToString("")
    }
    
    val challenge = remember(verifier) {
        verifier.encodeUtf8().sha256().base64Url().replace("=", "")
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.userAgentString = "PixivAndroidApp/5.0.234 (Android 11; Pixel 5)"
                
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        val url = request.url.toString()
                        if (url.startsWith("pixiv://")) {
                            val code = request.url.getQueryParameter("code")
                            if (code != null) {
                                val cookie = CookieManager.getInstance().getCookie("https://pixiv.net") ?: ""
                                onCookieReceived(cookie)
                                onAuthCodeReceived(code, verifier)
                            }
                            return true
                        }
                        return false
                    }
                    
                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)
                        if (url.contains("pixiv.net")) {
                            val cookie = CookieManager.getInstance().getCookie("https://pixiv.net") ?: ""
                            if (cookie.contains("PHPSESSID")) {
                                onCookieReceived(cookie)
                            }
                        }
                    }
                }
                
                val loginUrl = "https://app-api.pixiv.net/web/v1/login?code_challenge=$challenge&code_challenge_method=S256&client=pixiv-android"
                loadUrl(loginUrl)
            }
        },
        update = { webView -> }
    )
}
