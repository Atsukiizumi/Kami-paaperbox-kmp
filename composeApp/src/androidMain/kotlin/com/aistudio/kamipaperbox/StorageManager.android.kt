package com.aistudio.kamipaperbox

import android.os.Environment
import okio.FileSystem

actual val fileSystem: FileSystem = FileSystem.SYSTEM

actual fun getDefaultStorageDirectory(): String {
    val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    return "${picturesDir.absolutePath}/KamiPaperbox"
}
