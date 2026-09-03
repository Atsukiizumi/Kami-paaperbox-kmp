package com.aistudio.kamipaperbox

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.blur
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 当可用宽度 < 600dp 时，采用移动端/安卓交互模式 (底部 NavigationBar)
            // 当可用宽度 >= 600dp 时，采用桌面端交互模式 (左侧 NavigationRail)
            val isCompactScreen = maxWidth < 600.dp

            if (isCompactScreen) {
                // 📱 移动端 / 安卓布局：顶栏标题/状态 + 内容主体 + 底部导航栏
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp
                        ) {
                            Screen.values().forEach { screen ->
                                NavigationBarItem(
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
                                    label = { Text(screen.title, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AppScreenContent(
                            screen = currentScreen,
                            isCompact = true,
                            activeTheme = activeTheme,
                            onThemeChange = { activeTheme = it },
                            onSelect = { selectedWork = it }
                        )
                    }
                }
            } else {
                // 🖥 桌面端 / 平板布局：左侧宽轨导航 (NavigationRail) + 沉浸内容
                Row(modifier = Modifier.fillMaxSize()) {
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

                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        AppScreenContent(
                            screen = currentScreen,
                            isCompact = false,
                            activeTheme = activeTheme,
                            onThemeChange = { activeTheme = it },
                            onSelect = { selectedWork = it }
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
                        isCompact = isCompactScreen,
                        onDismiss = { selectedWork = null }
                    )
                }
            }
        }
    }
}

@Composable
fun AppScreenContent(
    screen: Screen,
    isCompact: Boolean,
    activeTheme: ThemePreset,
    onThemeChange: (ThemePreset) -> Unit,
    onSelect: (WorkCard) -> Unit
) {
    when (screen) {
        Screen.BROWSE -> BrowseView(isCompact = isCompact, onSelect = onSelect)
        Screen.SEARCH -> SearchView(isCompact = isCompact, onSelect = onSelect)
        Screen.VAULT -> VaultView(isCompact = isCompact, onSelect = onSelect)
        Screen.HISTORY -> HistoryView(isCompact = isCompact, onSelect = onSelect)
        Screen.SETTINGS -> SettingsView(
            isCompact = isCompact,
            currentTheme = activeTheme,
            onThemeChange = onThemeChange
        )
    }
}

