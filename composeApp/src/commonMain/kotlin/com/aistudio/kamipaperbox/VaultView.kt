package com.aistudio.kamipaperbox

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VaultView(
    isCompact: Boolean,
    onSelect: (WorkCard) -> Unit
) {
    val items by VaultManager.vaultItems.collectAsState()
    var selectedFilterSource by remember { mutableStateOf<Source?>(null) }
    var filterQuery by remember { mutableStateOf("") }

    val filteredItems = remember(items, selectedFilterSource, filterQuery) {
        items.filter { item ->
            val matchesSource = selectedFilterSource == null || item.source == selectedFilterSource
            val matchesQuery = filterQuery.isBlank() ||
                    item.title.contains(filterQuery, ignoreCase = true) ||
                    item.author.contains(filterQuery, ignoreCase = true) ||
                    item.tags.any { it.contains(filterQuery, ignoreCase = true) }
            matchesSource && matchesQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = if (isCompact) 12.dp else 20.dp)
    ) {
        // 顶栏统计与标题
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (isCompact) 10.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "本地画匣",
                    style = if (isCompact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "共藏 ${items.size} 幅作品 · 离线全平台持久化",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // 源分类过滤 Chips
        if (items.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedFilterSource == null,
                    onClick = { selectedFilterSource = null },
                    label = { Text("全部 (${items.size})", fontSize = 11.sp) }
                )
                Source.entries.forEach { src ->
                    val count = items.count { it.source == src }
                    if (count > 0) {
                        FilterChip(
                            selected = selectedFilterSource == src,
                            onClick = { selectedFilterSource = src },
                            label = { Text("${src.displayName} ($count)", fontSize = 11.sp) }
                        )
                    }
                }
            }

            // 画匣内快速模糊搜索
            OutlinedTextField(
                value = filterQuery,
                onValueChange = { filterQuery = it },
                placeholder = { Text("在藏画中过滤标题、作者或标签...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(bottom = 6.dp)
            )
            Spacer(Modifier.height(8.dp))
        }

        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "画匣尚空，快去卷轴或检索中搜罗心仪之作吧",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
            }
        } else if (filteredItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "未匹配到藏画，请调整过滤条件",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(minSize = if (isCompact) 160.dp else 220.dp),
                horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 14.dp),
                verticalItemSpacing = if (isCompact) 8.dp else 14.dp,
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredItems, key = { it.key }) { vItem ->
                    val work = WorkCard(
                        source = vItem.source,
                        id = vItem.id,
                        title = vItem.title,
                        author = vItem.author,
                        thumb = vItem.thumb,
                        originalUrl = vItem.originalUrl,
                        tags = vItem.tags
                    )
                    ArtworkCard(
                        work = work,
                        isCompact = isCompact,
                        onClick = { onSelect(work) }
                    )
                }
            }
        }
    }
}
