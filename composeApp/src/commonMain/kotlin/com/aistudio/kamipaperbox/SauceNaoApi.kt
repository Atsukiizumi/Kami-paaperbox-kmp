package com.aistudio.kamipaperbox

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SauceNaoResponse(
    val header: SauceNaoHeader? = null,
    val results: List<SauceNaoResult> = emptyList()
)

@Serializable
data class SauceNaoHeader(
    val status: Int,
    val message: String? = null
)

@Serializable
data class SauceNaoResult(
    val header: SauceNaoResultHeader? = null,
    val data: SauceNaoResultData? = null
)

@Serializable
data class SauceNaoResultHeader(
    val similarity: String,
    val thumbnail: String,
    val index_id: Int,
    val index_name: String
)

@Serializable
data class SauceNaoResultData(
    val ext_urls: List<String>? = null,
    val title: String? = null,
    val pixiv_id: Int? = null,
    val member_name: String? = null,
    val member_id: Int? = null,
    val creator: String? = null,
    val source: String? = null
)

object SauceNaoClient {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun searchByUrl(url: String, apiKey: String = ""): List<SauceNaoResult> {
        return try {
            val response: SauceNaoResponse = httpClient.get("https://saucenao.com/search.php") {
                parameter("url", url)
                parameter("output_type", 2)
                parameter("numres", 6)
                if (apiKey.isNotBlank()) {
                    parameter("api_key", apiKey)
                }
            }.body()
            response.results.sortedByDescending { it.header?.similarity?.toDoubleOrNull() ?: 0.0 }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
