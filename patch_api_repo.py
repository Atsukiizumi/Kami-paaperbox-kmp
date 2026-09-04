import re

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/ApiClient.kt", "r") as f:
    text = f.read()

repo_code = """object GalleryRepository {
    var httpClient = createConfiguredHttpClient(SettingsManager.prefs.value.proxyUrl)

    fun updateProxy(proxyUrl: String) {
        httpClient.close()
        httpClient = createConfiguredHttpClient(proxyUrl)
    }

    private fun createConfiguredHttpClient(proxyUrl: String?): HttpClient {
        return createHttpClient(proxyUrl).config {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }
"""

text = re.sub(r'object GalleryRepository \{[\s\n]+private val httpClient = HttpClient \{[\s\n]+install\(ContentNegotiation\) \{[\s\n]+json\(Json \{[\s\n]+ignoreUnknownKeys = true[\s\n]+isLenient = true[\s\n]+\}\)[\s\n]+\}[\s\n]+\}', repo_code, text)

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/ApiClient.kt", "w") as f:
    f.write(text)

