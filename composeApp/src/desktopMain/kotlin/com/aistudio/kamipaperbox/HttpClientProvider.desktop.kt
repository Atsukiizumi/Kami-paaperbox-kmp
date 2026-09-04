package com.aistudio.kamipaperbox
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.ProxyBuilder
import io.ktor.http.Url

actual fun createHttpClient(proxyUrl: String?): HttpClient {
    return HttpClient(CIO) {
        if (!proxyUrl.isNullOrBlank()) {
            engine {
                proxy = ProxyBuilder.http(Url(proxyUrl))
            }
        }
    }
}
