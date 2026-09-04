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

    fun loadPage(source: Source, page: Int) {
        scope.launch {
            isLoading = true
            currentPage = page
            posts = GalleryRepository.fetchPosts(
                source = source,
                page = page,
                limit = 50,
                includeR18 = prefs.includeR18,
                aiFilterMode = prefs.aiFilterMode,
                usePixivMirror = prefs.usePixivMirror
            )
            isLoading = false
        }
    }

    LaunchedEffect(selectedSource, prefs.includeR18, prefs.aiFilterMode, prefs.usePixivMirror) {
        loadPage(selectedSource, 1)
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

                Spacer(modifier = Modifier.width(8.dp))
                // 列数切换器
                listOf(2, 4, 6, 9).forEach { cols ->
                    FilterChip(
                        selected = prefs.gridColumns == cols,
                        onClick = { SettingsManager.setGridColumns(cols) },
                        label = { Text("$cols") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                }

                IconButton(
                    onClick = { loadPage(selectedSource, currentPage) },
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
                    Button(onClick = { loadPage(selectedSource, 1) }) {
                        Text("重试刷新")
                    }
                }
            }
        } else {
            val gridState = rememberLazyStaggeredGridState()

            // 滚动回顶部
            LaunchedEffect(currentPage, selectedSource) {
                gridState.scrollToItem(0)
            }

            LazyVerticalStaggeredGrid(
                state = gridState,
                columns = StaggeredGridCells.Fixed(prefs.gridColumns),
                horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 14.dp),
                verticalItemSpacing = if (isCompact) 8.dp else 14.dp,
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                items(posts, key = { "${it.source}_${it.id}_${it.thumb}" }) { work ->
                    ArtworkCard(
                        work = work,
                        isCompact = isCompact,
                        onClick = { onSelect(work) }
                    )
                }
            }

            // 底部翻页控制器
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { if (currentPage > 1) loadPage(selectedSource, currentPage - 1) },
                        enabled = currentPage > 1
                    ) {
                        Text("上一页")
                    }
                    
                    Text(
                        "第 $currentPage 页", 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    TextButton(
                        onClick = { loadPage(selectedSource, currentPage + 1) },
                        enabled = posts.size == 50 // Assume more pages if full page returned
                    ) {
                        Text("下一页")
                    }
                }
            }
        }
    }
}
