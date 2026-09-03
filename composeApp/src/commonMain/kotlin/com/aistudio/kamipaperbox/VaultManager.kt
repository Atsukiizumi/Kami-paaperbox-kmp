package com.aistudio.kamipaperbox

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 跨平台画匣 (Vault) 与浏览历史 (History) 状态管理器
 */
object VaultManager {
    private val _vaultItems = MutableStateFlow<List<VaultItem>>(emptyList())
    val vaultItems: StateFlow<List<VaultItem>> = _vaultItems.asStateFlow()

    private val _historyItems = MutableStateFlow<List<HistoryItem>>(emptyList())
    val historyItems: StateFlow<List<HistoryItem>> = _historyItems.asStateFlow()

    fun isItemInVault(key: String): Boolean {
        return _vaultItems.value.any { it.key == key }
    }

    fun toggleVault(work: WorkCard) {
        val key = "${work.source}_${work.id}"
        val current = _vaultItems.value.toMutableList()
        val index = current.indexOfFirst { it.key == key }
        if (index >= 0) {
            current.removeAt(index)
        } else {
            current.add(
                0,
                VaultItem(
                    key = key,
                    source = work.source,
                    id = work.id,
                    title = work.title,
                    author = work.author,
                    thumb = work.thumb,
                    originalUrl = work.originalUrl,
                    savedAt = 1725321600000L,
                    tags = work.tags
                )
            )
        }
        _vaultItems.value = current
    }

    fun recordHistory(work: WorkCard) {
        val key = "${work.source}_${work.id}"
        val current = _historyItems.value.filterNot { it.key == key }.toMutableList()
        current.add(
            0,
            HistoryItem(
                key = key,
                source = work.source,
                id = work.id,
                title = work.title,
                thumb = work.thumb,
                originalUrl = work.originalUrl,
                viewedAt = 1725321600000L
            )
        )
        _historyItems.value = current.take(100)
    }

    fun clearHistory() {
        _historyItems.value = emptyList()
    }
}
