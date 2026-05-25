package de.hartz.software.parannoying.offline.helper.security.file.inspectors

import java.io.File

interface FileInspector {
    fun supports(file: File): Boolean
    fun analyze(file: File, issues: MutableList<String>): Int
}