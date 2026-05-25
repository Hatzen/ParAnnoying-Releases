package de.hartz.software.parannoying.offline.helper.security.impl.hardcoded

import android.util.Base64
import de.hartz.software.parannoying.core.interfaces.di.security.DataConverter
import de.hartz.software.parannoying.core.interfaces.di.security.HardcodedEncryptionHelper
import java.util.BitSet
import javax.inject.Inject

class HardcodedEncryptionHelperImpl @Inject constructor(val dataConverter: DataConverter):
    HardcodedEncryptionHelper {


    override fun decrypt(text: String): String {
        return removeSaltFromString(text)
    }

    override fun encrypt(text: String): String {
        return saltString(text)
    }

    /**
     * Shift and salt some bytes, be careful changes to this will make the password (old data) unreadable.
     */
    fun saltString(data: String) : String {
        val size = data.length * 8
        val sizeOfData = size + (size / 7) + 1 + (size / 71) + 1
        val bitSet = BitSet(sizeOfData)
        val oldBitSet = BitSet.valueOf(data.toByteArray())
        // Counter for set current bits
        var z = 0
        for ( i in 0..oldBitSet.length()) {
            if (i % 7 == 0) {
                if (getRandomBoolean()) {
                    bitSet.set(z)
                }
                ++z
            } else if (i % 71 == 0) {
                if (getRandomBoolean()) {
                    bitSet.set(z)
                }
                ++z
            }
            if (oldBitSet.get(i)) {
                bitSet.set(z)
            }
            ++z
        }
        var saltedString = Base64.encodeToString(bitSet.toByteArray(), Base64.DEFAULT)
        saltedString = dataConverter.intToString(z) + saltedString
        saltedString = ReversibleCharacterShuffle().shuffleString(saltedString)
        return saltedString
    }

    /**
     * Unshift and unsalt
     */
    fun removeSaltFromString(saltedString: String) : String {
        val data = ReversibleCharacterShuffle().unshuffleString(saltedString)
        val base64LengthOfInt = dataConverter.base64LengthOfInt()
        val sizeOfData = dataConverter.stringToInt(data.substring(0, base64LengthOfInt))
        val cleanData = Base64.decode(data.substring(base64LengthOfInt, data.length), Base64.DEFAULT)

        val oldBitSet = BitSet.valueOf(cleanData)
        val bitSet = BitSet(sizeOfData)
        var z = 0
        var lengthOfOriginalData = 0
        for ( i in 0..oldBitSet.length()) {
            // Skip every x Bit
            if (i % 7 == 0) {
                ++z
            } else if (i % 71 == 0) {
                ++z
            }
            // If it ends with this bit end here.
            if (z >= sizeOfData) {
                break
            }
            // Else set this bit
            if (oldBitSet.get(z)) {
                bitSet.set(i)
            }
            ++z
            // Calculate length of Byte array
            lengthOfOriginalData = (i / 8) + 1
        }
        val longArray = bitSet.toByteArray()
        // Length of array is size of data + 1
        val originalData = ByteArray(lengthOfOriginalData)
        for ( i in 0 until lengthOfOriginalData) {
            originalData[i] = longArray[i]
        }
        return String(originalData)
    }

    private fun getRandomBoolean(): Boolean {
        // TODO: Use some variable so it is not easily identified as trash. But dont use date or anything which might give insights of any device data.
        return Math.random() < 0.5
    }

}