package de.hartz.software.parannoying.core.interfaces.di.security

import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey

interface AsymmetricEncryptionHelper {

    val KEY_SIZE_BITS: Int

    fun privateKeyToString(key: PrivateKey): String
    fun publicKeyToString(key: PublicKey): String

    fun getKeyPair(randomSizePassword: String? = null): KeyPair


    @Deprecated("Dont use this is insecure.")
    fun decrypt(text: String): String?
    @Deprecated("Dont use this is insecure.")
    fun encrypt(text: String): String?

    fun decrypt(text: String, key: PrivateKey): String?
    fun encrypt(text: String, key: PublicKey): String?
}
