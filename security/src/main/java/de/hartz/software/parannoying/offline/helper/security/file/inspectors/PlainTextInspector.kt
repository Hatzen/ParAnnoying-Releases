package de.hartz.software.parannoying.offline.helper.security.file.inspectors

import java.io.File

object PlainTextInspector : FileInspector {
    override fun supports(file: File): Boolean {
        val ext = file.extension.lowercase()
        return ext in listOf("txt", "log", "csv", "json", "xml")
    }

    override fun analyze(file: File, issues: MutableList<String>): Int {
        return try {
            val content = file.bufferedReader().use { it.readText().take(10000) }

            if (content.contains("<script") || content.contains("<?php")) {
                issues.add("Text enthält eingebetteten Code (Script oder PHP)")
                return 70
            }

            0
        } catch (e: Exception) {
            issues.add("Textdatei unlesbar")
            40
        }
    }
}