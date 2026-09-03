package com.aistudio.kamipaperbox

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class BooruPost(
    val id: Long? = null,
    val tags: String? = null,
    val tag_string: String? = null, // Danbooru uses tag_string
    val preview_url: String? = null,
    val file_url: String? = null,
    val large_file_url: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val rating: String? = "s"
)

@Serializable
data class PixivResponse(
    val illustrations: List<PixivIllustration> = emptyList()
)

@Serializable
data class PixivIllustration(
    val id: Long,
    val title: String,
    val user: PixivUser,
    val image_urls: PixivImageUrls,
    val meta_single_page: PixivMetaSinglePage? = null,
    val meta_pages: List<PixivMetaPage>? = null,
    val tags: List<PixivTag> = emptyList(),
    val x_restrict: Int = 0,
    val illust_ai_type: Int = 0
)

@Serializable
data class PixivUser(
    val id: Long,
    val name: String,
    val account: String
)

@Serializable
data class PixivImageUrls(
    val medium: String,
    val large: String? = null,
    val square_medium: String? = null
)

@Serializable
data class PixivMetaSinglePage(
    val original_image_url: String? = null
)

@Serializable
data class PixivMetaPage(
    val image_urls: PixivImageUrls
)

@Serializable
data class PixivTag(
    val name: String
)

@Serializable
data class FanboxResponse(
    val body: List<FanboxPost>? = null // Simplified for illustrative purposes
)

@Serializable
data class FanboxPost(
    val id: String,
    val title: String,
    val user: FanboxUser,
    val coverImageUrl: String? = null,
    val images: List<FanboxImage>? = null,
    val isRestricted: Boolean = false
)

@Serializable
data class FanboxUser(
    val userId: String,
    val name: String
)

@Serializable
data class FanboxImage(
    val originalUrl: String,
    val thumbnailUrl: String
)

object GalleryRepository {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
            })
        }
        defaultRequest {
            header("User-Agent", "KamiPaperbox/1.0 (Android/Desktop Multiplatform App)")
        }
    }

    suspend fun fetchPosts(
        source: Source = Source.DANBOORU,
        page: Int = 1,
        query: String = "",
        limit: Int = 40
    ): List<WorkCard> {
        return try {
            when (source) {
                Source.DANBOORU -> fetchGenericBooru("https://danbooru.donmai.us/posts.json", source, page, query, limit)
                Source.KONACHAN -> fetchGenericBooru("https://konachan.com/post.json", source, page, query, limit)
                Source.YANDE -> fetchGenericBooru("https://yande.re/post.json", source, page, query, limit)
                Source.PIXIV -> fetchPixiv(page, query)
                Source.FANBOX -> fetchFanbox(page, query)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private suspend fun fetchPixiv(page: Int, query: String, includeR18: Boolean = false): List<WorkCard> {
        val url = if (query.isBlank()) {
            "https://app-api.pixiv.net/v1/illust/recommended"
        } else {
            "https://app-api.pixiv.net/v1/search/illust"
        }
        
        val response: PixivResponse = try {
            httpClient.get(url) {
                if (query.isNotBlank()) parameter("word", query)
                parameter("offset", (page - 1) * 30)
                // 远端控制 R18 逻辑
                parameter("include_restrict_safe", if (includeR18) 0 else 1)
                parameter("restrict", if (includeR18) "all" else "safe")
            }.body()
        } catch (e: Exception) {
            PixivResponse()
        }

        return response.illustrations.map { illust ->
            WorkCard(
                source = Source.PIXIV,
                id = illust.id.toString(),
                title = illust.title,
                author = illust.user.name,
                authorId = illust.user.id.toString(),
                thumb = illust.image_urls.medium,
                originalUrl = illust.meta_single_page?.original_image_url ?: illust.image_urls.large ?: illust.image_urls.medium,
                pageCount = illust.meta_pages?.size ?: 1,
                tags = illust.tags.map { it.name },
                rating = if (illust.x_restrict > 0) "e" else "s",
                isAi = illust.illust_ai_type == 2
            )
        }
    }

    private suspend fun fetchFanbox(page: Int, query: String): List<WorkCard> {
        // Fanbox API is complex, this is a simplified simulation
        val url = "https://api.fanbox.cc/post.list"
        
        return try {
            // Simulated response logic
            emptyList() 
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchGenericBooru(
        baseUrl: String,
        source: Source,
        page: Int,
        query: String,
        limit: Int
    ): List<WorkCard> {
        val rawPosts: List<BooruPost> = httpClient.get(baseUrl) {
            parameter("limit", limit)
            parameter("page", page)
            if (query.isNotBlank()) parameter("tags", query)
        }.body()

        return rawPosts.map { post ->
            val tagList = (post.tag_string ?: post.tags ?: "").split(" ").filter { it.isNotBlank() }
            val original = post.file_url ?: post.large_file_url ?: ""
            val thumb = post.preview_url ?: original

            WorkCard(
                source = source,
                id = post.id.toString(),
                title = "#${post.id} ${tagList.take(2).joinToString(" ")}",
                author = tagList.find { it.startsWith("artist:") }?.removePrefix("artist:") ?: "Unknown",
                thumb = thumb,
                originalUrl = original,
                tags = tagList,
                width = post.width,
                height = post.height,
                rating = post.rating ?: "s",
                isAi = tagList.any { it.contains("ai_generated") || it.contains("stable_diffusion") }
            )
        }
    }
}
