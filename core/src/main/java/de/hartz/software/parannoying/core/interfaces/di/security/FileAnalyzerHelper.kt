package de.hartz.software.parannoying.core.interfaces.di.security

import android.content.Context
import java.io.File

interface FileAnalyzerHelper {
    fun scan(file: File, context: Context): FileAnalyzeResult
}