package de.hartz.software.parannoying.offline.helper.security.impl.random

import de.hartz.software.parannoying.core.interfaces.di.security.DataConverter
import de.hartz.software.parannoying.core.interfaces.di.security.RandomHelper
import java.net.NetworkInterface
import java.security.SecureRandom
import java.util.UUID
import javax.inject.Inject
import kotlin.experimental.or

class RandomHelperImpl @Inject constructor(val dataConverter: DataConverter): RandomHelper {

    override fun randomBoolean(): Boolean {
        return SecureRandom().nextBoolean()
    }

    // TODO: Check this is really only used for passwords. otherwise generate random bytes and encode properly to string WHICH IS LONGER THAN REQUIRED!!
    override fun computeRandomHashWithSpecificLength(length: Int): String {
        // https://www.baeldung.com/kotlin-random-alphanumeric-string
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9') + '#' + '-' + '/' + '='
        val generator = SecureRandom()
        return (1..length)
            .map { i -> generator.nextInt(charPool.size) }
            .map(charPool::get)
            .joinToString("");
    }

    override fun computeSecureRandomHashWithSpecificLength(length: Int): String {
        val generator = ByteArray(length)
        SecureRandom().nextBytes(generator)
        return dataConverter.byteArrayToString(generator)
    }

    override fun getRandomPinCode () : String {
        return computeRandomHashWithSpecificLength(9)
    }

    override fun getRandomUUIDv4 () : String {
        return UUID.randomUUID().toString()
    }


    // RFC 4122-compliant UUIDv1
    override fun getIdentifiableUUIDv1(): String {
        return generateUUIDv1().toString()
    }

    private fun generateUUIDv1(): UUID {
        val epoch = System.currentTimeMillis() * 10_000 + 0x01b21dd213814000L // Convert to 100-nanosecond intervals
        val timeLow = (epoch and 0xFFFFFFFFL).toInt()
        val timeMid = ((epoch shr 32) and 0xFFFFL).toInt()
        val timeHighAndVersion = ((epoch shr 48) and 0x0FFF).toInt() or (1 shl 12) // Version 1

        val clockSeq = SecureRandom().nextInt(1 shl 14) // 14-bit clock sequence
        val clockSeqHigh = (clockSeq shr 8) or 0x80 // Set variant bits (RFC 4122)
        val clockSeqLow = clockSeq and 0xFF

        val node = getMacAddress() ?: generateRandomNode() // Use MAC if available, else random

        return UUID(
            timeLow.toLong() shl 32 or (timeMid.toLong() shl 16) or timeHighAndVersion.toLong(),
            (clockSeqHigh.toLong() shl 8) or clockSeqLow.toLong() shl 48 or node
        )
    }

    private fun getMacAddress(): Long? {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (networkInterface in interfaces) {
                val mac = networkInterface.hardwareAddress ?: continue
                if (mac.size == 6) {
                    return mac.toHexLong()
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun generateRandomNode(): Long {
        val node = ByteArray(6)
        SecureRandom().nextBytes(node)
        node[0] = (node[0] or 0x01).toByte() // Set multicast bit to avoid real MAC conflicts
        return node.toHexLong()
    }

    private fun ByteArray.toHexLong(): Long {
        return this.fold(0L) { acc, byte -> (acc shl 8) or (byte.toLong() and 0xFF) }
    }
}