package com.aistudio.kamipaperbox

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AppPreferences(
    val includeR18: Boolean = false,
    val blurNsfw: Boolean = true,
    val aiFilterMode: AiFilterMode = AiFilterMode.BADGE_ONLY,
    val usePixivMirror: Boolean = true,
    val themePreset: ThemePreset = ThemePreset.WASHI,
    val searchHistory: List<String> = listOf("landscape", "original", "1girl", "genshin_impact", "cyberpunk"),
    val vaultPath: String = "",
    val gridColumns: Int = 2,
    val pixivAccessToken: String = "",
    val pixivRefreshToken: String = "",
    val sauceNaoApiKey: String = ""
)

object SettingsManager {
    private val _prefs = MutableStateFlow(AppPreferences())
    val prefs = _prefs.asStateFlow()

    val vaultPath = MutableStateFlow("") // Expose directly for simplicity, or sync with prefs

    fun setVaultPath(path: String) {
        vaultPath.value = path
        _prefs.update { it.copy(vaultPath = path) }
    }

    fun setGridColumns(columns: Int) {
        _prefs.update { it.copy(gridColumns = columns) }
    }

    fun setPixivTokens(accessToken: String, refreshToken: String) {
        _prefs.update { it.copy(pixivAccessToken = accessToken, pixivRefreshToken = refreshToken) }
    }

    fun setSauceNaoApiKey(key: String) {
        _prefs.update { it.copy(sauceNaoApiKey = key) }
    }

    fun setIncludeR18(value: Boolean) {
        _prefs.update { it.copy(includeR18 = value) }
    }

    fun setBlurNsfw(value: Boolean) {
        _prefs.update { it.copy(blurNsfw = value) }
    }

    fun setAiFilterMode(mode: AiFilterMode) {
        _prefs.update { it.copy(aiFilterMode = mode) }
    }

    fun setUsePixivMirror(value: Boolean) {
        _prefs.update { it.copy(usePixivMirror = value) }
    }

    fun setThemePreset(preset: ThemePreset) {
        _prefs.update { it.copy(themePreset = preset) }
    }

    fun addSearchHistory(query: String) {
        if (query.isBlank()) return
        _prefs.update { current ->
            val updated = listOf(query) + current.searchHistory.filter { it != query }
            current.copy(searchHistory = updated.take(15))
        }
    }

    fun clearSearchHistory() {
        _prefs.update { it.copy(searchHistory = emptyList()) }
    }
}
