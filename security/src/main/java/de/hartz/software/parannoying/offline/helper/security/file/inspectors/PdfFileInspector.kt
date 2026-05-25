package de.hartz.software.parannoying.offline.helper.security.file.inspectors

import java.io.File

object PdfFileInspector : FileInspector {
    override fun supports(file: File): Boolean =
        file.extension.lowercase() == "pdf"

    override fun analyze(file: File, issues: MutableList<String>): Int {
        return try {
            val content = file.bufferedReader().use { it.readText().take(5000) }

            var score = 0
            if (content.contains("/JS") || content.contains("/JavaScript")) {
                issues.add("PDF enthält eingebettetes JavaScript")
                score += 70
            }
            if (content.contains("/Launch") || content.contains("/EmbeddedFile")) {
                issues.add("PDF kann externe Programme starten oder Dateien enthalten")
                score += 60
            }

            score
        } catch (e: Exception) {
            issues.add("PDF-Datei konnte nicht gelesen werden")
            40
        }
    }
}