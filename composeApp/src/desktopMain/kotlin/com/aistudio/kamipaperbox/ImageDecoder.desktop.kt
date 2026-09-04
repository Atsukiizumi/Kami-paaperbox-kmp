package com.aistudio.kamipaperbox

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

actual fun decodeImageBitmap(bytes: ByteArray): ImageBitmap {
    val image = Image.makeFromEncoded(bytes)
    return image.toComposeImageBitmap()
}
