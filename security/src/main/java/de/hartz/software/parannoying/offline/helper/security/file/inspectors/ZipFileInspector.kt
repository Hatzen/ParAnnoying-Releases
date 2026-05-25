package de.hartz.software.parannoying.offline.helper.security.file.inspectors

import java.io.File

object ZipFileInspector : FileInspector {
    override fun supports(file: File): Boolean =
        file.extension.lowercase() == "zip"

    override fun analyze(file: File, issues: MutableList<String>): Int {
        return try {
            val zipFile = java.util.zip.ZipFile(file)
            val entries = zipFile.entries()
            var score = 0
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (entry.name.contains("..")) {
                    issues.add("ZIP enthält potenziell gefährliche Pfade ('..')")
                    score = 70
                }
                if (entry.size > 50_000_000) {
                    issues.add("ZIP enthält extrem große Datei (>50MB): ${entry.name}")
                    score = 60
                }
            }
            score
        } catch (e: Exception) {
            issues.add("ZIP-Datei beschädigt oder manipuliert: ${e.message}")
            80
        }
    }
}