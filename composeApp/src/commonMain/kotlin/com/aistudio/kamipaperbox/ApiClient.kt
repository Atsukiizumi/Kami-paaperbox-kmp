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
    val name: String,
    val translated_name: String? = null
)

@Serializable
data class FanboxCreatorResponse(
    val body: FanboxCreatorBody? = null
)

@Serializable
data class FanboxCreatorBody(
    val items: List<FanboxPostItem> = emptyList(),
    val nextUrl: String? = null
)

@Serializable
data class FanboxPostItem(
    val id: String,
    val title: String,
    val feeRequired: Int? = 0,
    val publishedDatetime: String? = null,
    val coverImageUrl: String? = null,
    val user: FanboxUser? = null,
    val tags: List<String> = emptyList(),
    val isRestricted: Boolean = false,
    val body: FanboxPostBody? = null
)

@Serializable
data class FanboxPostBody(
    val type: String? = null,
    val text: String? = null,
    val images: List<FanboxImageItem>? = null,
    val imageMap: Map<String, FanboxImageItem>? = null
)

@Serializable
data class FanboxImageItem(
    val id: String,
    val originalUrl: String? = null,
    val thumbnailUrl: String? = null
)

@Serializable
data class FanboxUser(
    val userId: String,
    val name: String
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

    private val curatedFanboxCreators = listOf(
        "c-row", "mignon", "morikuraen", "kantoku", "fuzichoco", "torino", "anmi"
    )

    private fun normalizeImageUrl(url: String, useMirror: Boolean): String {
        if (!useMirror || url.isBlank()) return url
        return url
            .replace("https://i.pximg.net/", "https://i.pixiv.re/")
            .replace("http://i.pximg.net/", "https://i.pixiv.re/")
            .replace("https://pixiv.pximg.net/", "https://i.pixiv.re/")
    }

    suspend fun fetchPosts(
        source: Source = Source.DANBOORU,
        page: Int = 1,
        query: String = "",
        limit: Int = 40,
        includeR18: Boolean = SettingsManager.prefs.value.includeR18,
        aiFilterMode: AiFilterMode = SettingsManager.prefs.value.aiFilterMode,
        usePixivMirror: Boolean = SettingsManager.prefs.value.usePixivMirror
    ): List<WorkCard> {
        return try {
            when (source) {
                Source.DANBOORU -> fetchGenericBooru(
                    baseUrl = "https://danbooru.donmai.us/posts.json",
                    source = source,
                    page = page,
                    query = query,
                    limit = limit,
                    includeR18 = includeR18,
                    aiFilterMode = aiFilterMode
                )
                Source.KONACHAN -> fetchGenericBooru(
                    baseUrl = "https://konachan.com/post.json",
                    source = source,
                    page = page,
                    query = query,
                    limit = limit,
                    includeR18 = includeR18,
                    aiFilterMode = aiFilterMode
                )
                Source.YANDE -> fetchGenericBooru(
                    baseUrl = "https://yande.re/post.json",
                    source = source,
                    page = page,
                    query = query,
                    limit = limit,
                    includeR18 = includeR18,
                    aiFilterMode = aiFilterMode
                )
                Source.PIXIV -> fetchPixiv(
                    page = page,
                    query = query,
                    includeR18 = includeR18,
                    useMirror = usePixivMirror
                )
                Source.FANBOX -> fetchFanbox(
                    page = page,
                    query = query,
                    useMirror = usePixivMirror
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private suspend fun fetchPixiv(
        page: Int,
        query: String,
        includeR18: Boolean,
        useMirror: Boolean
    ): List<WorkCard> {
        val trimmedQuery = query.trim()
        val url = if (trimmedQuery.isBlank()) {
            "https://app-api.pixiv.net/v1/illust/recommended"
        } else {
            "https://app-api.pixiv.net/v1/search/illust"
        }
        
        val response: PixivResponse = try {
            httpClient.get(url) {
                if (trimmedQuery.isNotBlank()) {
                    // 支持空格分割多 tag
                    parameter("word", trimmedQuery)
                    parameter("search_target", "partial_match_for_tags")
                    parameter("sort", "date_desc")
                }
                parameter("offset", (page - 1) * 30)
                // 远端控制 R18 过滤逻辑
                parameter("include_restrict_safe", if (includeR18) 0 else 1)
                parameter("restrict", if (includeR18) "all" else "safe")
            }.body()
        } catch (e: Exception) {
            PixivResponse()
        }

        return response.illustrations.map { illust ->
            val thumbUrl = normalizeImageUrl(illust.image_urls.medium, useMirror)
            val originalUrl = normalizeImageUrl(
                illust.meta_single_page?.original_image_url ?: illust.image_urls.large ?: illust.image_urls.medium,
                useMirror
            )

            // 多图 (Manga/图集) 解析
            val allPages = if (illust.meta_pages != null && illust.meta_pages.isNotEmpty()) {
                illust.meta_pages.map { pageItem ->
                    normalizeImageUrl(pageItem.image_urls.large ?: pageItem.image_urls.medium, useMirror)
                }
            } else {
                listOf(originalUrl)
            }

            val tagTranslations = illust.tags
                .filter { !it.translated_name.isNullOrBlank() }
                .associate { it.name to it.translated_name!! }

            WorkCard(
                source = Source.PIXIV,
                id = illust.id.toString(),
                title = illust.title,
                author = illust.user.name,
                authorId = illust.user.id.toString(),
                thumb = thumbUrl,
                originalUrl = originalUrl,
                pageCount = allPages.size,
                additionalImages = allPages,
                tags = illust.tags.map { it.name },
                translatedTags = tagTranslations,
                rating = if (illust.x_restrict > 0) "e" else "s",
                isAi = illust.illust_ai_type == 2
            )
        }
    }

    private suspend fun fetchFanbox(
        page: Int,
        query: String,
        useMirror: Boolean
    ): List<WorkCard> {
        val targetCreator = if (query.isNotBlank()) {
            query.trim().split(" ").firstOrNull() ?: query.trim()
        } else {
            val index = ((page - 1).coerceAtLeast(0)) % curatedFanboxCreators.size
            curatedFanboxCreators[index]
        }

        val url = "https://api.fanbox.cc/post.listCreator"
        
        return try {
            val response: FanboxCreatorResponse = httpClient.get(url) {
                header("Origin", "https://www.fanbox.cc")
                header("Referer", "https://www.fanbox.cc/")
                parameter("creatorId", targetCreator)
                parameter("limit", 20)
            }.body()

            val items = response.body?.items ?: emptyList()
            items.mapNotNull { item ->
                // 提取博文包含的全部图片序列
                val bodyImages = item.body?.images?.mapNotNull { it.originalUrl ?: it.thumbnailUrl }
                    ?: item.body?.imageMap?.values?.mapNotNull { it.originalUrl ?: it.thumbnailUrl }
                    ?: emptyList()

                val allImages = (listOfNotNull(item.coverImageUrl) + bodyImages)
                    .distinct()
                    .map { normalizeImageUrl(it, useMirror) }

                if (allImages.isEmpty()) return@mapNotNull null

                val isLocked = (item.feeRequired ?: 0) > 0 || item.isRestricted
                val postThumb = allImages.first()
                val postOriginal = allImages.first()

                WorkCard(
                    source = Source.FANBOX,
                    id = item.id,
                    title = item.title,
                    author = item.user?.name ?: targetCreator,
                    authorId = item.user?.userId ?: targetCreator,
                    thumb = postThumb,
                    originalUrl = postOriginal,
                    pageCount = allImages.size,
                    additionalImages = allImages,
                    tags = item.tags.ifEmpty { listOf(targetCreator, "fanbox") },
                    rating = "s",
                    isAi = false,
                    isRestricted = isLocked
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private suspend fun fetchGenericBooru(
        baseUrl: String,
        source: Source,
        page: Int,
        query: String,
        limit: Int,
        includeR18: Boolean,
        aiFilterMode: AiFilterMode
    ): List<WorkCard> {
        // 构建支持空格分割的多 tag 查询，并注入 R18/AI 规则
        val tagTokens = query.trim().split(" ").filter { it.isNotBlank() }.toMutableList()

        if (aiFilterMode == AiFilterMode.HIDE_AI && tagTokens.none { it.contains("ai_generated") }) {
            tagTokens.add("-ai_generated")
        }

        if (!includeR18 && tagTokens.none { it.startsWith("rating:") }) {
            val safeRatingTag = if (source == Source.DANBOORU) "rating:g" else "rating:s"
            tagTokens.add(safeRatingTag)
        }

        val finalTagString = tagTokens.joinToString(" ")

        val rawPosts: List<BooruPost> = httpClient.get(baseUrl) {
            parameter("limit", limit)
            parameter("page", page)
            if (finalTagString.isNotBlank()) parameter("tags", finalTagString)
        }.body()

        return rawPosts.mapNotNull { post ->
            val tagList = (post.tag_string ?: post.tags ?: "").split(" ").filter { it.isNotBlank() }
            val original = post.file_url ?: post.large_file_url ?: ""
            val thumb = post.preview_url ?: original

            if (original.isBlank() && thumb.isBlank()) return@mapNotNull null

            val isAiPost = tagList.any { 
                it.contains("ai_generated") || 
                it.contains("stable_diffusion") || 
                it.contains("novelai") || 
                it.contains("midjourney") 
            }

            if (aiFilterMode == AiFilterMode.HIDE_AI && isAiPost) {
                return@mapNotNull null
            }

            val tagTranslations = tagList.associateWith { tag -> 
                TagLexiconManager.getTranslation(tag) ?: ""
            }.filterValues { it.isNotBlank() }

            WorkCard(
                source = source,
                id = (post.id ?: 0L).toString(),
                title = "#${post.id} ${tagList.take(2).joinToString(" ")}",
                author = tagList.find { it.startsWith("artist:") }?.removePrefix("artist:") ?: "Unknown",
                thumb = thumb,
                originalUrl = original,
                tags = tagList,
                translatedTags = tagTranslations,
                width = post.width,
                height = post.height,
                rating = post.rating ?: "s",
                isAi = isAiPost
            )
        }
    }
}
