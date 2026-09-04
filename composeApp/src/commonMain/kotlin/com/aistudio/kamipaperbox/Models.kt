package com.aistudio.kamipaperbox

import kotlinx.serialization.Serializable

@Serializable
enum class Source(val displayName: String) {
    DANBOORU("Danbooru"),
    KONACHAN("Konachan"),
    YANDE("Yande.re"),
    PIXIV("Pixiv"),
    FANBOX("Fanbox")
}

@Serializable
enum class AiFilterMode(val label: String) {
    SHOW_ALL("显示全部"),
    BADGE_ONLY("仅标注勋章"),
    HIDE_AI("彻底排除 AI")
}

@Serializable
data class WorkCard(
    val source: Source,
    val id: String,
    val title: String,
    val author: String,
    val authorId: String = "",
    val thumb: String,
    val originalUrl: String,
    val pageCount: Int = 1,
    val additionalImages: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val translatedTags: Map<String, String> = emptyMap(),
    val width: Int? = null,
    val height: Int? = null,
    val rating: String = "s", // s: safe, q: questionable, e: explicit
    val isAi: Boolean = false,
    val isRestricted: Boolean = false // Fanbox patron/locked indicator
)

@Serializable
data class VaultItem(
    val key: String, // source_id
    val source: Source,
    val id: String,
    val title: String,
    val author: String,
    val thumb: String,
    val originalUrl: String,
    val savedAt: Long = 0L,
    val tags: List<String> = emptyList(),
    val localFilePath: String? = null,
    val fileHash: String? = null
)

@Serializable
data class HistoryItem(
    val key: String,
    val source: Source,
    val id: String,
    val title: String,
    val thumb: String,
    val originalUrl: String,
    val viewedAt: Long
)

@Serializable
data class CreatorHistoryItem(
    val key: String,
    val source: Source,
    val authorId: String,
    val authorName: String,
    val thumb: String, // could be avatar or a sample work
    val viewedAt: Long
)
