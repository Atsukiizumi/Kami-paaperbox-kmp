package com.aistudio.kamipaperbox

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Existing models
@Serializable
data class DanbooruPost(
    val id: Int? = null,
    val file_url: String? = null,
    val large_file_url: String? = null,
    val preview_file_url: String? = null,
    val tag_string: String? = null,
    val source: String? = null
)

@Serializable
data class PixivResponse(
    val illusts: List<PixivIllust> = emptyList()
)

@Serializable
data class PixivIllust(
    val id: Int,
    val title: String,
    val image_urls: PixivImageUrls,
    val meta_single_page: PixivMetaSinglePage? = null,
    val meta_pages: List<PixivMetaPage>? = emptyList(),
    val tags: List<PixivTag>? = emptyList()
)

@Serializable
data class PixivImageUrls(
    val square_medium: String? = null,
    val medium: String? = null,
    val large: String? = null
)

@Serializable
data class PixivMetaSinglePage(
    val original_image_url: String? = null
)

@Serializable
data class PixivMetaPage(
    val image_urls: PixivImageUrls? = null
)

@Serializable
data class PixivTag(
    val name: String,
    val translated_name: String? = null
)

// Fanbox models (mock/simplified)
@Serializable
data class FanboxPost(
    val id: String,
    val title: String,
    val coverImageUrl: String? = null,
    val body: FanboxBody? = null
)

@Serializable
data class FanboxBody(
    val images: List<FanboxImage>? = emptyList()
)

@Serializable
data class FanboxImage(
    val originalUrl: String,
    val thumbnailUrl: String
)

@Serializable
data class FanboxResponse(
    val body: FanboxResponseBody
)

@Serializable
data class FanboxResponseBody(
    val items: List<FanboxPost>
)

object GalleryRepository {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun fetchPosts(
        source: Source,
        query: String = "",
        page: Int = 1,
        limit: Int = 40,
        includeR18: Boolean = false,
        aiFilterMode: AiFilterMode = AiFilterMode.SHOW_ALL,
        usePixivMirror: Boolean = false
    ): List<WorkCard> {
        return try {
            when (source) {
                Source.DANBOORU -> fetchDanbooru(query, page, limit, includeR18, aiFilterMode)
                Source.PIXIV -> fetchPixiv(query, page, limit, includeR18, aiFilterMode, usePixivMirror)
                Source.FANBOX -> fetchFanbox(query, page, limit)
                Source.KONACHAN -> emptyList() // Not implemented yet
                Source.YANDE -> emptyList() // Not implemented yet
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private suspend fun fetchDanbooru(
        query: String,
        page: Int,
        limit: Int,
        includeR18: Boolean,
        aiFilterMode: AiFilterMode
    ): List<WorkCard> {
        val trimmedQuery = query.trim()
        val tagsList = mutableListOf<String>()

        if (trimmedQuery.isNotBlank()) {
            tagsList.addAll(trimmedQuery.split(" ").take(2))
        }

        if (!includeR18) {
            tagsList.add("rating:general")
        }

        when (aiFilterMode) {
            AiFilterMode.HIDE_AI -> tagsList.add("-ai_generated")
            else -> {}
        }

        val tagsQuery = tagsList.joinToString(" ")

        val response: List<DanbooruPost> = httpClient.get("https://danbooru.donmai.us/posts.json") {
            if (tagsQuery.isNotBlank()) {
                parameter("tags", tagsQuery)
            }
            parameter("page", page)
            parameter("limit", limit)
        }.body()

        return response.filter { it.file_url != null || it.large_file_url != null }.map { post ->
            val preview = post.preview_file_url ?: post.large_file_url ?: post.file_url ?: ""
            val original = post.large_file_url ?: post.file_url ?: preview
            WorkCard(
                id = post.id?.toString() ?: "",
                thumb = preview,
                originalUrl = original,
                title = "Danbooru #${post.id}",
                author = post.source ?: "Unknown",
                source = Source.DANBOORU
            )
        }
    }

    private suspend fun fetchPixiv(
        query: String,
        page: Int,
        limit: Int,
        includeR18: Boolean,
        aiFilterMode: AiFilterMode,
        usePixivMirror: Boolean
    ): List<WorkCard> {
        val trimmedQuery = query.trim()
        val offset = (page - 1) * 30
        
        val url = if (usePixivMirror) {
            "https://api.obfs.dev/api/pixiv/search" // Example mirror
        } else {
            "https://app-api.pixiv.net/v1/search/illust"
        }
        
        val accessToken = PixivAuthManager.getValidAccessToken()
        
        val response: PixivResponse = try {
            httpClient.get(url) {
                if (accessToken != null) {
                    header("Authorization", "Bearer $accessToken")
                }
                header("App-OS", "ios")
                header("App-OS-Version", "14.6")
                header("App-Version", "7.6.2")
                header("User-Agent", "PixivIOSApp/7.6.2 (iOS 14.6; iPhone13,2)")
                
                if (trimmedQuery.isNotBlank()) {
                    // 支持空格分割多 tag
                    parameter("word", trimmedQuery)
                } else {
                    parameter("word", "オリジナル")
                }
                parameter("search_target", "partial_match_for_tags")
                parameter("sort", "date_desc")
                parameter("offset", offset)
                parameter("filter", "for_ios")
            }.body()
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }

        return response.illusts.mapNotNull { illust ->
            // Filter AI
            val isAi = illust.tags?.any { it.name.contains("AI") || it.name.contains("novelai") } == true
            if (aiFilterMode == AiFilterMode.HIDE_AI && isAi) return@mapNotNull null

            val preview = illust.image_urls.medium ?: illust.image_urls.square_medium ?: return@mapNotNull null
            val original = illust.meta_single_page?.original_image_url 
                ?: illust.meta_pages?.firstOrNull()?.image_urls?.large
                ?: illust.image_urls.large 
                ?: preview

            val finalPreview = if (usePixivMirror && !preview.contains("obfs.dev")) {
                preview.replace("i.pximg.net", "i.pixiv.re")
            } else preview
            
            val finalOriginal = if (usePixivMirror && !original.contains("obfs.dev")) {
                original.replace("i.pximg.net", "i.pixiv.re")
            } else original

            WorkCard(
                id = illust.id.toString(),
                thumb = finalPreview,
                originalUrl = finalOriginal,
                title = illust.title,
                author = "Pixiv #${illust.id}",
                source = Source.PIXIV,
                isAi = isAi
            )
        }
    }

    private suspend fun fetchFanbox(
        query: String,
        page: Int,
        limit: Int
    ): List<WorkCard> {
        val creatorId = query.trim().ifBlank { "mignon" } // default for demo
        val response: FanboxResponse = try {
            httpClient.get("https://api.fanbox.cc/post.listCreator") {
                parameter("creatorId", creatorId)
                parameter("limit", limit)
                header("Origin", "https://www.fanbox.cc")
                // In a real implementation, a valid FANBOXSESSID cookie is required here for R18/paid content.
            }.body()
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }

        val cards = mutableListOf<WorkCard>()
        response.body.items.forEach { post ->
            val preview = post.coverImageUrl ?: post.body?.images?.firstOrNull()?.thumbnailUrl
            val original = post.body?.images?.firstOrNull()?.originalUrl ?: preview

            if (preview != null && original != null) {
                cards.add(
                    WorkCard(
                        id = post.id,
                        thumb = preview,
                        originalUrl = original,
                        title = post.title,
                        author = creatorId,
                        source = Source.FANBOX
                    )
                )
            }
        }
        return cards
    }
}
