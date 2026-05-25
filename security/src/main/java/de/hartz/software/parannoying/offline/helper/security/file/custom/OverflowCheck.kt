package de.hartz.software.parannoying.offline.helper.security.file.custom

import java.io.File

object OverflowCheck {
    fun analyze(file: File, issues: MutableList<String>): Int {
        val maxSafeSize = 512 * 1024 * 1024 // 512 MB
        if (file.length() > maxSafeSize) {
            issues.add("Datei ist ungewöhnlich groß (> 512 MB)")
            return 70
        }

        val suspiciousHeaderBytes = 16
        val header = ByteArray(suspiciousHeaderBytes)
        file.inputStream().use { it.read(header) }

        if (header.all { it == 0.toByte() }) {
            issues.add("Header besteht nur aus Nullbytes – potentiell manipuliert")
            return 60
        }

        return 0
    }
}