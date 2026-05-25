package de.hartz.software.parannoying.offline.helper.security.impl.asymmetric.rsa

import org.bouncycastle.crypto.AsymmetricBlockCipher
import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.encodings.PKCS1Encoding
import org.bouncycastle.crypto.engines.RSAEngine
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters
import org.bouncycastle.crypto.params.RSAKeyParameters
import java.math.BigInteger
import java.security.SecureRandom

object RabinWilliamsRsaEncryptionHelper {
    private const val PKCS1_PADDING_SIZE = 11 // Padding size for PKCS#1 v1.5

    fun maxPlaintextSize(bitLength: Int): Int {
        val keySizeInBytes = bitLength / 8
        return keySizeInBytes - PKCS1_PADDING_SIZE
    }

    private val random = SecureRandom()

    data class RWKeyPair(val publicKey: RSAKeyParameters, val privateKey: RSAKeyParameters)

    fun generateRWKeyPair(bitLength: Int = 2048): RWKeyPair {
        val e = BigInteger.valueOf(131071) // 65537 Common public exponent
        val pGen = RSAKeyPairGenerator()
        val commonCertainity = 80
        pGen.init(RSAKeyGenerationParameters(e, random, bitLength, 101))

        val keyPair: AsymmetricCipherKeyPair = pGen.generateKeyPair()
        val publicKey = keyPair.public as RSAKeyParameters
        val privateKey = keyPair.private as RSAKeyParameters

        return RWKeyPair(publicKey, privateKey)
    }

    fun encryptMessage(message: String, publicKey: RSAKeyParameters): ByteArray {
        val engine: AsymmetricBlockCipher = PKCS1Encoding(RSAEngine()) // Use padding
        engine.init(true, publicKey)

        return engine.processBlock(message.toByteArray(), 0, message.length)
    }

    fun decryptMessage(encryptedData: ByteArray, privateKey: RSAKeyParameters): String {
        val engine: AsymmetricBlockCipher = PKCS1Encoding(RSAEngine()) // Use padding
        engine.init(false, privateKey)

        val decryptedBytes = engine.processBlock(encryptedData, 0, encryptedData.size)
        return String(decryptedBytes)
    }
}