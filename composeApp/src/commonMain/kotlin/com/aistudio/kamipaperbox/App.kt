package com.aistudio.kamipaperbox

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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

enum class Screen(val title: String) {
    BROWSE("浏览"),
    SEARCH("检索"),
    VAULT("画匣"),
    HISTORY("足迹"),
    SETTINGS("纸谱")
}

@Composable
fun MainAppView() {
    var currentScreen by remember { mutableStateOf(Screen.BROWSE) }
    var selectedWork by remember { mutableStateOf<WorkCard?>(null) }
    var activeTheme by remember { mutableStateOf(ThemePreset.WASHI) }

    KamiTheme(preset = activeTheme) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Desktop / Tablet 侧边导航栏 (Navigation Rail)
                NavigationRail(
                    modifier = Modifier.width(88.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    header = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "匣",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Kami",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                ) {
                    Screen.values().forEach { screen ->
                        NavigationRailItem(
                            selected = currentScreen == screen,
                            onClick = { currentScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = when (screen) {
                                        Screen.BROWSE -> if (currentScreen == screen) Icons.Filled.GridView else Icons.Outlined.GridView
                                        Screen.SEARCH -> if (currentScreen == screen) Icons.Filled.Search else Icons.Outlined.Search
                                        Screen.VAULT -> if (currentScreen == screen) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder
                                        Screen.HISTORY -> if (currentScreen == screen) Icons.Filled.History else Icons.Outlined.History
                                        Screen.SETTINGS -> if (currentScreen == screen) Icons.Filled.Tune else Icons.Outlined.Tune
                                    },
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title, fontSize = 12.sp) }
                        )
                    }
                }

                // 主内容呈现区
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    when (currentScreen) {
                        Screen.BROWSE -> BrowseView(onSelect = { selectedWork = it })
                        Screen.SEARCH -> SearchView(onSelect = { selectedWork = it })
                        Screen.VAULT -> VaultView(onSelect = { selectedWork = it })
                        Screen.HISTORY -> HistoryView(onSelect = { selectedWork = it })
                        Screen.SETTINGS -> SettingsView(
                            currentTheme = activeTheme,
                            onThemeChange = { activeTheme = it }
                        )
                    }
                }
            }

            // 全局大图浮层灯箱 (Lightbox)
            AnimatedVisibility(
                visible = selectedWork != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedWork?.let { work ->
                    LightboxView(
                        work = work,
                        onDismiss = { selectedWork = null }
                    )
                }
            }
        }
    }
}

@Composable
fun BrowseView(onSelect: (WorkCard) -> Unit) {
    var posts by remember { mutableStateOf<List<WorkCard>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedSource by remember { mutableStateOf(Source.SAFEBOORU) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedSource) {
        isLoading = true
        posts = GalleryRepository.fetchPosts(source = selectedSource, page = 0, limit = 40)
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        // 顶部源切换栏与操作条
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "卷轴浏览",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "流动的无尽图库 · 桌面高帧率渲染",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Source.values().take(3).forEach { src ->
                    FilterChip(
                        selected = selectedSource == src,
                        onClick = { selectedSource = src },
                        label = { Text(src.displayName) }
                    )
                }
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            // 桌面端自适应 3~5 列瀑布流
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(minSize = 220.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalItemSpacing = 14.dp,
                contentPadding = PaddingValues(bottom = 32.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(posts) { work ->
                    ArtworkCard(work = work, onClick = { onSelect(work) })
                }
            }
        }
    }
}

@Composable
fun SearchView(onSelect: (WorkCard) -> Unit) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<WorkCard>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Column(modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)) {
            Text(
                "图谱检索",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("输入标签，如 genshin_impact, 1girl, landscape...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("推荐标签: landscape, official_art, anime, sky", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Button(
                onClick = {
                    scope.launch {
                        isSearching = true
                        results = GalleryRepository.fetchPosts(query = query, limit = 40)
                        isSearching = false
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("检索")
            }
        }

        if (isSearching) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(minSize = 220.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalItemSpacing = 14.dp,
                contentPadding = PaddingValues(bottom = 32.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(results) { work ->
                    ArtworkCard(work = work, onClick = { onSelect(work) })
                }
            }
        }
    }
}

@Composable
fun VaultView(onSelect: (WorkCard) -> Unit) {
    val items by VaultManager.vaultItems.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "本地画匣",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "共归档 ${items.size} 卷藏画 · 离线持久化",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("画匣尚空，在浏览画卷时点击收藏即可归入此匣", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(minSize = 220.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalItemSpacing = 14.dp,
                contentPadding = PaddingValues(bottom = 32.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items) { vItem ->
                    val work = WorkCard(
                        source = vItem.source,
                        id = vItem.id,
                        title = vItem.title,
                        author = vItem.author,
                        thumb = vItem.thumb,
                        originalUrl = vItem.originalUrl,
                        tags = vItem.tags
                    )
                    ArtworkCard(work = work, onClick = { onSelect(work) })
                }
            }
        }
    }
}

