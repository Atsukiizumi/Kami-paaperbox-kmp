package com.aistudio.kamipaperbox
import io.ktor.client.HttpClient
expect fun createHttpClient(proxyUrl: String?): HttpClient
