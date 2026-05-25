package de.hartz.software.parannoying.offline.helper.security.impl.hash

import de.hartz.software.parannoying.core.interfaces.di.security.DataConverter
import de.hartz.software.parannoying.core.interfaces.di.security.HashHelper
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import kotlin.math.min

class HashHelperImpl @Inject constructor(val dataConverter: DataConverter): HashHelper {

    override fun getStringHashForUi(text: String): String {
        try {
            val crypt = MessageDigest.getInstance("SHA-1")
            crypt.reset()
            crypt.update(text.toByteArray(charset("UTF-16")))

            return BigInteger(1, crypt.digest()).toString(16).substring(0, 10)
        } catch (e: Exception) {
            return "" + text.hashCode()
        }
    }

    override fun hashWithSpecificLength (vararg seeds: String, length: Int): String {
        return hash(*seeds).substring(0, length)
    }

    override fun hashWithMaxLength (vararg seeds: String, length: Int): String {
        val hash = hash(*seeds)
        return hash.substring(0, min(hash.length, length))
    }

    override fun hash (vararg seeds: String): String {
        val sortedSeeds = seeds.toMutableList()
        sortedSeeds.sort() // Sort so the order of seeds doesnt matter.
        val seedsIterator = sortedSeeds.listIterator()

        var previous = hash(seedsIterator.next())
        var next: String
        while (seedsIterator.hasNext()) {
            next = seedsIterator.next()
            // Use concat to combine string value
            var combined = previous + next

            // Use previous hash.
            previous = hash(combined)
        }
        return previous
    }

    override fun hash(seed: String): String {
        // TODO: use salt properly?
        return generatePBKDF2Hash(seed, SALT_LIST[2].toByteArray())
    }

    private fun generatePBKDF2Hash(password: String, salt: ByteArray = SecureRandom().generateSeed(16)): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, 65536, 256)
        // Cant use PBKDF2WithHmacSHA256 https://stackoverflow.com/a/51092975/8524651
        val factory = SecretKeyFactory.getInstance("PBKDF2withHmacSHA1")
        val hash = factory.generateSecret(spec).encoded
        return dataConverter.base64Encode(hash)
    }

    override fun getStaticSaltList(): Array<String> {
        return SALT_LIST
    }


    val SALT_LIST = arrayOf(
        "1984ca1e41d17ade8df34d15f9230d6a0f24295e",
        "5de1574f1d4b1a48f3caf8e122560c70fc54c012",
        "c2789bac0dbeae206b77cae6d88f2da0c336ae3f",
        "c21ec52318c41dca44f5c1731f6a47eac4928f4d",
        "83c6eaa8fa100c0d9f9a7d6ee343e4d0459edfbd",
        "46054b3946df711a04aa863e2217189f766d369e",
        "fa2e717743502b4080656e45b4a8e4ab0a693258",
        "86fbaa9783256f0faf6bd12417122097127015e7",
        "9d6558cef7911ecae490c458977c87d8bc2d1aab",
        "aed93f864202135f58ab984913b1a32e33cfd73e",
        "0cb731866106bf0926fb6e868d776b9e74e74c31",
        "0a45df606a47f69e282e10aa85ece12c23834c98",
        "b4c073e5bd029bec3626784b87bf0a8d152956e5",
        "3ecf9bfada3be5e027811e3c21c49c2853173e01"
    )

}