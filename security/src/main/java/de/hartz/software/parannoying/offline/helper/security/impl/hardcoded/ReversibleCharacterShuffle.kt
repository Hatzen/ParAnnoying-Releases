package de.hartz.software.parannoying.offline.helper.security.impl.hardcoded

import de.hartz.software.parannoying.core.interfaces.di.security.HardcodedEncryptionHelper
import javax.inject.Inject


class ReversibleCharacterShuffle @Inject constructor(): HardcodedEncryptionHelper {

    override fun decrypt(text: String): String {
        return shuffleStringWithSin(shuffleStringHalf(text))
    }

    override fun encrypt(text: String): String {
        return unshuffleStringHalf(unshuffleStringWithSin(text))
    }

    fun shuffleString(input: String): String {
        return shuffleStringWithSin(shuffleStringHalf(input))
    }

    fun unshuffleString(input: String): String {
        return unshuffleStringHalf(unshuffleStringWithSin(input))
    }

    private fun shuffleStringWithSin(input: String): String {
        val chars = input.toCharArray()
        val len = chars.size

        // Apply sine function to character positions to introduce additional randomness.
        for (i in 0 until len) {
            val angle = 2.0 * Math.PI * i / len // Adjust the factor for desired randomness.
            var newPosition = (i + len * Math.sin(angle)).toInt() % len
            if (newPosition < 0) {
                newPosition += len // Normalize the new position if it's negative.
            }
            val temp = chars[i]
            chars[i] = chars[newPosition]
            chars[newPosition] = temp
        }
        return chars.concatToString()
    }

    private fun unshuffleStringWithSin(shuffled: String): String {
        val chars = shuffled.toCharArray()
        val len = chars.size

        // Apply the inverse sine function to character positions for unshuffling.
        for (i in len - 1 downTo 0) {
            val angle = 2.0 * Math.PI * i / len // Adjust the factor for desired randomness.
            var newPosition = (i + len * Math.sin(angle)).toInt() % len
            if (newPosition < 0) {
                newPosition += len // Normalize the new position if it's negative.
            }
            val temp = chars[i]
            chars[i] = chars[newPosition]
            chars[newPosition] = temp
        }
        return chars.concatToString()
    }

    private fun shuffleStringHalf(input: String): String {
        val chars = input.toCharArray()
        val len = chars.size

        // Create two separate character arrays for even and odd-indexed characters.
        val evenChars = CharArray((len + 1) / 2)
        val oddChars = CharArray(len / 2)

        // Separate even and odd characters.
        for (i in 0 until len) {
            if (i % 2 == 0) {
                evenChars[i / 2] = chars[i]
            } else {
                oddChars[i / 2] = chars[i]
            }
        }

        // Combine odd and even characters to shuffle.
        val shuffled = StringBuilder()
        shuffled.append(evenChars)
        shuffled.append(oddChars)
        return shuffled.toString()
    }

    private fun unshuffleStringHalf(shuffled: String): String {
        val chars = shuffled.toCharArray()
        val len = chars.size

        // Determine the length of the original input string.
        val originalLen = len / 2 + if (len % 2 == 0) 0 else 1

        // Create character arrays for even and odd-indexed characters.
        val evenChars = CharArray(originalLen)
        val oddChars = CharArray(len - originalLen)

        // Separate even and odd characters.
        for (i in 0 until len) {
            if (i < originalLen) {
                evenChars[i] = chars[i]
            } else {
                oddChars[i - originalLen] = chars[i]
            }
        }

        // Combine even and odd characters to unshuffle.
        val unshuffled = StringBuilder()
        for (i in 0 until originalLen) {
            unshuffled.append(evenChars[i])
            if (i < len - originalLen) {
                unshuffled.append(oddChars[i])
            }
        }
        return unshuffled.toString()
    }

}