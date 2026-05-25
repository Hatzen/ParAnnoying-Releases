package de.hartz.software.parannoying.offline.helper.security.experimental

import android.os.Build
import androidx.annotation.RequiresApi
import org.bouncycastle.crypto.engines.ThreefishEngine
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.TweakableBlockCipherParameters
import java.security.SecureRandom
import java.util.Base64


// TODO: Move to symmetric helper..
fun generateRandomTweak(): ByteArray {
    val tweak = ByteArray(16) // 128 Bit Tweak
    SecureRandom().nextBytes(tweak)
    return tweak
}

fun encryptThreefish(plaintext: ByteArray, key: ByteArray, tweak: ByteArray): ByteArray {
    val engine = ThreefishEngine(512) // Threefish-512 mit 64-Byte Blöcken
    val params = TweakableBlockCipherParameters(KeyParameter(key), tweak)
    engine.init(true, params)

    val ciphertext = ByteArray(plaintext.size)
    engine.processBlock(plaintext, 0, ciphertext, 0)
    return ciphertext
}

fun decryptThreefish(ciphertext: ByteArray, key: ByteArray, tweak: ByteArray): ByteArray {
    val engine = ThreefishEngine(512)
    val params = TweakableBlockCipherParameters(KeyParameter(key), tweak)
    engine.init(false, params)

    val decrypted = ByteArray(ciphertext.size)
    engine.processBlock(ciphertext, 0, decrypted, 0)
    return decrypted
}

@RequiresApi(Build.VERSION_CODES.O)
fun main() {
    val key = ByteArray(64) // Threefish-512 benötigt einen 512-Bit (64-Byte) Schlüssel
    SecureRandom().nextBytes(key)

    val tweak = generateRandomTweak() // Zufälliger Tweak-Wert
    val message = "Hallo, Threefish!".toByteArray()

    println("Original: ${String(message)}")

    val encrypted = encryptThreefish(message, key, tweak)
    println("Verschlüsselt (Base64): ${Base64.getEncoder().encodeToString(encrypted)}")

    val decrypted = decryptThreefish(encrypted, key, tweak)
    println("Entschlüsselt: ${String(decrypted)}")
}