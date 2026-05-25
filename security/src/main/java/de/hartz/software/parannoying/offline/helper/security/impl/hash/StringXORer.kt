package de.hartz.software.parannoying.offline.helper.security.impl.hash

import android.util.Base64
import kotlin.experimental.xor


// https://stackoverflow.com/questions/5126616/xor-operation-with-two-strings-in-java
@Deprecated("Use real hash instead")
internal class StringXORer {

    @Deprecated("Better do not use XOR it generates very weak data.")
    fun encode(s: String, key: String): String {
        return base64Encode(xorWithKey(s.toByteArray(), key.toByteArray()))
    }

    fun decode(s: String, key: String): String {
        throw NotImplementedError()
        return String(xorWithKey(base64Decode(s), key.toByteArray()))
    }

    private fun xorWithKey(a: ByteArray, key: ByteArray): ByteArray {
        val minArray = Math.min(a.size, key.size)

        val out = ByteArray(minArray)
        for (i in 0 until minArray) {
            out[i] = (a[i] xor key[i % key.size]).toByte()
        }
        return out
    }

    private fun base64Decode(s: String): ByteArray {
        try {
            return Base64.decode(s, Base64.DEFAULT)
        } catch (e: IllegalArgumentException) {
            throw RuntimeException(e)
        }

    }

    private fun base64Encode(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.DEFAULT)
        //.replaceAll("\\s", "")
    }
}