@Composable
fun BrowseView(isCompact: Boolean, onSelect: (WorkCard) -> Unit) {
    var posts by remember { mutableStateOf<List<WorkCard>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedSource by remember { mutableStateOf(Source.DANBOORU) }
    var currentPage by remember { mutableStateOf(1) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedSource) {
        isLoading = true
        currentPage = 1
        posts = GalleryRepository.fetchPosts(source = selectedSource, page = 1, limit = 40)
        isLoading = false
    }

    suspend fun loadMore() {
        if (isLoading) return
        isLoading = true
        val nextPage = currentPage + 1
        val newPosts = GalleryRepository.fetchPosts(source = selectedSource, page = nextPage, limit = 40)
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
            Column {
                Text(
                    "卷轴浏览",
                    style = if (isCompact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    if (isCompact) "触控流式图库" else "流动的无尽图库 · 桌面高帧率渲染",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Source.entries.forEach { src ->
                    FilterChip(
                        selected = selectedSource == src,
                        onClick = { selectedSource = src },
                        label = { Text(src.displayName, fontSize = if (isCompact) 12.sp else 14.sp) }
                    )
                }
            }
        }

        if (isLoading && posts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            // 瀑布流：手机端自适应 2 列 (minSize = 160.dp)，桌面端自适应 3~6 列 (minSize = 220.dp)
            val gridState = androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState()
            
            // 自动加载更多逻辑
            val shouldLoadMore = remember {
                derivedStateOf {
                    val lastVisibleItemIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    lastVisibleItemIndex >= posts.size - 10 && posts.isNotEmpty() && !isLoading
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
                items(posts) { work ->
                    ArtworkCard(work = work, isCompact = isCompact, onClick = { onSelect(work) })
                }
                
                if (isLoading && posts.isNotEmpty()) {
                    item(span = androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan.FullLine) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
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

@Composable
fun SearchView(isCompact: Boolean, onSelect: (WorkCard) -> Unit) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<WorkCard>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var selectedSource by remember { mutableStateOf(Source.DANBOORU) }
    var currentPage by remember { mutableStateOf(1) }
    val scope = rememberCoroutineScope()

    suspend fun performSearch(newSearch: Boolean = true) {
        if (isSearching) return
        if (newSearch) {
            currentPage = 1
            results = emptyList()
        }
        isSearching = true
        val nextPage = if (newSearch) 1 else currentPage + 1
        val newResults = GalleryRepository.fetchPosts(source = selectedSource, query = query, page = nextPage, limit = 40)
        if (newResults.isNotEmpty()) {
            results = if (newSearch) newResults else results + newResults
            currentPage = nextPage
        }
        isSearching = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = if (isCompact) 12.dp else 20.dp)
    ) {
        Column(modifier = Modifier.padding(top = if (isCompact) 10.dp else 16.dp, bottom = 12.dp)) {
            Text(
                "图谱检索",
                style = if (isCompact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索标签，如 landscape, 1girl...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = {
                    scope.launch { performSearch() }
                })
            )
        }

        // 源切换与热搜
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Source.entries.forEach { src ->
                    FilterChip(
                        selected = selectedSource == src,
                        onClick = { selectedSource = src },
                        label = { Text(src.displayName, fontSize = 11.sp) },
                        modifier = Modifier.height(32.dp)
                    )
                }
            }
            
            Button(
                onClick = { scope.launch { performSearch() } },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("检索", fontSize = 13.sp)
            }
        }

        if (isSearching && results.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val gridState = androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState()
            
            val shouldLoadMore = remember {
                derivedStateOf {
                    val lastVisibleItemIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    lastVisibleItemIndex >= results.size - 10 && results.isNotEmpty() && !isSearching
                }
            }

            LaunchedEffect(shouldLoadMore.value) {
                if (shouldLoadMore.value) {
                    performSearch(newSearch = false)
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
                items(results) { work ->
                    ArtworkCard(work = work, isCompact = isCompact, onClick = { onSelect(work) })
                }
                
                if (isSearching && results.isNotEmpty()) {
                    item(span = androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan.FullLine) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
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

@Composable
fun VaultView(isCompact: Boolean, onSelect: (WorkCard) -> Unit) {
    val items by VaultManager.vaultItems.collectAsState()

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
                    "本地画匣",
                    style = if (isCompact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
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
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("画匣尚空，点击画卷中的收藏即可归入此匣", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 13.sp)
                }
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(minSize = if (isCompact) 160.dp else 220.dp),
                horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 14.dp),
                verticalItemSpacing = if (isCompact) 8.dp else 14.dp,
                contentPadding = PaddingValues(bottom = 24.dp),
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
                    ArtworkCard(work = work, isCompact = isCompact, onClick = { onSelect(work) })
                }
            }
        }
    }
}

@Composable
fun HistoryView(isCompact: Boolean, onSelect: (WorkCard) -> Unit) {
    val items by VaultManager.historyItems.collectAsState()

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
                columns = StaggeredGridCells.Adaptive(minSize = if (isCompact) 160.dp else 220.dp),
                horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 14.dp),
                verticalItemSpacing = if (isCompact) 8.dp else 14.dp,
                contentPadding = PaddingValues(bottom = 24.dp),
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
                    ArtworkCard(work = work, isCompact = isCompact, onClick = { onSelect(work) })
                }
            }
        }
    }
}

