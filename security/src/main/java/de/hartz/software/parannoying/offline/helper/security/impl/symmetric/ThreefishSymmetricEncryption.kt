package de.hartz.software.parannoying.offline.helper.security.impl.symmetric

import de.hartz.software.parannoying.core.interfaces.di.security.DataConverter
import de.hartz.software.parannoying.core.interfaces.di.security.SymmetricEncryptionHelper
import org.bouncycastle.crypto.BlockCipher
import org.bouncycastle.crypto.engines.ThreefishEngine
import org.bouncycastle.crypto.paddings.PKCS7Padding
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher
import org.bouncycastle.crypto.params.KeyParameter
import java.security.SecureRandom
import javax.inject.Inject

class ThreefishSymmetricEncryption @Inject constructor(val dataConverter: DataConverter):
    SymmetricEncryptionHelper {
    companion object {
        // TODO: Adjust size to match or remove completly
        val STATIC_IV = "aMase4l22K1lfH1k"
        val STATIC_KEY = "aMase4l22K1lfH1kaMase4l22K1lfH1k"
    }
    override val KEY_SIZE: Int = 128
    override val SEED_SIZE: Int = 111 // TODO: set proper size

    // In bits for libs.
    val keySize = KEY_SIZE * 8


    private val blockCipher: BlockCipher = ThreefishEngine(keySize)

    override fun getKeyFromPassphrase(randomSizePassword: String, salt: String?): String {
        TODO("Not yet implemented")
    }

    fun generateKey(): ByteArray {
        val key = ByteArray(KEY_SIZE)
        SecureRandom().nextBytes(key)
        return key
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

    override fun encrypt(data: String, key: String, seed: String?): String {
        val bytes = dataConverter.stringToByteArray(data)
        val keyBytes = dataConverter.stringToByteArray(key)

        val paddedCipher = PaddedBufferedBlockCipher(blockCipher, PKCS7Padding())
        val keyParameter = KeyParameter(keyBytes)
        paddedCipher.init(true, keyParameter)

        val output = ByteArray(paddedCipher.getOutputSize(bytes.size))
        val len = paddedCipher.processBytes(bytes, 0, bytes.size, output, 0)
        paddedCipher.doFinal(output, len)

        return dataConverter.byteArrayToString(output)
    }

    override fun decrypt(encryptedData: String, key: String, seed: String?): String {
        val bytes = dataConverter.stringToByteArray(encryptedData)
        val keyBytes = dataConverter.stringToByteArray(key)

        val paddedCipher = PaddedBufferedBlockCipher(blockCipher, PKCS7Padding())
        val keyParameter = KeyParameter(keyBytes)
        paddedCipher.init(false, keyParameter)

        val output = ByteArray(paddedCipher.getOutputSize(bytes.size))
        val len = paddedCipher.processBytes(bytes, 0, bytes.size, output, 0)
        paddedCipher.doFinal(output, len)

        return dataConverter.byteArrayToString(output)
    }

}