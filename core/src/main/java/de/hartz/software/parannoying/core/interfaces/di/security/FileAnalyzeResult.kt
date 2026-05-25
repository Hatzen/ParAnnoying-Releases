package de.hartz.software.parannoying.core.interfaces.di.security


data class FileAnalyzeResult(
    val total: Int,
    val entropyScore: Int,
    val patternScore: Int,
    val overflowScore: Int,
    val formatScore: Int,
    val regexFileScannerScanner: Int
)
