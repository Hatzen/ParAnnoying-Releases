package de.hartz.software.parannoying.offline.helper.security.file

import android.content.Context
import de.hartz.software.parannoying.core.interfaces.di.security.FileAnalyzeResult
import de.hartz.software.parannoying.core.interfaces.di.security.FileAnalyzerHelper
import de.hartz.software.parannoying.offline.helper.security.file.custom.DangerousPatternMatcher
import de.hartz.software.parannoying.offline.helper.security.file.custom.EntropyAnalyzer
import de.hartz.software.parannoying.offline.helper.security.file.custom.OverflowCheck
import de.hartz.software.parannoying.offline.helper.security.file.inspectors.OfficeFileInspector
import de.hartz.software.parannoying.offline.helper.security.file.inspectors.PdfFileInspector
import de.hartz.software.parannoying.offline.helper.security.file.inspectors.PlainTextInspector
import de.hartz.software.parannoying.offline.helper.security.file.inspectors.ZipFileInspector
import de.hartz.software.parannoying.offline.helper.security.file.pattern.RegexFileScanner
import java.io.File
import javax.inject.Inject

class FileAnalyzerHelperImpl @Inject constructor(): FileAnalyzerHelper {

    private val formatInspectors = listOf(
        ZipFileInspector,
        PdfFileInspector,
        OfficeFileInspector,
        PlainTextInspector
    )

    override fun scan(file: File, context: Context): FileAnalyzeResult {
        val issues = mutableListOf<String>()

        val regexFileScannerScanner = RegexFileScanner().analyze(file, context)

        // Standardprüfungen
        val entropyScore = EntropyAnalyzer.analyze(file, issues)
        val patternScore = DangerousPatternMatcher.analyze(file, issues)
        val overflowScore = OverflowCheck.analyze(file, issues)

        // Formatabhängige Prüfungen
        val formatScore = formatInspectors
            .filter { it.supports(file) }
            .maxOfOrNull { it.analyze(file, issues) } ?: 0

        /*
        val totalScore = listOf(entropyScore, patternScore, overflowScore, formatScore).maxOrNull() ?: 0

        FileRiskScanner.Result(
            score = totalScore.coerceIn(0, 100),
            issues = issues.distinct(),
            analyzedBytes = file.length()
        )
        */

        val total = entropyScore +
            patternScore +
            overflowScore +
            formatScore +
            regexFileScannerScanner

        return FileAnalyzeResult(
            total,
            entropyScore.coerceIn(0, 100),
            patternScore.coerceIn(0, 100),
            overflowScore.coerceIn(0, 100),
            formatScore.coerceIn(0, 100),
            regexFileScannerScanner.coerceIn(0, 100)
        )
    }
}