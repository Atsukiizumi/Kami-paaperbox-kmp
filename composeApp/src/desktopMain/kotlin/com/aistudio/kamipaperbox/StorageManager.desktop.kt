package com.aistudio.kamipaperbox

import okio.FileSystem

actual val fileSystem: FileSystem = FileSystem.SYSTEM

actual fun getDefaultStorageDirectory(): String {
    return System.getProperty("user.home") + "/Pictures/KamiPaperbox"
}
