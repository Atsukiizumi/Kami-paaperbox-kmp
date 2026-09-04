package com.aistudio.kamipaperbox

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class Screen(val title: String) {
    BROWSE("浏览"),
    SEARCH("检索"),
    VAULT("画匣"),
    HISTORY("足迹"),
    SETTINGS("纸谱"),
    QUEUE("传输")
}

@Composable
fun MainAppView() {
    val prefs by SettingsManager.prefs.collectAsState()
    var currentScreen by remember { mutableStateOf(Screen.BROWSE) }
    var selectedWork by remember { mutableStateOf<WorkCard?>(null) }
    var selectedCreator by remember { mutableStateOf<Pair<String, Source>?>(null) }
    
    // 用于跨页面（如详情页点击标签/作者）携带到检索页面的参数
    var searchTargetQuery by remember { mutableStateOf<String?>(null) }
    var searchTargetSource by remember { mutableStateOf<Source?>(null) }

    KamiTheme(preset = prefs.themePreset) {
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
                            Screen.entries.forEach { screen ->
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
                                            Screen.QUEUE -> if (currentScreen == screen) Icons.Filled.Download else Icons.Outlined.Download

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
                            initialSearchQuery = searchTargetQuery,
                            initialSearchSource = searchTargetSource,
                            onSelect = { selectedWork = it },
                            onSelectCreator = { author, source -> selectedCreator = Pair(author, source) }
                        )
                    }
                }
            } else {
                // 🖥 桌面端 / 平板布局：左侧宽轨导航 (NavigationRail) + 沉浸式内容主体
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
                        Screen.entries.forEach { screen ->
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
                                            Screen.QUEUE -> if (currentScreen == screen) Icons.Filled.Download else Icons.Outlined.Download

                                        },
                                        contentDescription = screen.title
                                    )
                                },
                                label = { Text(screen.title, fontSize = 12.sp) }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        AppScreenContent(
                            screen = currentScreen,
                            isCompact = false,
                            initialSearchQuery = searchTargetQuery,
                            initialSearchSource = searchTargetSource,
                            onSelect = { selectedWork = it },
                            onSelectCreator = { author, source -> selectedCreator = Pair(author, source) }
                        )
                    }
                }
            }

            // 全局大图灯箱浮层 (Lightbox)
            AnimatedVisibility(
                visible = selectedWork != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedWork?.let { work ->
                    LightboxView(
                        work = work,
                        isCompact = isCompactScreen,
                        onDismiss = { selectedWork = null },
                        onTagClick = { tag, source ->
                            selectedWork = null
                            searchTargetQuery = if (currentScreen == Screen.SEARCH && !searchTargetQuery.isNullOrBlank()) {
                                "$searchTargetQuery $tag"
                            } else {
                                tag
                            }
                            searchTargetSource = source
                            currentScreen = Screen.SEARCH
                        },
                        onAuthorClick = { author, source ->
                            selectedWork = null
                            selectedCreator = Pair(author, source)
                        }
                    )
                }
            }

            // 画师主页浮层 (Creator Profile)
            AnimatedVisibility(
                visible = selectedCreator != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                selectedCreator?.let { (author, source) ->
                    CreatorView(
                        author = author,
                        source = source,
                        isCompact = isCompactScreen,
                        onDismiss = { selectedCreator = null },
                        onSelectWork = { selectedWork = it }
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
    initialSearchQuery: String?,
    initialSearchSource: Source?,
    onSelect: (WorkCard) -> Unit,
    onSelectCreator: (String, Source) -> Unit
) {
    when (screen) {
        Screen.BROWSE -> BrowseView(isCompact = isCompact, onSelect = onSelect)
        Screen.SEARCH -> SearchView(
            isCompact = isCompact,
            initialQuery = initialSearchQuery,
            initialSource = initialSearchSource,
            onSelect = onSelect
        )
        Screen.VAULT -> VaultView(isCompact = isCompact, onSelect = onSelect)
        Screen.HISTORY -> HistoryView(isCompact = isCompact, onSelect = onSelect, onAuthorSelect = onSelectCreator)
        Screen.SETTINGS -> SettingsView(isCompact = isCompact)
        Screen.QUEUE -> QueueView(onBack = { })
    }
}
