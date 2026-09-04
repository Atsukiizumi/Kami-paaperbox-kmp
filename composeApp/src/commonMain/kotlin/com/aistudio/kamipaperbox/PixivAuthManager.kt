package com.aistudio.kamipaperbox

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.header
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PixivAuthResponse(
    val access_token: String? = null,
    val refresh_token: String? = null,
    val expires_in: Int? = null,
    val has_error: Boolean = false,
    val errors: PixivAuthErrors? = null
)

@Serializable
data class PixivAuthErrors(
    val system: PixivAuthErrorSystem? = null
)

@Serializable
data class PixivAuthErrorSystem(
    val message: String? = null
)

object PixivAuthManager {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val mutex = Mutex()
    private const val CLIENT_ID = "MOBrBDS8blbauoSck0ZfDbtuzpyT"
    private const val CLIENT_SECRET = "ls6kPT0b4syOtzYEBXhXFp9TuZFAQ4PbPTD4tzT85A1zE4"

    suspend fun getValidAccessToken(): String? {
    suspend fun loginWithCode(code: String, codeVerifier: String): Boolean = mutex.withLock {
        try {
            val response: PixivAuthResponse = httpClient.submitForm(
                url = "https://oauth.secure.pixiv.net/auth/token",
                formParameters = Parameters.build {
                    append("client_id", CLIENT_ID)
                    append("client_secret", CLIENT_SECRET)
                    append("grant_type", "authorization_code")
                    append("code", code)
                    append("code_verifier", codeVerifier)
                    append("redirect_uri", "https://app-api.pixiv.net/web/v1/users/auth/pixiv/callback")
                    append("include_policy", "true")
                }
            ) {
                header("User-Agent", "PixivAndroidApp/5.0.234 (Android 11; Pixel 5)")
            }.body()

            if (response.has_error || response.access_token == null) {
                println("Pixiv Auth Error: ${response.errors?.system?.message}")
                return false
            }

            SettingsManager.setPixivTokens(
                accessToken = response.access_token,
                refreshToken = response.refresh_token ?: ""
            )
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
        val prefs = SettingsManager.prefs.value
        val accessToken = prefs.pixivAccessToken
        val refreshToken = prefs.pixivRefreshToken

        if (refreshToken.isBlank()) return null

        if (accessToken.isNotBlank()) {
            return accessToken
        }

        return refreshAccessToken(refreshToken)
    }

    suspend fun refreshAccessToken(refreshToken: String): String? = mutex.withLock {
        val currentPrefs = SettingsManager.prefs.value
        if (currentPrefs.pixivAccessToken.isNotBlank() && currentPrefs.pixivRefreshToken == refreshToken) {
            return currentPrefs.pixivAccessToken
        }

        try {
            val response: PixivAuthResponse = httpClient.submitForm(
                url = "https://oauth.secure.pixiv.net/auth/token",
                formParameters = Parameters.build {
                    append("client_id", CLIENT_ID)
                    append("client_secret", CLIENT_SECRET)
                    append("grant_type", "refresh_token")
                    append("refresh_token", refreshToken)
                }
            ) {
                header("User-Agent", "PixivAndroidApp/5.0.234 (Android 11; Pixel 5)")
            }.body()

            if (response.has_error || response.access_token == null) {
                println("Pixiv Auth Error: ${response.errors?.system?.message}")
                return null
            }

            SettingsManager.setPixivTokens(
                accessToken = response.access_token,
                refreshToken = response.refresh_token ?: refreshToken
            )
            return response.access_token
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
