package de.hartz.software.parannoying.core.interfaces.di.security

interface SymmetricEncryptionHelper {

    /**
     * size in bytes. So usually 8 times in bits which is used more commonly.
     */
    val KEY_SIZE: Int
    val SEED_SIZE: Int

    fun getKeyFromPassphrase(randomSizePassword: String, salt: String? = null): String

    fun decrypt(text: String, key: String, seed: String? = null): String?
    fun encrypt(text: String, key: String, seed: String? = null): String?


    @Deprecated("Dont use this is insecure.")
    fun decrypt(text: String): String?
    @Deprecated("Dont use this is insecure.")
    fun encrypt(text: String): String?


    @Deprecated("Dont use this is insecure.")
    fun decrypt(text: String, key: String): String?
    @Deprecated("Dont use this is insecure.")
    fun encrypt(text: String, key: String): String?
}
