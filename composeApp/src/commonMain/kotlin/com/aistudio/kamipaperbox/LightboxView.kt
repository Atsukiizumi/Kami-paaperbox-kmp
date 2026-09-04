package com.aistudio.kamipaperbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun LightboxView(
    work: WorkCard,
    isCompact: Boolean,
    onDismiss: () -> Unit,
    onTagClick: (tag: String, source: Source) -> Unit,
    onAuthorClick: (author: String, source: Source) -> Unit
) {
    val isInVault = VaultManager.isItemInVault("${work.source}_${work.id}")
    val imageList = if (work.additionalImages.isNotEmpty()) work.additionalImages else listOf(work.originalUrl)
    var currentPageIndex by remember(work) { mutableStateOf(0) }
    
    var scale by remember(currentPageIndex, work) { mutableStateOf(1f) }
    var offsetX by remember(currentPageIndex, work) { mutableStateOf(0f) }
    var offsetY by remember(currentPageIndex, work) { mutableStateOf(0f) }

    val currentDisplayUrl = imageList.getOrNull(currentPageIndex) ?: work.originalUrl

    LaunchedEffect(work) {
        VaultManager.recordHistory(work)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
    ) {
        // 主大图展示区 (支持双指捏合缩放、拖拽平移、双击缩放复位)
        Box(
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

        // 多图翻页浮动控制器 (当作品包含多图或漫画模式时)
        if (imageList.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentPageIndex > 0) {
                            currentPageIndex--
                        }
                    },
                    enabled = currentPageIndex > 0,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.6f),
                        contentColor = Color.White,
                        disabledContainerColor = Color.Black.copy(alpha = 0.2f),
                        disabledContentColor = Color.White.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "上一页")
                }

                IconButton(
                    onClick = {
                        if (currentPageIndex < imageList.size - 1) {
                            currentPageIndex++
                        }
                    },
                    enabled = currentPageIndex < imageList.size - 1,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.6f),
                        contentColor = Color.White,
                        disabledContainerColor = Color.Black.copy(alpha = 0.2f),
                        disabledContentColor = Color.White.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "下一页")
                }
            }
        }

        // 顶部控制条 (返回、页码信息、画匣收藏、快速搜作者)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = if (isCompact) 28.dp else 20.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onDismiss,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.55f))
                ) {
                    Icon(
                        if (isCompact) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Close,
                        contentDescription = "返回",
                        tint = Color.White
                    )
                }

                if (imageList.size > 1) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "${currentPageIndex + 1} / ${imageList.size}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                val snackbarHostState = remember { SnackbarHostState() }
                
                IconButton(
                    onClick = {
                        try {
                            uriHandler.openUri(currentDisplayUrl)
                        } catch (e: Exception) {
                            coroutineScope.launch { snackbarHostState.showSnackbar("无法打开链接") }
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.55f))
                ) {
                    Icon(
                        Icons.Default.Public,
                        contentDescription = "在浏览器中打开",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = {
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(currentDisplayUrl))
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("图片链接已复制到剪贴板")
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.55f))
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "复制链接",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Button(
                    onClick = { VaultManager.toggleVault(work) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isInVault) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.25f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        if (isInVault) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (isInVault) "已存入画匣" else "入画匣", fontSize = 12.sp)
                }
            }
        }

        // 底部作品信息与交互标签栏 (底栏弹层)
        Surface(
            color = Color.Black.copy(alpha = 0.88f),
            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                // 如果有多图，在此处显示缩略图导航带 (对标 v0.8.51 底部缩略图分页条)
                if (imageList.size > 1) {
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(imageList.size) { index ->
                            val url = imageList[index]
                            val isSelected = index == currentPageIndex
                            
                            AsyncImage(
                                model = url,
                                contentDescription = "Page ${index + 1}",
                                modifier = Modifier
                                    .size(if (isSelected) 56.dp else 48.dp)
                                    .clickable { currentPageIndex = index }
                                    .background(Color.DarkGray, RoundedCornerShape(8.dp))
                                    .padding(if (isSelected) 2.dp else 0.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                // 标题与勋章
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = work.title.ifBlank { "#${work.id}" },
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (work.isAi) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Text(
                                "AI 生成",
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (work.rating == "q" || work.rating == "e") {
                        Surface(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Text(
                                "R18",
                                color = MaterialTheme.colorScheme.onError,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))

                // 创作者信息 (支持点击跳转搜作者)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "作者:",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable {
                            onAuthorClick(work.author, work.source)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = work.author.ifBlank { "Unknown" },
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.width(2.dp))
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "搜索此作者",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }

                    Text(
                        text = "· 来源: ${work.source.displayName}",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )

                    if (work.width != null && work.height != null) {
                        Text(
                            text = "· ${work.width}×${work.height}",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }
                }

                // 交互标签胶囊流 (支持点击追加搜索，显示译名)
                if (work.tags.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        work.tags.forEach { tag ->
                            val translation = work.translatedTags[tag]
                            val label = if (!translation.isNullOrBlank()) "$tag · $translation" else tag

                            Surface(
                                color = Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.clickable {
                                    onTagClick(tag, work.source)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "#",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.width(2.dp))
                                    Text(
                                        text = label,
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
