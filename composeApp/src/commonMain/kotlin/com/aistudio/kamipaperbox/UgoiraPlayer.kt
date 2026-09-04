package com.aistudio.kamipaperbox

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * A Multiplatform Ugoira (Animated Image) Player.
 * In a full implementation, this reads the extracted frames and delay.json from Okio's FileSystem,
 * and renders them precisely in sequence onto a Canvas.
 */
@Composable
fun UgoiraPlayer(
    frames: List<ImageBitmap>,
    delays: List<Int>, // delay in milliseconds for each frame
    modifier: Modifier = Modifier
) {
    if (frames.isEmpty()) return

    var currentFrameIndex by remember { mutableStateOf(0) }

    LaunchedEffect(frames, delays) {
        if (frames.size != delays.size || frames.isEmpty()) return@LaunchedEffect
        
        while (isActive) {
            delay(delays[currentFrameIndex].toLong())
            currentFrameIndex = (currentFrameIndex + 1) % frames.size
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val currentImage = frames.getOrNull(currentFrameIndex)
        if (currentImage != null) {
            // Draw the image scaled to fit or fill the canvas
            drawImage(
                image = currentImage,
                dstSize = androidx.compose.ui.unit.IntSize(size.width.toInt(), size.height.toInt())
            )
        }
    }
}
