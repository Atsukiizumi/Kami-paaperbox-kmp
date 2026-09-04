import re

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/ApiClient.kt", "r") as f:
    text = f.read()

ugoira_models = """
@Serializable
data class UgoiraMetadataResponse(
    val ugoira_metadata: UgoiraMetadata? = null
)

@Serializable
data class UgoiraMetadata(
    val zip_urls: PixivImageUrls? = null,
    val frames: List<UgoiraFrame>? = null
)

@Serializable
data class UgoiraFrame(
    val file: String,
    val delay: Int
)
"""

text = text.replace("@Serializable\ndata class PixivIllust(", ugoira_models + "\n@Serializable\ndata class PixivIllust(")

ugoira_fetch = """    suspend fun fetchPixivUgoiraMetadata(illustId: String): UgoiraMetadata? {
        val url = "https://app-api.pixiv.net/v1/ugoira/metadata?illust_id=$illustId"
        val accessToken = PixivAuthManager.getValidAccessToken()
        return try {
            val response: UgoiraMetadataResponse = httpClient.get(url) {
                if (accessToken != null) {
                    header("Authorization", "Bearer $accessToken")
                }
                header("User-Agent", "PixivAndroidApp/5.0.234 (Android 11; Pixel 5)")
            }.body()
            response.ugoira_metadata
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
"""

text = text.replace("    suspend fun fetchPosts(", ugoira_fetch + "\n    suspend fun fetchPosts(")

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/ApiClient.kt", "w") as f:
    f.write(text)

