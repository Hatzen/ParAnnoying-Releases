package de.hartz.software.parannoying.core.interfaces.di.security

interface CompressionHelper {

    fun compress(data: ByteArray): String

    fun decompress(data: String): ByteArray
}