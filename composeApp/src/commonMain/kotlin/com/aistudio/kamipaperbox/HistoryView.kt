package com.aistudio.kamipaperbox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HistoryView(
    isCompact: Boolean,
    onSelect: (WorkCard) -> Unit,
    onAuthorSelect: ((String, Source) -> Unit)? = null
) {
    val items by VaultManager.historyItems.collectAsState()
    val creatorItems by VaultManager.creatorHistoryItems.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Artwork, 1: Creator

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清空足迹") },
            text = { Text("确定要清除所有的历史浏览记录吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (selectedTab == 0) {
                            VaultManager.clearHistory()
                        } else {
                            VaultManager.clearCreatorHistory()
                        }
                        showClearDialog = false
                    }
                ) {
                    Text("确认清空", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = if (isCompact) 12.dp else 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (isCompact) 10.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "浏览足迹",
                    style = if (isCompact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (selectedTab == 0) "记录最近查阅的 ${items.size} 卷作品" else "记录最近关注的 ${creatorItems.size} 位画师",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            if ((selectedTab == 0 && items.isNotEmpty()) || (selectedTab == 1 && creatorItems.isNotEmpty())) {
                TextButton(onClick = { showClearDialog = true }) {
                    Icon(
                        Icons.Outlined.DeleteSweep,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("清空足迹")
                }
            }
        }
        
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("作品 (Artworks)", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("画师 (Creators)", fontWeight = FontWeight.Bold) }
            )
        }

        if (selectedTab == 0) {
            if (items.isEmpty()) {
                EmptyHistoryPlaceholder()
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(minSize = if (isCompact) 160.dp else 220.dp),
                    horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 14.dp),
                    verticalItemSpacing = if (isCompact) 8.dp else 14.dp,
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items, key = { it.key }) { hItem ->
                        val work = WorkCard(
                            source = hItem.source,
                            id = hItem.id,
                            title = hItem.title,
                            author = "Artist",
                            thumb = hItem.thumb,
                            originalUrl = hItem.originalUrl
                        )
                        ArtworkCard(
                            work = work,
                            isCompact = isCompact,
                            onClick = { onSelect(work) }
                        )
                    }
                }
            }
        } else {
            if (creatorItems.isEmpty()) {
                EmptyHistoryPlaceholder()
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(creatorItems.size) { index ->
                        val cItem = creatorItems[index]
                        CreatorHistoryRow(cItem, onAuthorSelect)
                    }
                }
            }
        }
    }
}

@Composable
fun CreatorHistoryRow(item: CreatorHistoryItem, onAuthorSelect: ((String, Source) -> Unit)?) {
    androidx.compose.material3.Card(
        onClick = {
            onAuthorSelect?.invoke(item.authorId.ifBlank { item.authorName }, item.source)
        },
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            coil3.compose.AsyncImage(
                model = item.thumb,
                contentDescription = item.authorName,
                modifier = Modifier
                    .size(56.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = item.authorName.ifBlank { "Unknown Artist" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Text(item.source.displayName, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                    if (item.authorId.isNotBlank()) {
                        Spacer(Modifier.width(8.dp))
                        Text("ID: ${item.authorId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyHistoryPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.History,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
            )
            Spacer(Modifier.height(14.dp))
            Text(
                "暂无记录",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
        }
    }
}
