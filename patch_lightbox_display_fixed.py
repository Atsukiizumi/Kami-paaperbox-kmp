with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/LightboxView.kt", "r") as f:
    text = f.read()

target = """            contentAlignment = Alignment.Center
        ) {
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
        }"""

replacement = """            contentAlignment = Alignment.Center
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
        }"""

text = text.replace(target, replacement)

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/LightboxView.kt", "w") as f:
    f.write(text)

