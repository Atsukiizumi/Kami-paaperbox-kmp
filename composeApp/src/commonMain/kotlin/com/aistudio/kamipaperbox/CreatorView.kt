package com.aistudio.kamipaperbox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorView(
    author: String,
    source: Source,
    isCompact: Boolean,
    onDismiss: () -> Unit,
    onSelectWork: (WorkCard) -> Unit
) {
    var posts by remember { mutableStateOf<List<WorkCard>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val prefs by SettingsManager.prefs.collectAsState()

    LaunchedEffect(author, source) {
        isLoading = true
        val query = if (source == Source.DANBOORU || source == Source.KONACHAN || source == Source.YANDE) {
            "artist:$author"
        } else {
            author
        }
        
        posts = GalleryRepository.fetchPosts(
            source = source,
            query = query,
            page = 1,
            limit = 50,
            includeR18 = prefs.includeR18,
            aiFilterMode = prefs.aiFilterMode,
            usePixivMirror = prefs.usePixivMirror
        )
        
        // Record creator history when loaded
        if (posts.isNotEmpty()) {
            val thumb = posts.firstOrNull()?.thumb ?: ""
            VaultManager.recordCreatorHistory(
                source = source,
                authorId = author,
                authorName = author, // we don't have exact author name vs id mapping in all sources easily, so use author for both
                thumb = thumb
            )
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(author, fontWeight = FontWeight.Bold)
                        Text(source.displayName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (posts.isEmpty()) {
                Text(
                    "未找到相关作品",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(prefs.gridColumns),
                    horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 14.dp),
                    verticalItemSpacing = if (isCompact) 8.dp else 14.dp,
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(posts, key = { "${it.source}_${it.id}_${it.thumb}" }) { work ->
                        ArtworkCard(
                            work = work,
                            isCompact = isCompact,
                            onClick = { onSelectWork(work) }
                        )
                    }
                }
            }
        }
    }
}
