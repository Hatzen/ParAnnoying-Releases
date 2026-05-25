package de.hartz.software.parannoying.offline.helper.security.experimental


import android.os.Build
import androidx.annotation.RequiresApi
import java.net.NetworkInterface
import java.security.SecureRandom
import java.time.Instant
import java.util.UUID
import kotlin.experimental.or

@RequiresApi(Build.VERSION_CODES.O)
fun extractTimeAndMac(uuid: UUID) {
    val timestamp = (uuid.timestamp() - 0x01b21dd213814000L) / 10_000 // Convert to milliseconds
    val instant = Instant.ofEpochMilli(timestamp)

    val nodeId = uuid.node().toString(16).padStart(12, '0') // Node (MAC or random)
    val clockSeq = uuid.clockSequence() // Clock sequence

    println("Extracted Timestamp: $instant")
    println("Extracted MAC (or Random Node ID): $nodeId")
    println("Extracted Clock Sequence: $clockSeq")
}

fun main() {
    val uuid = generateUUIDv1()
    println("Generated UUIDv1: $uuid")
    // val uuid = UUID.fromString("f47ac10b-58cc-11d4-a716-446655440000") // Example UUIDv1

    extractTimeAndMac(uuid)
}


fun generateUUIDv1(): UUID {
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

// 🔹 **Get MAC Address for Node ID (If Available)**
fun getMacAddress(): Long? {
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

// 🔹 **Generate a Random Node ID**
fun generateRandomNode(): Long {
    val node = ByteArray(6)
    SecureRandom().nextBytes(node)
    node[0] = (node[0] or 0x01).toByte() // Set multicast bit to avoid real MAC conflicts
    return node.toHexLong()
}

// 🔹 **Convert ByteArray to Long**
fun ByteArray.toHexLong(): Long {
    return this.fold(0L) { acc, byte -> (acc shl 8) or (byte.toLong() and 0xFF) }
}