package de.hartz.software.parannoying.offline

import de.hartz.software.parannoying.offline.helper.security.impl.converter.DataConverterImpl
import de.hartz.software.parannoying.offline.helper.security.impl.hardcoded.ReversibleCharacterShuffle
import de.hartz.software.parannoying.offline.helper.security.impl.random.RandomHelperImpl
import org.junit.Assert
import org.junit.Test

class ShuffleTest {

    @Test
    fun test() {
        val original = "abcdefghijklmnopqrstuvwxyz"

        // Shuffle the original string.
        val shuffled = ReversibleCharacterShuffle().shuffleString(original)
        println("Shuffled: $shuffled")

        // Unshuffle the shuffled string.
        val unshuffled = ReversibleCharacterShuffle().unshuffleString(shuffled)
        println("Unshuffled: $unshuffled")

        // Check if the unshuffled result matches the original.
        Assert.assertEquals(original, unshuffled)

    }


    @Test
    fun test2() {
        val original = "abcdefghijklmnopqrstuvwxyz"

        // Shuffle the original string.
        val shuffled = ReversibleCharacterShuffle().shuffleString(original)
        println("Shuffled: $shuffled")

        // Unshuffle the shuffled string.
        val unshuffled = ReversibleCharacterShuffle().unshuffleString(shuffled)
        println("Unshuffled: $unshuffled")

        // Check if the unshuffled result matches the original.
        Assert.assertEquals(original, unshuffled)

    }


    @Test
    fun test3() {
        val original = RandomHelperImpl(DataConverterImpl()).computeRandomHashWithSpecificLength(101)

        // Shuffle the original string.
        var shuffled = ReversibleCharacterShuffle().shuffleString(original)
        shuffled = ReversibleCharacterShuffle().shuffleString(shuffled)
        println("Shuffled: $shuffled")

        // Unshuffle the shuffled string.
        var unshuffled = ReversibleCharacterShuffle().unshuffleString(shuffled)
        unshuffled = ReversibleCharacterShuffle().unshuffleString(unshuffled)
        println("Unshuffled: $unshuffled")

        // Check if the unshuffled result matches the original.
        Assert.assertEquals(original, unshuffled)

    }
}