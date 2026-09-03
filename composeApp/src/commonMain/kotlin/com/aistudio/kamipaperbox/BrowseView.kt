package com.aistudio.kamipaperbox

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun BrowseView(
    isCompact: Boolean,
    onSelect: (WorkCard) -> Unit
) {
    val prefs by SettingsManager.prefs.collectAsState()
    var posts by remember { mutableStateOf<List<WorkCard>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedSource by remember { mutableStateOf(Source.DANBOORU) }
    var currentPage by remember { mutableStateOf(1) }
    val scope = rememberCoroutineScope()

    fun refresh(source: Source) {
        scope.launch {
            isLoading = true
            currentPage = 1
            posts = GalleryRepository.fetchPosts(
                source = source,
                page = 1,
                limit = 40,
                includeR18 = prefs.includeR18,
                aiFilterMode = prefs.aiFilterMode,
                usePixivMirror = prefs.usePixivMirror
            )
            isLoading = false
        }
    }

    LaunchedEffect(selectedSource, prefs.includeR18, prefs.aiFilterMode, prefs.usePixivMirror) {
        refresh(selectedSource)
    }

    suspend fun loadMore() {
        if (isLoading) return
        isLoading = true
        val nextPage = currentPage + 1
        val newPosts = GalleryRepository.fetchPosts(
            source = selectedSource,
            page = nextPage,
            limit = 40,
            includeR18 = prefs.includeR18,
            aiFilterMode = prefs.aiFilterMode,
            usePixivMirror = prefs.usePixivMirror
        )
        if (newPosts.isNotEmpty()) {
            posts = posts + newPosts
            currentPage = nextPage
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = if (isCompact) 12.dp else 20.dp)
    ) {
        // 顶部源切换栏与操作条
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (isCompact) 10.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.padding(end = 12.dp)) {
                Text(
                    "卷轴浏览",
                    style = if (isCompact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    if (isCompact) "触控流式画卷 · ${selectedSource.displayName}" else "流动的无尽画卷 · ${selectedSource.displayName} 实时同步",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Source.entries.forEach { src ->
                    FilterChip(
                        selected = selectedSource == src,
                        onClick = { selectedSource = src },
                        label = { Text(src.displayName, fontSize = if (isCompact) 12.sp else 13.sp) }
                    )
                }

                IconButton(
                    onClick = { refresh(selectedSource) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "刷新",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (isLoading && posts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (posts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "未能获取到内容，请检查网络或点击刷新",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = { refresh(selectedSource) }) {
                        Text("重试刷新")
                    }
                }
            }
        } else {
            val gridState = rememberLazyStaggeredGridState()

            val shouldLoadMore = remember {
                derivedStateOf {
                    val lastVisibleItemIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    lastVisibleItemIndex >= posts.size - 8 && posts.isNotEmpty() && !isLoading
                }
            }

            LaunchedEffect(shouldLoadMore.value) {
                if (shouldLoadMore.value) {
                    loadMore()
                }
            }

            LazyVerticalStaggeredGrid(
                state = gridState,
                columns = StaggeredGridCells.Adaptive(minSize = if (isCompact) 160.dp else 220.dp),
                horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 14.dp),
                verticalItemSpacing = if (isCompact) 8.dp else 14.dp,
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(posts, key = { "${it.source}_${it.id}_${it.thumb}" }) { work ->
                    ArtworkCard(
                        work = work,
                        isCompact = isCompact,
                        onClick = { onSelect(work) }
                    )
                }

                if (isLoading && posts.isNotEmpty()) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    }
                }
            }
        }
    }
}
