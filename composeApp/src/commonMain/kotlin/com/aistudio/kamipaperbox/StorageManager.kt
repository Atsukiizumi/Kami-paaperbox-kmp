package com.aistudio.kamipaperbox

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.HashingSink
import okio.Path
import okio.Path.Companion.toPath
import okio.blackholeSink
import okio.buffer
import okio.source
import okio.use

expect val fileSystem: FileSystem

expect fun getDefaultStorageDirectory(): String

object StorageManager {
    fun getStorageDir(): Path {
        val path = SettingsManager.vaultPath.value.ifBlank {
            getDefaultStorageDirectory()
        }
        val dir = path.toPath()
        if (!fileSystem.exists(dir)) {
            fileSystem.createDirectories(dir)
        }
        return dir
    }

    suspend fun calculateSha256(path: Path): String = withContext(Dispatchers.IO) {
        val hashingSink = HashingSink.sha256(blackholeSink())
        fileSystem.source(path).use { source ->
            hashingSink.buffer().use { sink ->
                sink.writeAll(source)
            }
        }
        hashingSink.hash.hex()
    }
}
