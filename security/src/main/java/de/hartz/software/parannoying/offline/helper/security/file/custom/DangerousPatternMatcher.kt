package de.hartz.software.parannoying.offline.helper.security.file.custom

import java.io.File

object DangerousPatternMatcher {
    private val dangerousPatterns = listOf(
        Regex("""\b(base64|eval|dex|payload|inject|exec|powershell|cmd)\b""", RegexOption.IGNORE_CASE),
        Regex("""[<>|&;`$]"""), // Shell-Metazeichen
        Regex("""\x00{5,}"""),  // Viele Nullbytes
        Regex("""\u202e""")     // RTL Override-Trick
    )

    fun analyze(file: File, issues: MutableList<String>): Int {
        val content = try {
            file.bufferedReader().use { it.readText().take(5000) } // nur erste 5kB prüfen
        } catch (e: Exception) {
            return 0 // Wenn Datei binär oder unlesbar
        }

        var score = 0
        for (pattern in dangerousPatterns) {
            if (pattern.containsMatchIn(content)) {
                issues.add("Verdächtige Zeichenkette erkannt: ${pattern.pattern}")
                score += 20
            }
        }

        return score.coerceAtMost(80)
    }
}