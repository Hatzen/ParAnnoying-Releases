package de.hartz.software.parannoying.offline.helper.security.file.inspectors

import java.io.File

object OfficeFileInspector : FileInspector {
    override fun supports(file: File): Boolean {
        val ext = file.extension.lowercase()
        return ext in listOf("docx", "xlsx", "pptx")
    }

    override fun analyze(file: File, issues: MutableList<String>): Int {
        return try {
            val zipFile = java.util.zip.ZipFile(file)
            val entries = zipFile.entries()
            var hasMacros = false

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement().name
                if (entry.contains("vbaProject.bin")) {
                    hasMacros = true
                    break
                }
            }

            if (hasMacros) {
                issues.add("Office-Dokument enthält Makros (vbaProject.bin)")
                80
            } else 0
        } catch (e: Exception) {
            issues.add("Office-Datei beschädigt oder nicht lesbar")
            50
        }
    }
}