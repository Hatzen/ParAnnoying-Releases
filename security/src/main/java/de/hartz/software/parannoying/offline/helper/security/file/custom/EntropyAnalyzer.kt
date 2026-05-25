package de.hartz.software.parannoying.offline.helper.security.file.custom

import java.io.File

object EntropyAnalyzer {
    fun analyze(file: File, issues: MutableList<String>): Int {
        val bufferSize = 1024 * 1024 // 1 MB
        val byteFreq = IntArray(256)
        var totalBytes = 0L

        file.inputStream().use { stream ->
            val buffer = ByteArray(bufferSize)
            var read: Int

            while (stream.read(buffer).also { read = it } != -1) {
                for (i in 0 until read) {
                    byteFreq[buffer[i].toInt() and 0xFF]++
                }
                totalBytes += read
                if (totalBytes > 5 * bufferSize) break // Analyse auf 5 MB begrenzen
            }
        }

        val entropy = byteFreq.filter { it > 0 }
            .map { freq ->
                val p = freq.toDouble() / totalBytes
                -p * kotlin.math.ln(p) / kotlin.math.ln(2.0)
            }.sum()

        return when {
            entropy >= 7.5 -> {
                issues.add("Hohe Entropie (möglicherweise verschlüsselt oder gepackt)")
                80
            }
            entropy in 6.0..7.5 -> {
                issues.add("Ungewöhnliche Entropie – ggf. binär oder gepackt")
                40
            }
            else -> 0
        }
    }
}