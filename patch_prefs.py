import re

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/SettingsManager.kt", "r") as f:
    text = f.read()

text = text.replace('val sauceNaoApiKey: String = ""\n)', 'val sauceNaoApiKey: String = "",\n    val proxyUrl: String = ""\n)')

proxy_setter = """    fun setSauceNaoApiKey(key: String) {
        _prefs.update { it.copy(sauceNaoApiKey = key) }
    }

    fun setProxyUrl(url: String) {
        _prefs.update { it.copy(proxyUrl = url) }
        GalleryRepository.updateProxy(url)
    }
"""

text = text.replace('    fun setSauceNaoApiKey(key: String) {\n        _prefs.update { it.copy(sauceNaoApiKey = key) }\n    }', proxy_setter)

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/SettingsManager.kt", "w") as f:
    f.write(text)

