package de.hartz.software.parannoying.core.interfaces.di.security

interface HardcodedEncryptionHelper {
    fun decrypt(text: String): String

    // TODO: Is salted and is different for same input, maybe provide deterministic output.
    fun encrypt(text: String): String
}
