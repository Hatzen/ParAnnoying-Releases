package de.hartz.software.parannoying.offline

import de.hartz.software.parannoying.offline.helper.security.impl.asymmetric.ElgamalEncryptionHelper.decryptMessage
import de.hartz.software.parannoying.offline.helper.security.impl.asymmetric.ElgamalEncryptionHelper.encryptMessage
import de.hartz.software.parannoying.offline.helper.security.impl.asymmetric.ElgamalEncryptionHelper.generateElGamalKeyPair
import de.hartz.software.parannoying.offline.helper.security.impl.asymmetric.ElgamalEncryptionHelper.maxPlaintextSize
import de.hartz.software.parannoying.offline.helper.security.impl.asymmetric.rsa.LUCRSAEncryptionHelper
import de.hartz.software.parannoying.offline.helper.security.impl.asymmetric.rsa.RabinWilliamsRsaEncryptionHelper
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test

// Takes a lot of time pobably concept test with tiny keys and tiny data should be sufficient
@Ignore
class AsymmetricTest {

    @Test
    fun test() {
        val bit = 8192 // 3072
        // Generate Key Pair
        val (publicKey, privateKey) = generateElGamalKeyPair(bit)

        // Encrypt Message
        val message = "Hello, ElGamal with BouncyCastle!"
            .repeat(2000)
            .substring(0,
                maxPlaintextSize(bit))
        val encrypted = encryptMessage(message, publicKey)
        println("Encrypted: ${encrypted.joinToString()}")

        // Decrypt Message
        val decrypted = decryptMessage(encrypted, privateKey)
        println("Decrypted: $decrypted")

        Assert.assertEquals(message, decrypted)
    }


    @Test
    fun testRsa() {
        val bit = 8192 // 3072
        // Encrypt Message
        val message = "Hello, Rsa with BouncyCastle!"
            .repeat(2000)
            .substring(0,
                RabinWilliamsRsaEncryptionHelper.maxPlaintextSize(bit)
            )

        // Rabin-Williams (RW)
        val rwKeyPair = RabinWilliamsRsaEncryptionHelper.generateRWKeyPair(bit)
        val rwCipher = RabinWilliamsRsaEncryptionHelper.encryptMessage(message, rwKeyPair.publicKey)
        val rwDecrypted = RabinWilliamsRsaEncryptionHelper.decryptMessage(rwCipher, rwKeyPair.privateKey)
        println("RW Decrypted: $rwDecrypted")

        Assert.assertEquals(message, rwDecrypted)

        // LUC
        val lucKeyPair = LUCRSAEncryptionHelper.generateLUCKeyPair(bit)
        val lucCipher = LUCRSAEncryptionHelper.encryptMessage(message, lucKeyPair.publicKey)
        val lucDecrypted = LUCRSAEncryptionHelper.decryptMessage(lucCipher, lucKeyPair.privateKey, lucKeyPair.publicKey)
        println("LUC Decrypted: $lucDecrypted")

        Assert.assertEquals(message, rwDecrypted)
    }

}