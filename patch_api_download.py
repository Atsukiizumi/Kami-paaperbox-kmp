import re

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/ApiClient.kt", "r") as f:
    text = f.read()

download_func = """    suspend fun downloadBytes(url: String): ByteArray? {
        return try {
            val response: io.ktor.client.statement.HttpResponse = httpClient.get(url) {
                val accessToken = PixivAuthManager.getValidAccessToken()
                if (accessToken != null && url.contains("pixiv.net")) {
                    header("Authorization", "Bearer $accessToken")
                }
                if (url.contains("pixiv.net") || url.contains("pximg.net")) {
                    header("User-Agent", "PixivAndroidApp/5.0.234 (Android 11; Pixel 5)")
                    header("Referer", "https://app-api.pixiv.net/")
                }
            }
            response.io.ktor.client.call.body<ByteArray>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
"""

text = text.replace("    suspend fun fetchPixivUgoiraMetadata", download_func + "\n    suspend fun fetchPixivUgoiraMetadata")

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/ApiClient.kt", "w") as f:
    f.write(text)

