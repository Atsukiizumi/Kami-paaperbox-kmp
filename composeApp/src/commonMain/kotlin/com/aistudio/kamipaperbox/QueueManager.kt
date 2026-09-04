package com.aistudio.kamipaperbox

import io.ktor.client.HttpClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import okio.Path.Companion.toPath
import okio.buffer
import okio.use

enum class DownloadState { PENDING, DOWNLOADING, SUCCESS, ERROR }

data class DownloadTask(
    val id: String,
    val work: WorkCard,
    val state: DownloadState = DownloadState.PENDING,
    val progress: Float = 0f, // 0.0 to 1.0
    val localPath: String? = null,
    val errorMessage: String? = null
)

object QueueManager {
    private val httpClient = HttpClient()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _tasks = MutableStateFlow<List<DownloadTask>>(emptyList())
    val tasks = _tasks.asStateFlow()


    private val activeDownloads = mutableSetOf<String>()
    private const val MAX_CONCURRENT_DOWNLOADS = 3

    fun enqueue(work: WorkCard) {
        val taskId = "${work.source.name}_${work.id}"
        if (_tasks.value.any { it.id == taskId }) return // Already in queue

        val newTask = DownloadTask(id = taskId, work = work)
        _tasks.update { it + newTask }
        processQueue()
    }

    private fun processQueue() {
        val pendingTasks = _tasks.value.filter { it.state == DownloadState.PENDING }
        val availableSlots = MAX_CONCURRENT_DOWNLOADS - activeDownloads.size

        if (availableSlots > 0 && pendingTasks.isNotEmpty()) {
            val tasksToStart = pendingTasks.take(availableSlots)
            tasksToStart.forEach { task ->
                activeDownloads.add(task.id)
                updateTask(task.id) { it.copy(state = DownloadState.DOWNLOADING, progress = 0f) }
                startDownload(task)
            }
        }
    }

    private fun startDownload(task: DownloadTask) {
        scope.launch {
            try {
                val urlToDownload = task.work.originalUrl
                val storageDir = StorageManager.getStorageDir()
                
                // Extract filename from URL or use ID
                val fileName = urlToDownload.substringAfterLast("/").substringBefore("?")
                val finalFileName = if (fileName.isNotBlank()) "${task.work.source.name}_${task.work.id}_$fileName" else "${task.work.source.name}_${task.work.id}.jpg"
                val destPath = storageDir / finalFileName

                // Download using Ktor
                val response: HttpResponse = httpClient.get(urlToDownload)
                if (!response.status.isSuccess()) {
                    throw Exception("HTTP Error: ${response.status}")
                }

                val contentLength = response.contentLength() ?: -1L
                val channel: ByteReadChannel = response.bodyAsChannel()
                var bytesCopied = 0L

                val sink = fileSystem.sink(destPath).buffer()
                val buffer = ByteArray(8 * 1024)

                sink.use { s ->
                    while (!channel.isClosedForRead) {
                        val read = channel.readAvailable(buffer, 0, buffer.size)
                        if (read > 0) {
                            s.write(buffer, 0, read)
                            bytesCopied += read
                            if (contentLength > 0) {
                                val progress = (bytesCopied.toFloat() / contentLength.toFloat()).coerceIn(0f, 1f)
                                updateTask(task.id) { it.copy(progress = progress) }
                            }
                        }
                    }
                }

                // Calculate Hash
                val hash = StorageManager.calculateSha256(destPath)

                // Save to Vault Database
                VaultManager.saveToVault(task.work, destPath.toString(), hash)

                updateTask(task.id) { 
                    it.copy(
                        state = DownloadState.SUCCESS, 
                        progress = 1f, 
                        localPath = destPath.toString()
                    ) 
                }
            } catch (e: Exception) {
                e.printStackTrace()
                updateTask(task.id) { 
                    it.copy(
                        state = DownloadState.ERROR, 
                        errorMessage = e.message ?: "Unknown error"
                    ) 
                }
            } finally {
                activeDownloads.remove(task.id)
                processQueue()
            }
        }
    }

    fun removeTask(id: String) {
        _tasks.update { list -> list.filterNot { it.id == id } }
    }

    fun clearCompleted() {
        _tasks.update { list -> list.filterNot { it.state == DownloadState.SUCCESS } }
    }

    private fun updateTask(id: String, transform: (DownloadTask) -> DownloadTask) {
        _tasks.update { list ->
            list.map { if (it.id == id) transform(it) else it }
        }
    }
}
