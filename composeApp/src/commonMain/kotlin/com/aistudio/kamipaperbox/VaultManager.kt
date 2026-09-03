package com.aistudio.kamipaperbox

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 跨平台画匣 (Vault) 与浏览历史 (History) 状态管理器
 */
object VaultManager {
    private val database = getRoomDatabase(getDatabaseBuilder())
    private val dao = database.galleryDao()
    private val scope = CoroutineScope(Dispatchers.Main)

    val vaultItems: StateFlow<List<VaultItem>> = dao.getVaultItems()
        .map { entities ->
            entities.map { it.toVaultItem() }
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val historyItems: StateFlow<List<HistoryItem>> = dao.getHistoryItems()
        .map { entities ->
            entities.map { it.toHistoryItem() }
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val vaultKeys = vaultItems.map { items -> items.map { it.key }.toSet() }
        .stateIn(scope, SharingStarted.Eagerly, emptySet())

    fun isItemInVault(key: String): Boolean {
        return vaultKeys.value.contains(key)
    }

    fun toggleVault(work: WorkCard) {
        val key = "${work.source}_${work.id}"
        scope.launch(Dispatchers.IO) {
            if (isItemInVault(key)) {
                dao.deleteVaultItem(key)
            } else {
                dao.insertVaultItem(
                    VaultEntity(
                        key = key,
                        source = work.source.name,
                        id = work.id,
                        title = work.title,
                        author = work.author,
                        thumb = work.thumb,
                        originalUrl = work.originalUrl,
                        savedAt = System.currentTimeMillis(),
                        tags = work.tags.joinToString(",")
                    )
                )
            }
        }
    }

    fun recordHistory(work: WorkCard) {
        val key = "${work.source}_${work.id}"
        scope.launch(Dispatchers.IO) {
            dao.insertHistoryItem(
                HistoryEntity(
                    key = key,
                    source = work.source.name,
                    id = work.id,
                    title = work.title,
                    thumb = work.thumb,
                    originalUrl = work.originalUrl,
                    viewedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun clearHistory() {
        scope.launch(Dispatchers.IO) {
            dao.clearHistory()
        }
    }

    private fun VaultEntity.toVaultItem() = VaultItem(
        key = key,
        source = Source.valueOf(source),
        id = id,
        title = title,
        author = author,
        thumb = thumb,
        originalUrl = originalUrl,
        savedAt = savedAt,
        tags = if (tags.isBlank()) emptyList() else tags.split(",")
    )

    private fun HistoryEntity.toHistoryItem() = HistoryItem(
        key = key,
        source = Source.valueOf(source),
        id = id,
        title = title,
        thumb = thumb,
        originalUrl = originalUrl,
        viewedAt = viewedAt
    )
}
