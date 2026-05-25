package de.hartz.software.parannoying.offline.helper.security.impl.symmetric

import com.goterl.lazysodium.LazySodiumJava
import com.goterl.lazysodium.SodiumJava
import com.goterl.lazysodium.interfaces.SecretBox
import com.goterl.lazysodium.utils.Key
import com.goterl.lazysodium.utils.LibraryLoader
import de.hartz.software.parannoying.core.interfaces.di.security.CompressionHelper
import de.hartz.software.parannoying.core.interfaces.di.security.DataConverter
import de.hartz.software.parannoying.core.interfaces.di.security.RandomHelper
import de.hartz.software.parannoying.core.interfaces.di.security.SymmetricEncryptionHelper
import javax.inject.Inject

// https://github.com/terl/lazysodium-java/blob/master/sample-app/src/main/kotlin/Main.kt
class SalsaSymmetricEncryptor @Inject constructor(
    val dataConverter: DataConverter,
    val compressionHelper: CompressionHelper,
    val randomHelper: RandomHelper
) : SymmetricEncryptionHelper {

    override val KEY_SIZE: Int = SalsaSymmetricEncryptor.KEY_SIZE
    override val SEED_SIZE: Int = SalsaSymmetricEncryptor.SEED_SIZE

    private val lazySodium = LazySodiumJava(SodiumJava(LibraryLoader.Mode.BUNDLED_ONLY))

    override fun getKeyFromPassphrase(randomSizePassword: String, salt: String?): String {
        return Key.fromPlainString(randomSizePassword + salt).getAsPlainString()
    }

    companion object {
        val KEY_SIZE: Int = SecretBox.XSALSA20POLY1305_KEYBYTES
        val SEED_SIZE: Int = SecretBox.NONCEBYTES
        val STATIC_IV = "aMase4l22K1lfH1k+öeföe.sfs".substring(0, SEED_SIZE)
        val STATIC_KEY = "aMase4l22K1lfH1kaMase4l22K1lfH1k".substring(0, KEY_SIZE)
    }

    override fun decrypt(text: String, key: String, seedOrig: String?): String? {
        if (seedOrig?.length != SEED_SIZE || key.length != KEY_SIZE) {
            throw IllegalArgumentException()
        }


        val key: Key = Key.fromPlainString(key)
        val nonce: ByteArray =
            seedOrig.toByteArray()
        val decrypted: String = lazySodium.cryptoSecretBoxOpenEasy(text, nonce, key)
        return decrypted
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

        val key: Key = Key.fromPlainString(key)
        val nonce: ByteArray =
            seedOrig.toByteArray()
        // TODO: Check SecretBox.NONCEBYTES
        return lazySodium.cryptoSecretBoxEasy(text, nonce, key)
    }
}
