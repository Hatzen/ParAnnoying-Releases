package de.hartz.software.parannoying.offline.helper.security.impl.converter

import android.util.Base64
import com.google.gson.Gson
import de.hartz.software.parannoying.core.interfaces.di.security.DataConverter
import java.math.BigInteger
import java.nio.ByteBuffer
import java.util.Arrays
import javax.inject.Inject

class DataConverterImpl @Inject constructor(): DataConverter {

    /**
     * TODO: Move this base64 encoding to own class.
     *  Especially the result is often easy to identify AAA...== as usually the numbers are very low and near to each other.
     *  But not a security issue as the content gets encrypted usually.
     *
     *  Java and android base64 differences so maybe use library to have common for every enviroment
     */
    override fun longToString(int: Long) : String {
        val bytes = ByteBuffer.allocate(8).putLong(int).array()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    override fun stringToLong(string: String) : Long {
        val wrapped = ByteBuffer.wrap(Base64.decode(string, Base64.DEFAULT))
        return wrapped.long
    }

    override fun intToString(int: Int) : String {
        val bytes = ByteBuffer.allocate(4).putInt(int).array()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    override fun stringToInt(string: String) : Int {
        val wrapped = ByteBuffer.wrap(Base64.decode(string, Base64.DEFAULT))
        return wrapped.int
    }

    override fun byteArrayToString(string: ByteArray) :String  {
        // return String(org.bouncycastle.util.encoders.Base64.encode(string))
        return String(string)
    }

    override fun stringToByteArray(string: String) : ByteArray {
        // return org.bouncycastle.util.encoders.Base64.decode(string)
        return string.toByteArray()
    }


    override fun base64Encode(string: ByteArray) :String  {
        return String(org.bouncycastle.util.encoders.Base64.encode(string))
    }

    override fun base64Decode(string: String) : ByteArray {
        return org.bouncycastle.util.encoders.Base64.decode(string)
    }

    override fun objectToString(any: Any): String {
        val gson = Gson()
        val result = gson.toJson(any)
        return result
    }

    override fun <T> stringToObject(string: String, clazz: Class<T>): T {
        val gson = Gson()
        val result = gson.fromJson(string, clazz)
        return result
    }

    override fun base64LengthOfInt() : Int {
        val dataSize = 8
        return (4 * (dataSize / 3)) + 1 // The + 1 seems to be needed to get the correct position within a substring start and or end
    }

    override fun base64LengthOfLong() : Int {
        // https://stackoverflow.com/questions/13378815/base64-length-calculation
        // TODO: Beautify and unify with ofInt
        val dataSize = 11
        return ((4 * (dataSize/ 3))) + 1 // The + 1 seems to be needed to get the correct position within a substring start and or end
    }


    private val isAndroid: Boolean = try {
        Class.forName("android.os.Build")
        true
    } catch (e: ClassNotFoundException) {
        false
    }

    fun encode(input: ByteArray): String {
        return if (isAndroid) {
            android.util.Base64.encodeToString(input, android.util.Base64.NO_WRAP)
        } else {
            java.util.Base64.getEncoder().encodeToString(input)
        }
    }

    fun decode(input: String): ByteArray {
        return if (isAndroid) {
            android.util.Base64.decode(input, android.util.Base64.NO_WRAP)
        } else {
            java.util.Base64.getDecoder().decode(input)
        }
    }


    /**
     * Feature	Base-36 (Your Approach)	Base-64
     * Compactness	✅ (for small numbers)	✅✅ (for binary/random data)
     * URL-Safe	✅ (No special chars)	❌ (Needs Base64-URL variant)
     * Performance	❌ (BigInteger conversion is slow)	✅ (Fast bitwise encoding)
     * Standard Usage	❌ (Uncommon for binary)	✅ (Widely used in cryptography, encoding, JWTs, etc.)
     * Readability	✅ (Lowercase letters & numbers)	❌ (Mixed-case & special chars)
     */
    private fun bytesToString(b: ByteArray): String {
        val b2 = ByteArray(b.size + 1)
        b2[0] = 1
        System.arraycopy(b, 0, b2, 1, b.size)
        return BigInteger(b2).toString(36)
    }

    private fun stringToBytes(s: String): ByteArray {
        val b2 = BigInteger(s, 36).toByteArray()
        return Arrays.copyOfRange(b2, 1, b2.size)
    }


    // Convert a key (byte array) to Base58
    fun keyToBase58(key: ByteArray): String {
        val alphabet = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
        var num = BigInteger(1, key)
        val encoded = StringBuilder()
        while (num > BigInteger.ZERO) {
            val remainder = num.mod(BigInteger.valueOf(58))
            num = num.divide(BigInteger.valueOf(58))
            encoded.insert(0, alphabet[remainder.toInt()])
        }
        return encoded.toString()
    }

    // Convert Base58 back to byte array
    fun base58ToKey(base58: String): ByteArray {
        val alphabet = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
        var num = BigInteger.ZERO
        for (char in base58) {
            num = num.multiply(BigInteger.valueOf(58)).add(BigInteger.valueOf(alphabet.indexOf(char).toLong()))
        }
        return num.toByteArray()
    }
}