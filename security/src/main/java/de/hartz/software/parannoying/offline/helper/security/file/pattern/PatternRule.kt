package de.hartz.software.parannoying.offline.helper.security.file.pattern

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import java.io.File
import java.util.regex.Pattern

data class PatternRule(
    val id: String,
    val name: String,
    val description: String,
    val pattern: String,
    val severity: Int
)

class RegexFileScanner {
    fun analyze(fileToScan: File, context: Context): Int {
        val rules = loadPatternRules(context)

        val riskScore = scan(fileToScan, rules)
        println("Risk score for file: $riskScore/100")
        return riskScore
    }

    private fun loadPatternRules(context: Context): List<PatternRule> {
        val fileContent = context.assets.open("file_scanner_pattern.json")
            .bufferedReader()
            .use { it.readText() }
        val gson = Gson()
        return gson.fromJson(fileContent, Array<PatternRule>::class.java).toList()
    }

    private fun scan(file: File, rules: List<PatternRule>): Int {
        var riskScore = 0
        var matchedRulesCount = 0

        file.forEachLine { line ->
            for (rule in rules) {
                val pattern = Pattern.compile(rule.pattern)
                if (pattern.matcher(line).find()) {
                    riskScore += rule.severity
                    matchedRulesCount++
                    Log.d(javaClass.simpleName, "Rule matched: ${rule.name} (Severity: ${rule.severity})")
                }
            }
        }

        // Risiko-Wert auf 100 begrenzen
        return if (riskScore > 100) 100 else riskScore
    }
}