package com.aistudio.kamipaperbox

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SafebooruRawPost(
    val id: Long,
    val tags: String = "",
    val image: String = "",
    val directory: String = "",
    val width: Int = 0,
    val height: Int = 0,
    val rating: String = "safe"
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
        source: Source = Source.SAFEBOORU,
        page: Int = 0,
        query: String = "",
        limit: Int = 30
    ): List<WorkCard> {
        return try {
            when (source) {
                Source.SAFEBOORU -> fetchSafebooru(page, query, limit)
                else -> fetchSafebooru(page, query, limit)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private suspend fun fetchSafebooru(page: Int, query: String, limit: Int): List<WorkCard> {
        val url = "https://safebooru.org/index.php?page=dapi&s=post&q=index&json=1"
        val tagsParam = if (query.isNotBlank()) query else null
        
        val rawPosts: List<SafebooruRawPost> = httpClient.get(url) {
            parameter("limit", limit)
            parameter("pid", page)
            if (tagsParam != null) {
                parameter("tags", tagsParam)
            }
        }.body()

        return rawPosts.map { post ->
            val imageUrl = "https://safebooru.org/images/${post.directory}/${post.image}"
            val thumbUrl = "https://safebooru.org/thumbnails/${post.directory}/thumbnail_${post.image}"
            val tagList = post.tags.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
            val isAi = tagList.any { it.contains("ai_generated", ignoreCase = true) || it.contains("stable_diffusion", ignoreCase = true) }
            
            WorkCard(
                source = Source.SAFEBOORU,
                id = post.id.toString(),
                title = "#${post.id} ${tagList.take(3).joinToString(" ")}",
                author = tagList.firstOrNull { it.startsWith("artist:") }?.removePrefix("artist:") ?: "Unknown",
                thumb = thumbUrl,
                originalUrl = imageUrl,
                tags = tagList,
                width = post.width,
                height = post.height,
                rating = post.rating,
                isAi = isAi
            )
        }
    }
}
