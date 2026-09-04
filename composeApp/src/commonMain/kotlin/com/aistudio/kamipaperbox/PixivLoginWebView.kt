package com.aistudio.kamipaperbox

import androidx.compose.runtime.Composable

@Composable
expect fun PixivLoginWebView(
    onAuthCodeReceived: (String, String) -> Unit, // code, codeVerifier
    onCookieReceived: (String) -> Unit // cookie string
)