@Composable
fun HistoryView(onSelect: (WorkCard) -> Unit) {
    val items by VaultManager.historyItems.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("浏览足迹", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("最近查阅的历史作品", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            if (items.isNotEmpty()) {
                TextButton(onClick = { VaultManager.clearHistory() }) {
                    Text("清空足迹")
                }
            }
        }

        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无足迹记录", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(minSize = 220.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalItemSpacing = 14.dp,
                contentPadding = PaddingValues(bottom = 32.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items) { hItem ->
                    val work = WorkCard(
                        source = hItem.source,
                        id = hItem.id,
                        title = hItem.title,
                        author = "Artist",
                        thumb = hItem.thumb,
                        originalUrl = hItem.originalUrl
                    )
                    ArtworkCard(work = work, onClick = { onSelect(work) })
                }
            }
        }
    }
}

@Composable
fun SettingsView(currentTheme: ThemePreset, onThemeChange: (ThemePreset) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("纸谱与首选项", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        Text("和风纸色主题", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ThemePreset.values().forEach { preset ->
                ElevatedCard(
                    onClick = { onThemeChange(preset) },
                    modifier = Modifier.width(160.dp).padding(4.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (currentTheme == preset) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            when (preset) {
                                ThemePreset.WASHI -> "和纸 (Washi)"
                                ThemePreset.AOSUMI -> "青墨 (Aosumi)"
                                ThemePreset.SHUSHA -> "朱砂 (Shusha)"
                            },
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (currentTheme == preset) "当前生效" else "点击切换",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        Text("平台与运行环境", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(
            "架构: Compose Multiplatform (CMP) Desktop-First\n目标平台: Windows / macOS / Linux / iOS / Android / Web\n图形引擎: Skia / DirectX / Metal 硬件级加速",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ArtworkCard(work: WorkCard, onClick: () -> Unit) {
    val isInVault = VaultManager.isItemInVault("${work.source}_${work.id}")

    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box {
                val ratio = if (work.width != null && work.height != null && work.width > 0) {
                    (work.width.toFloat() / work.height.toFloat()).coerceIn(0.6f, 1.8f)
                } else 1.0f

                AsyncImage(
                    model = work.thumb,
                    contentDescription = work.title,
                    modifier = Modifier.fillMaxWidth().aspectRatio(ratio),
                    contentScale = ContentScale.Crop
                )

                if (work.isAi) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(8.dp).align(Alignment.TopStart)
                    ) {
                        Text("AI", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                if (isInVault) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier.padding(8.dp).size(24.dp).align(Alignment.TopEnd)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = work.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = work.author,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = work.source.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun LightboxView(work: WorkCard, onDismiss: () -> Unit) {
    val isInVault = VaultManager.isItemInVault("${work.source}_${work.id}")
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    LaunchedEffect(work) {
        VaultManager.recordHistory(work)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f))
    ) {
        // 居中大图与手势缩放支持 (双指捏合 / 拖拽)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.8f, 5.0f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = work.originalUrl,
                contentDescription = work.title,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    ),
                contentScale = ContentScale.Fit
            )
        }

        // 顶部控制条
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDismiss,
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Close, contentDescription = "关闭", tint = Color.White)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { VaultManager.toggleVault(work) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isInVault) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(if (isInVault) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (isInVault) "已入画匣" else "归入画匣")
                }
            }
        }

        // 底部作品信息条
        Surface(
            color = Color.Black.copy(alpha = 0.75f),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(work.title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("创作者: ${work.author}  ·  源: ${work.source.displayName}", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                if (work.tags.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "标签: " + work.tags.take(10).joinToString(", "),
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
