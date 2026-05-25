package de.hartz.software.parannoying.offline.helper.security.experimental

import android.os.Build
import androidx.annotation.RequiresApi
import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.params.ParametersWithRandom
import org.bouncycastle.pqc.legacy.crypto.mceliece.McElieceCipher
import org.bouncycastle.pqc.legacy.crypto.mceliece.McElieceKeyGenerationParameters
import org.bouncycastle.pqc.legacy.crypto.mceliece.McElieceKeyPairGenerator
import org.bouncycastle.pqc.legacy.crypto.mceliece.McElieceParameters
import org.bouncycastle.pqc.legacy.crypto.mceliece.McEliecePrivateKeyParameters
import org.bouncycastle.pqc.legacy.crypto.mceliece.McEliecePublicKeyParameters
import java.security.SecureRandom
import java.util.Base64

fun generateMcElieceKeyPair(): AsymmetricCipherKeyPair {
    val keyGen = McElieceKeyPairGenerator()
    val params = McElieceKeyGenerationParameters(SecureRandom(), McElieceParameters())
    keyGen.init(params)
    return keyGen.generateKeyPair()
}

fun encryptMcEliece(message: ByteArray, publicKey: McEliecePublicKeyParameters): ByteArray {
    val encryptor = McElieceCipher()
    encryptor.init(true, ParametersWithRandom(publicKey, SecureRandom()))
    return encryptor.messageEncrypt(message)
}

fun decryptMcEliece(ciphertext: ByteArray, privateKey: McEliecePrivateKeyParameters): ByteArray {
    val decryptor = McElieceCipher()
    decryptor.init(false, privateKey)
    return decryptor.messageDecrypt(ciphertext)
}

@RequiresApi(Build.VERSION_CODES.O)
fun main() {
    val keyPair = generateMcElieceKeyPair()
    val publicKey = keyPair.public as McEliecePublicKeyParameters
    val privateKey = keyPair.private as McEliecePrivateKeyParameters

    val message = "Hallo, Quantenwelt!".toByteArray()
    println("Original: ${String(message)}")

    val encrypted = encryptMcEliece(message, publicKey)
    println("Verschlüsselt (Base64): ${Base64.getEncoder().encodeToString(encrypted)}")

    val decrypted = decryptMcEliece(encrypted, privateKey)
    println("Entschlüsselt: ${String(decrypted)}")
}