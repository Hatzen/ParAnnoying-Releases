package de.hartz.software.parannoying.offline.helper.security.impl.asymmetric.rsa

import android.os.Build
import androidx.annotation.RequiresApi
import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters
import org.bouncycastle.crypto.params.RSAKeyParameters
import java.math.BigInteger
import java.security.SecureRandom

object LUCRSAEncryptionHelper {

    private const val PKCS1_PADDING_SIZE = 11 // Adjust if Rabin-Williams uses different padding

    fun maxPlaintextSize(bitLength: Int): Int {
        val keySizeInBytes = bitLength / 8
        return keySizeInBytes - PKCS1_PADDING_SIZE
    }

    private val random = SecureRandom()

    data class LUCKeyPair(val publicKey: Pair<BigInteger, BigInteger>, val privateKey: BigInteger)

    fun generateLUCKeyPair(bitLength: Int = 2048): LUCKeyPair {
        val e = BigInteger.valueOf(131071) // 65537 Common public exponent
        val pGen = RSAKeyPairGenerator()
        pGen.init(RSAKeyGenerationParameters(e, random, bitLength, 80))

        val keyPair: AsymmetricCipherKeyPair = pGen.generateKeyPair()
        val publicKey = keyPair.public as RSAKeyParameters
        val privateKey = keyPair.private as RSAKeyParameters

        return LUCKeyPair(Pair(publicKey.modulus, publicKey.exponent), privateKey.exponent)
    }

    fun encryptMessage(message: String, publicKey: Pair<BigInteger, BigInteger>): BigInteger {
        val (n, e) = publicKey
        val messageBigInt = BigInteger(1, message.toByteArray())
        return lucasModExp(messageBigInt, e, n)
    }

    fun decryptMessage(ciphertext: BigInteger, privateKey: BigInteger, publicKey: Pair<BigInteger, BigInteger>): String {
        val (n, _) = publicKey
        val decrypted = lucasModExp(ciphertext, privateKey, n)
        return String(decrypted.toByteArray())
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun lucasModExp(m: BigInteger, e: BigInteger, n: BigInteger): BigInteger {
        var u = BigInteger("2")
        var v = m
        val binaryE = e.toString(2)

        for (bit in binaryE.drop(1)) {
            u = (u.multiply(v).subtract(m)).mod(n)
            v = (v.multiply(v).subtract(BigInteger.TWO)).mod(n)
            if (bit == '1') {
                u = (u.multiply(m).subtract(v)).mod(n)
                v = (v.multiply(m).subtract(BigInteger.TWO)).mod(n)
            }
        }
        return u
    }
}