package com.aistudio.kamipaperbox

import java.util.zip.ZipInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

actual fun extractZip(zipBytes: ByteArray): Map<String, ByteArray> {
    val result = mutableMapOf<String, ByteArray>()
    ZipInputStream(ByteArrayInputStream(zipBytes)).use { zis ->
        var entry = zis.nextEntry
        while (entry != null) {
            if (!entry.isDirectory) {
                val baos = ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                var len: Int
                while (zis.read(buffer).also { len = it } > 0) {
                    baos.write(buffer, 0, len)
                }
                result[entry.name] = baos.toByteArray()
            }
            entry = zis.nextEntry
        }
    }
    return result
}
