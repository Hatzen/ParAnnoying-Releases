package de.hartz.software.parannoying.offline.helper.security.impl.symmetric

import android.util.Base64
import android.util.Log
import de.hartz.software.parannoying.core.interfaces.di.StorageInterface
import de.hartz.software.parannoying.core.interfaces.di.security.CompressionHelper
import de.hartz.software.parannoying.core.interfaces.di.security.DataConverter
import de.hartz.software.parannoying.core.interfaces.di.security.RandomHelper
import de.hartz.software.parannoying.core.interfaces.di.security.SymmetricEncryptionHelper
import java.nio.charset.Charset
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

// https://stackoverflow.com/questions/18573573/aes-512-bit-encryption-with-android
// https://javapointers.com/tutorial/how-to-encrypt-and-decrypt-using-aes-in-java/
// TODO: Not considered yet, but might be useful:
// https://stackoverflow.com/questions/8622367/what-are-best-practices-for-using-aes-encryption-in-android
class AESEncryptor @Inject constructor(
    val dataConverter: DataConverter,
    val compressionHelper: CompressionHelper,
    val randomHelper: RandomHelper
) : SymmetricEncryptionHelper {

    override val KEY_SIZE: Int = 32
    override val SEED_SIZE: Int = 16

    override fun getKeyFromPassphrase(randomSizePassword: String, salt: String?): String {
        // TODO: define proper salt.
        return stretchKey(randomSizePassword, salt ?: "test1223")
    }

    companion object {
        // TODO: We need manual padding? https://stackoverflow.com/questions/39401521/aes-cfb-pkcs5padding-behaving-differently-on-different-android-versions
         private val AES_ALGORITHM = "AES/CBC/PKCS5Padding"

        val STATIC_IV = "aMase4l22K1lfH1k"
        val STATIC_KEY = "aMase4l22K1lfH1kaMase4l22K1lfH1k"
    }

    fun stretchKey(key: String, salt: String): String {
        val password = key.toCharArray()
        val salt = salt.toByteArray()

        val spec = PBEKeySpec(password, salt, 10000, 256)

        val keyFactory = SecretKeyFactory.getInstance("PBKDF2withHmacSHA1")
        val hash = keyFactory.generateSecret(spec).encoded
        return Base64.encodeToString(hash, Base64.DEFAULT).substring(0, KEY_SIZE)
    }

    override fun decrypt(text: String, key: String, seedOrig: String?): String? {
        if (seedOrig?.length != SEED_SIZE || key.length != KEY_SIZE) {
            throw IllegalArgumentException()
        }
        val seed = if (seedOrig == null) {
            randomHelper.computeSecureRandomHashWithSpecificLength(SEED_SIZE)
        } else {
            seedOrig!!
        }

        val ivSpec = IvParameterSpec(seed.toByteArray())
        val secretKeySpec = SecretKeySpec(key.toByteArray(), AES_ALGORITHM)

        if (!StorageInterface.SECURE) {
            return text
        }
        try {
            // TODO: Replace AES/CBC/PKCS5Padding with "AES/GCM/NoPadding"
            val cipher = Cipher.getInstance(AES_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec)
            val decryptedBytes = cipher.doFinal(compressionHelper.decompress(text))
            return String(decryptedBytes, Charset.forName("UTF-8"))
        } catch (e: Exception) {
            Log.w(javaClass.simpleName, e.message, e)
        }
        return null
    }

    override fun decrypt(text: String): String? {
        return decrypt(text, STATIC_KEY, STATIC_IV)
    }

    override fun decrypt(text: String, key: String): String? {
        return decrypt(text, key, STATIC_IV)
    }

    override fun encrypt(text: String): String? {
        return encrypt(text, STATIC_KEY, STATIC_IV)
    }

    override fun encrypt(text: String, key: String): String? {
        return encrypt(text, key, STATIC_IV)
    }

    override fun encrypt(text: String, key: String, seedOrig: String?): String? {
        if (seedOrig?.length != SEED_SIZE || key.length != KEY_SIZE) {
            throw IllegalArgumentException("iv or key not proper length ${seedOrig?.length} != $SEED_SIZE || ${key.length} != $KEY_SIZE")
        }
        val seed = if (seedOrig == null) {
            randomHelper.computeSecureRandomHashWithSpecificLength(SEED_SIZE)
        } else {
            seedOrig!!
        }

        val ivSpec = IvParameterSpec(seed.toByteArray())
        val secretKeySpec = SecretKeySpec(key.toByteArray(), AES_ALGORITHM)

        if (!StorageInterface.SECURE) {
            return text
        }
        var encryptedValue: String? = null
        try {
            val cipher = Cipher.getInstance(AES_ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec)
            val encrypted  = cipher.doFinal(text.toByteArray())
            encryptedValue = compressionHelper.compress(encrypted)
        } catch (e: Exception) {
            Log.e(this.javaClass.toString(), e.message, e)
        }
        return encryptedValue
    }
}
