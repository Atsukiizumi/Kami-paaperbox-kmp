import re

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/LightboxView.kt", "r") as f:
    text = f.read()

ugoira_display = """        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(currentDisplayUrl) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.7f, 6.0f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }
                .pointerInput(currentDisplayUrl) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (scale > 1.2f) {
                                scale = 1f
                                offsetX = 0f
                                offsetY = 0f
                            } else {
                                scale = 2.5f
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (work.isUgoira) {
                val data = ugoiraData
                if (data != null) {
                    UgoiraPlayer(
                        frames = data.first,
                        delays = data.second,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = if (isCompact) 8.dp else 24.dp, vertical = 72.dp)
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offsetX,
                                translationY = offsetY
                            )
                    )
                } else {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                AsyncImage(
                    model = currentDisplayUrl,
                    contentDescription = work.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = if (isCompact) 8.dp else 24.dp, vertical = 72.dp)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        ),
                    contentScale = ContentScale.Fit
                )
            }
"""

text = re.sub(r'        Box\(\s+modifier = Modifier\s+\.fillMaxSize\(\)\s+\.pointerInput\(currentDisplayUrl\) \{\s+detectTransformGestures.*?\s+AsyncImage\([^)]+\s+contentScale = ContentScale\.Fit\s+\)', ugoira_display, text, flags=re.DOTALL)

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/LightboxView.kt", "w") as f:
    f.write(text)

