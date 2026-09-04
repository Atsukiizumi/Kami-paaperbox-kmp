package com.aistudio.kamipaperbox

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun SearchView(
    isCompact: Boolean,
    initialQuery: String? = null,
    initialSource: Source? = null,
    onSelect: (WorkCard) -> Unit
) {
    val prefs by SettingsManager.prefs.collectAsState()
    var query by remember { mutableStateOf(initialQuery ?: "") }
    var results by remember { mutableStateOf<List<WorkCard>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var selectedSource by remember { mutableStateOf(initialSource ?: Source.DANBOORU) }
    var currentPage by remember { mutableStateOf(1) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Keyword, 1: Reverse Image Search
    var reverseSearchUrl by remember { mutableStateOf("") }
    var sauceNaoResults by remember { mutableStateOf<List<SauceNaoResult>>(emptyList()) }
    var isReverseSearching by remember { mutableStateOf(false) }

    suspend fun performReverseSearch() {
        if (reverseSearchUrl.isBlank() || isReverseSearching) return
        isReverseSearching = true
        sauceNaoResults = emptyList()
        sauceNaoResults = SauceNaoClient.searchByUrl(reverseSearchUrl, prefs.sauceNaoApiKey)
        isReverseSearching = false
    }

    val scope = rememberCoroutineScope()
    
    suspend fun performSearch(newSearch: Boolean = true) {
        if (isSearching) return
        if (newSearch) {
            currentPage = 1
            results = emptyList()
            if (query.isNotBlank()) {
                SettingsManager.addSearchHistory(query.trim())
            }
        }
        isSearching = true
        val nextPage = if (newSearch) 1 else currentPage + 1
        val newResults = GalleryRepository.fetchPosts(
            source = selectedSource,
            query = query,
            page = nextPage,
            limit = 40,
            includeR18 = prefs.includeR18,
            aiFilterMode = prefs.aiFilterMode,
            usePixivMirror = prefs.usePixivMirror
        )
        if (newResults.isNotEmpty()) {
            results = if (newSearch) newResults else results + newResults
            currentPage = nextPage
        }
        isSearching = false
    }

    // 当外部传入新关键词或源时 (如在灯箱中点击标签或作者)，自动应用并触发搜索
    LaunchedEffect(initialQuery, initialSource) {
        if (!initialQuery.isNullOrBlank()) {
            query = initialQuery
            if (initialSource != null) {
                selectedSource = initialSource
            }
            performSearch(newSearch = true)
        }
    }

    val hotTags = remember(selectedSource) {
        when (selectedSource) {
            Source.FANBOX -> listOf("mignon", "c-row", "morikuraen", "kantoku", "fuzichoco", "torino")
            Source.PIXIV -> listOf("オリジナル", "風景", "女の子", "ホロライブ", "原神", "初音ミク", "Fate")
            else -> listOf("1girl", "landscape", "original", "genshin_impact", "cyberpunk", "hololive", "cat_ears")
        }
    }

    val activeTokens = remember(query) {
        query.trim().split(" ").filter { it.isNotBlank() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = if (isCompact) 12.dp else 20.dp)
    ) {
        // 顶栏标题
        Column(modifier = Modifier.padding(top = if (isCompact) 10.dp else 16.dp, bottom = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "图谱检索",
                    style = if (isCompact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
            
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                containerColor = androidx.compose.ui.graphics.Color.Transparent
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("关键词检索", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("以图搜图 (SauceNAO)", fontWeight = FontWeight.Bold) }
                )
            }
        }
            
        if (selectedTab == 0) {
            // 搜索输入框
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            if (selectedSource == Source.FANBOX) "输入创作者 ID/名称..." else "多标签空格分割，如 1girl landscape..."
                        )
                    },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "清除")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    scope.launch { performSearch() }
                })
            )
        }

        // 当前激活标签的分解胶囊 (点击单独移除标签)
        if (activeTokens.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                activeTokens.forEach { token ->
                    InputChip(
                        selected = true,
                        onClick = {
                            val newQuery = activeTokens.filter { it != token }.joinToString(" ")
                            query = newQuery
                            scope.launch { performSearch() }
                        },
                        label = { Text(token, fontSize = 11.sp) },
                        trailingIcon = {
                            Icon(Icons.Default.Close, contentDescription = "移除", modifier = Modifier.size(12.dp))
                        }
                    )
                }
            }
        }

        // 源选择与搜索按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
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
                        onClick = {
                            selectedSource = src
                            scope.launch { performSearch() }
                        },
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

        // 推荐热搜标签胶囊栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "热搜:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            hotTags.forEach { tag ->
                SuggestionChip(
                    onClick = {
                        query = if (query.isBlank()) tag else "$query $tag"
                        scope.launch { performSearch() }
                    },
                    label = { Text(tag, fontSize = 11.sp) }
                )
            }
        }

        // 历史搜索关键词 (如果存在)
        if (prefs.searchHistory.isNotEmpty() && results.isEmpty() && !isSearching) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("检索历史", style = MaterialTheme.typography.labelMedium)
                }
                TextButton(onClick = { SettingsManager.clearSearchHistory() }) {
                    Text("清除", fontSize = 11.sp)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                prefs.searchHistory.forEach { hist ->
                    AssistChip(
                        onClick = {
                            query = hist
                            scope.launch { performSearch() }
                        },
                        label = { Text(hist, fontSize = 11.sp) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // 搜索结果展示
        if (isSearching && results.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (!isSearching && results.isEmpty() && query.isNotBlank()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "未查阅到符合「$query」的作品，请尝试更换关键词或图源",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
            }
        } else {
            val gridState = rememberLazyStaggeredGridState()

            val shouldLoadMore = remember {
                derivedStateOf {
                    val lastVisibleItemIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    lastVisibleItemIndex >= results.size - 8 && results.isNotEmpty() && !isSearching
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
                items(results, key = { "${it.source}_${it.id}_${it.thumb}" }) { work ->
                    ArtworkCard(
                        work = work,
                        isCompact = isCompact,
                        onClick = { onSelect(work) }
                    )
                }

                if (isSearching && results.isNotEmpty()) {
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
        } // closes if (selectedTab == 0)
        
        if (selectedTab == 1) {
            // SauceNAO UI
                OutlinedTextField(
                    value = reverseSearchUrl,
                    onValueChange = { reverseSearchUrl = it },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    placeholder = { Text("输入图片 URL (支持 Pixiv, Danbooru 等外链)...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (reverseSearchUrl.isNotEmpty()) {
                            IconButton(onClick = { reverseSearchUrl = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "清除")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { scope.launch { performReverseSearch() } })
                )
                Button(
                    onClick = { scope.launch { performReverseSearch() } },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Text("以图搜图 (SauceNAO)")
                }

                if (isReverseSearching) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (sauceNaoResults.isEmpty() && reverseSearchUrl.isNotBlank()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("未找到相似图片", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        lazyItems(sauceNaoResults) { result ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.padding(12.dp)) {
                                    coil3.compose.AsyncImage(
                                        model = result.header?.thumbnail,
                                        contentDescription = "Thumbnail",
                                        modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text("相似度: ${result.header?.similarity}%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Spacer(Modifier.height(4.dp))
                                        Text(result.data?.title ?: result.data?.source ?: "未知来源", style = MaterialTheme.typography.bodyMedium)
                                        Text("画师: ${result.data?.member_name ?: result.data?.creator ?: "未知"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        if (result.data?.pixiv_id != null) {
                                            Text("Pixiv ID: ${result.data.pixiv_id}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        if (!result.data?.ext_urls.isNullOrEmpty()) {
                                            Spacer(Modifier.height(8.dp))
                                            OutlinedButton(onClick = { /* Could open URL */ }, modifier = Modifier.height(32.dp)) {
                                                Text(result.data!!.ext_urls!!.first(), fontSize = 10.sp, maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }
