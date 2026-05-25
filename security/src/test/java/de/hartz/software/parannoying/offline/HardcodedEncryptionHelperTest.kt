package de.hartz.software.parannoying.offline

import de.hartz.software.parannoying.offline.helper.security.impl.converter.DataConverterImpl
import de.hartz.software.parannoying.offline.helper.security.impl.hardcoded.HardcodedEncryptionHelperImpl
import de.hartz.software.parannoying.offline.helper.security.impl.random.RandomHelperImpl
import org.junit.Assert
import org.junit.Test

class HardcodedEncryptionHelperTest {

    @Test
    fun test() {
        val original = "abcdefghijklmnopqrstuvwxyz"

        val encrypt = HardcodedEncryptionHelperImpl(DataConverterImpl()).encrypt(original)
        println("encrypted: $encrypt")

        val decrypt = HardcodedEncryptionHelperImpl(DataConverterImpl()).decrypt(encrypt)
        println("decrypt: $decrypt")

        // Check if the unshuffled result matches the original.
        Assert.assertEquals(original, decrypt)

    }


    @Test
    fun testRandom() {
        val original = RandomHelperImpl(DataConverterImpl()).computeRandomHashWithSpecificLength(101)

        var encrypt = HardcodedEncryptionHelperImpl(DataConverterImpl()).encrypt(original)
        encrypt = HardcodedEncryptionHelperImpl(DataConverterImpl()).encrypt(encrypt)
        println("encrypt: $encrypt")

        var decrypt = HardcodedEncryptionHelperImpl(DataConverterImpl()).decrypt(encrypt)
        decrypt = HardcodedEncryptionHelperImpl(DataConverterImpl()).decrypt(decrypt)
        println("Unshuffled: $decrypt")

        // Check if the unshuffled result matches the original.
        Assert.assertEquals(original, decrypt)

    }
}