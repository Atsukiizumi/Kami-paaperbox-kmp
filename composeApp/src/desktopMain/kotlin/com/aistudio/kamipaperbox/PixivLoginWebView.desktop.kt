package com.aistudio.kamipaperbox

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
actual fun PixivLoginWebView(
    onAuthCodeReceived: (String, String) -> Unit,
    onCookieReceived: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Desktop WebView Login Not Supported Yet")
    }
}
