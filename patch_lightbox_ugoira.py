import re

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/LightboxView.kt", "r") as f:
    text = f.read()

ugoira_imports = """import androidx.compose.runtime.produceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.graphics.ImageBitmap
"""
text = text.replace("import kotlinx.coroutines.launch", "import kotlinx.coroutines.launch\n" + ugoira_imports)


ugoira_state = """    val isInVault = VaultManager.isItemInVault("${work.source}_${work.id}")
    
    val ugoiraData by produceState<Pair<List<ImageBitmap>, List<Int>>?>(initialValue = null, work) {
        if (work.isUgoira) {
            withContext(Dispatchers.IO) {
                val meta = ApiClient.fetchPixivUgoiraMetadata(work.id)
                val zipUrl = meta?.zip_urls?.large ?: meta?.zip_urls?.medium
                if (zipUrl != null && meta.frames != null) {
                    val zipBytes = ApiClient.downloadBytes(zipUrl)
                    if (zipBytes != null) {
                        val extracted = extractZip(zipBytes)
                        val frames = mutableListOf<ImageBitmap>()
                        val delays = mutableListOf<Int>()
                        meta.frames.forEach { frameMeta ->
                            val fileBytes = extracted[frameMeta.file]
                            if (fileBytes != null) {
                                frames.add(decodeImageBitmap(fileBytes))
                                delays.add(frameMeta.delay)
                            }
                        }
                        value = Pair(frames, delays)
                    }
                }
            }
        }
    }
"""

text = text.replace('    val isInVault = VaultManager.isItemInVault("${work.source}_${work.id}")', ugoira_state)


ugoira_display = """        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        if (scale > 1f) {
                            offsetX += pan.x
                            offsetY += pan.y
                        } else {
                            offsetX = 0f
                            offsetY = 0f
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (scale > 1f) {
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

# Replace the existing Box logic
text = re.sub(r'        Box\(\s+modifier = Modifier\s+\.fillMaxSize\(\)\s+\.pointerInput\(Unit\) \{.*?contentScale = ContentScale\.Fit\s+\)', ugoira_display, text, flags=re.DOTALL)

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/LightboxView.kt", "w") as f:
    f.write(text)

