package de.hartz.software.parannoying.offline.helper.security.file.custom

import java.io.File

object FileRiskScanner {

    data class Result(
        val score: Int,              // 0–100
        val issues: List<String>,    // Liste erkannter Warnungen
        val analyzedBytes: Long      // Anzahl untersuchter Bytes
    )

    fun scan(file: File): Result {
        val issues = mutableListOf<String>()
        val entropyScore = EntropyAnalyzer.analyze(file, issues)
        val patternScore = DangerousPatternMatcher.analyze(file, issues)
        val overflowScore = OverflowCheck.analyze(file, issues)

        val totalScore = listOf(entropyScore, patternScore, overflowScore).maxOrNull() ?: 0
        return Result(
            score = totalScore.coerceIn(0, 100),
            issues = issues.distinct(),
            analyzedBytes = file.length()
        )
    }
}