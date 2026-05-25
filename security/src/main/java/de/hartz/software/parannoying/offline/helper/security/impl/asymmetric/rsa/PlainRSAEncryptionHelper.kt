package de.hartz.software.parannoying.offline.helper.security.impl.asymmetric.rsa

import de.hartz.software.parannoying.core.interfaces.di.StorageInterface
import de.hartz.software.parannoying.core.interfaces.di.security.AsymmetricEncryptionHelper
import de.hartz.software.parannoying.core.interfaces.di.security.DataConverter
import java.security.InvalidKeyException
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.inject.Inject

// https://stackoverflow.com/a/23792993
class PlainRSAEncryptionHelper @Inject constructor(
    val dataConverter: DataConverter
) : AsymmetricEncryptionHelper {

    companion object {
        val GENERAL_ALGORITHM = "RSA"
    }

    override val KEY_SIZE_BITS: Int = 4096
    override fun privateKeyToString(key: PrivateKey): String {
        return KeyConverter().convertToDatabaseValue(key)!!
    }

    override fun publicKeyToString(key: PublicKey): String {
        return KeyConverter().convertToDatabaseValue(key)!!
    }

    override fun getKeyPair(randomSizePassword: String?): KeyPair {
        val kpg: KeyPairGenerator = KeyPairGenerator.getInstance(GENERAL_ALGORITHM)
        kpg.initialize(KEY_SIZE_BITS)
        val kp: KeyPair = kpg.genKeyPair()
        return kp
    }

    override fun decrypt(text: String): String? {
        TODO("Not yet implemented")
    }

    override fun encrypt(text: String): String? {
        TODO("Not yet implemented")
    }

    private lateinit var encryptedBytes: ByteArray
    private lateinit var decryptedBytes:ByteArray
    private lateinit var cipher: Cipher
    private lateinit var cipher1:Cipher
    private lateinit var encrypted: String
    private lateinit var decrypted:String

    @Throws(NoSuchAlgorithmException::class, NoSuchPaddingException::class, InvalidKeyException::class, IllegalBlockSizeException::class, BadPaddingException::class)
    override fun encrypt(plain: String, publicKey: PublicKey): String {
        if (!StorageInterface.SECURE) {
            return plain
        }
        cipher = getCipherCompat()
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)

        encryptedBytes = cipher.doFinal(dataConverter.stringToByteArray(plain))

        encrypted = dataConverter.base64Encode(encryptedBytes)
        return encrypted
    }

    @Throws(NoSuchAlgorithmException::class, NoSuchPaddingException::class, InvalidKeyException::class, IllegalBlockSizeException::class, BadPaddingException::class)
    override fun decrypt(result: String, privateKey: PrivateKey): String {
        if (!StorageInterface.SECURE) {
            return result
        }
        cipher1 = getCipherCompat()
        cipher1.init(Cipher.DECRYPT_MODE, privateKey)
        decryptedBytes = cipher1.doFinal(dataConverter.base64Decode(result))
        decrypted = dataConverter.byteArrayToString(decryptedBytes)
        return decrypted
    }

    private fun getCipherCompat(): Cipher {
        return Cipher.getInstance(GENERAL_ALGORITHM)
    }

}