@Composable
fun SettingsView(isCompact: Boolean, currentTheme: ThemePreset, onThemeChange: (ThemePreset) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(if (isCompact) 16.dp else 24.dp)
    ) {
        Text(
            "纸谱与首选项",
            style = if (isCompact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))

        Text("和风纸色主题", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ThemePreset.values().forEach { preset ->
                ElevatedCard(
                    onClick = { onThemeChange(preset) },
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (currentTheme == preset) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(if (isCompact) 10.dp else 16.dp)) {
                        Text(
                            when (preset) {
                                ThemePreset.WASHI -> "和纸 (Washi)"
                                ThemePreset.AOSUMI -> "青墨 (Aosumi)"
                                ThemePreset.SHUSHA -> "朱砂 (Shusha)"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isCompact) 13.sp else 15.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (currentTheme == preset) "当前生效" else "点击切换",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        Text("平台自适应特性", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(
            "• 桌面端 (Desktop): 宽轨 NavigationRail + 3~6 列自适应瀑布流 + 鼠标滚轮缩放\n• 安卓/移动端 (Android): Material 3 底部导航栏 + 双列瀑布流 + 全手势触摸缩放\n• 离线引擎: 画匣与足迹全平台持久化",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            lineHeight = 22.sp
        )
    }
}

@Composable
fun ArtworkCard(work: WorkCard, isCompact: Boolean, onClick: () -> Unit) {
    val isInVault = VaultManager.isItemInVault("${work.source}_${work.id}")
    // 仅 Booru 站点需要本地统一处理模糊，Pixiv/Fanbox 由远端控制
    val isNsfw = (work.rating == "q" || work.rating == "e") && (work.source != Source.PIXIV && work.source != Source.FANBOX)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(if (isCompact) 10.dp else 12.dp))
            .clickable(onClick = onClick),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(ratio)
                        .then(if (isNsfw) Modifier.blur(20.dp) else Modifier),
                    contentScale = ContentScale.Crop
                )

                Row(
                    modifier = Modifier.padding(6.dp).align(Alignment.TopStart),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (work.isAi) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("AI", color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                    if (isNsfw) {
                        Surface(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("R18", color = MaterialTheme.colorScheme.onError, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                }

                if (isInVault) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier.padding(6.dp).size(22.dp).align(Alignment.TopEnd)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(if (isCompact) 8.dp else 10.dp)) {
                Text(
                    text = work.title,
                    style = if (isCompact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = work.author,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = work.source.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun LightboxView(work: WorkCard, isCompact: Boolean, onDismiss: () -> Unit) {
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
            .background(Color.Black.copy(alpha = 0.94f))
    ) {
        // 双指捏合缩放、双击或拖拽平移手势
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
                    .padding(if (isCompact) 12.dp else 32.dp)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    ),
                contentScale = ContentScale.Fit
            )
        }

        // 顶部控制条 (包含安卓友好的后退与关闭按钮)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = if (isCompact) 28.dp else 20.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDismiss,
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(if (isCompact) Icons.Default.ArrowBack else Icons.Default.Close, contentDescription = "关闭", tint = Color.White)
            }

            Button(
                onClick = { VaultManager.toggleVault(work) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isInVault) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.25f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(if (isInVault) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (isInVault) "已收藏" else "入画匣", fontSize = 12.sp)
            }
        }

        // 底部作品信息条
        Surface(
            color = Color.Black.copy(alpha = 0.8f),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
        ) {
            Column(modifier = Modifier.padding(if (isCompact) 16.dp else 20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(work.title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.weight(1f))
                    if (work.isAi) {
                        Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp), modifier = Modifier.padding(start = 8.dp)) {
                            Text("AI 生成", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    if (work.rating == "q" || work.rating == "e") {
                        Surface(color = Color.Red.copy(alpha = 0.6f), shape = RoundedCornerShape(4.dp), modifier = Modifier.padding(start = 8.dp)) {
                            Text("R18", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("创作者: ${work.author}  ·  源: ${work.source.displayName}", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                if (work.tags.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "标签: " + work.tags.take(8).joinToString(", "),
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
