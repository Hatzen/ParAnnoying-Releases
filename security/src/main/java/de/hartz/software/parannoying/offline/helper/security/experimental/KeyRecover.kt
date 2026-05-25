package de.hartz.software.parannoying.offline.helper.security.experimental

import android.os.Build
import androidx.annotation.RequiresApi
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPrivateCrtKeySpec
import java.security.spec.RSAPrivateKeySpec
import java.util.Base64
import javax.crypto.Cipher

class KeyRecover {
}


@RequiresApi(Build.VERSION_CODES.O)
fun main() {
    // 🔹 Generate and encode RSA
    val rsaKeys = generateRSAKeyPair()
    val rsaCompact = rsaToCompact(rsaKeys)
    val rsaReconstructed = rsaFromCompact(rsaCompact)

    // 🔹 Generate and encode Ed25519
    val edKeys = generateEd25519KeyPair()
    val edCompact = ed25519ToCompact(edKeys)
    val edReconstructed = ed25519FromCompact(edCompact)

    // 🔹 Sign with Ed25519
    val message = "Hello, world!"
    val signedMessage = signMessage(edReconstructed, message)
    val isVerified = verifySignature(edKeys.public, message, signedMessage)

    // 🔹 Encrypt/Decrypt with RSA
    val encryptedMessage = encryptRSA(rsaKeys.public, message)
    val decryptedMessage = decryptRSA(rsaReconstructed, encryptedMessage)

    println("🔐 RSA Compact Key: $rsaCompact")
    println("🔑 Ed25519 Compact Key: $edCompact")
    println("✅ Ed25519 Signature Verified: $isVerified")
    println("🔒 RSA Encrypted Message: ${Base64.getUrlEncoder().encodeToString(encryptedMessage)}")
    println("🔓 RSA Decrypted Message: $decryptedMessage")
}

// Security.addProvider(BouncyCastleProvider())

// Generate RSA Key Pair
fun generateRSAKeyPair(): KeyPair {
    val keyGen = KeyPairGenerator.getInstance("RSA", "BC")
    keyGen.initialize(4096)
    return keyGen.generateKeyPair()
}

// Convert RSA Private Key to Compact Format
@RequiresApi(Build.VERSION_CODES.O)
fun rsaToCompact(keyPair: KeyPair): String {
    val keyFactory = KeyFactory.getInstance("RSA")
    val spec = keyFactory.getKeySpec(keyPair.private, RSAPrivateCrtKeySpec::class.java)

    val modulus = spec.modulus.toByteArray()
    val privateExponent = spec.privateExponent.toByteArray()
    val compactBytes = modulus + privateExponent

    return Base64.getUrlEncoder().withoutPadding().encodeToString(compactBytes)
}

// Generate Ed25519 Key Pair
fun generateEd25519KeyPair(): KeyPair {
    val keyGen = KeyPairGenerator.getInstance("Ed25519", "BC")
    return keyGen.generateKeyPair()
}

// Convert Ed25519 Private Key to Compact Format
@RequiresApi(Build.VERSION_CODES.O)
fun ed25519ToCompact(keyPair: KeyPair): String {
    val privateKeyBytes = keyPair.private.encoded
    return Base64.getUrlEncoder().withoutPadding().encodeToString(privateKeyBytes)
}// Reconstruct RSA Private Key from Compact Format
@RequiresApi(Build.VERSION_CODES.O)
fun rsaFromCompact(compactString: String): PrivateKey {
    val keyFactory = KeyFactory.getInstance("RSA")
    val compactBytes = Base64.getUrlDecoder().decode(compactString)

    // Split back into modulus & private exponent
    val modulusLength = compactBytes.size / 2
    val modulus = BigInteger(1, compactBytes.sliceArray(0 until modulusLength))
    val privateExponent = BigInteger(1, compactBytes.sliceArray(modulusLength until compactBytes.size))

    val spec = RSAPrivateKeySpec(modulus, privateExponent)
    return keyFactory.generatePrivate(spec)
}

// Reconstruct Ed25519 Private Key from Compact Format
@RequiresApi(Build.VERSION_CODES.O)
fun ed25519FromCompact(compactString: String): PrivateKey {
    val keyBytes = Base64.getUrlDecoder().decode(compactString)
    val keyFactory = KeyFactory.getInstance("Ed25519", "BC")
    val spec = PKCS8EncodedKeySpec(keyBytes)
    return keyFactory.generatePrivate(spec)
}

// Sign a message with Ed25519
@RequiresApi(Build.VERSION_CODES.O)
fun signMessage(privateKey: PrivateKey, message: String): String {
    val signature = Signature.getInstance("Ed25519")
    signature.initSign(privateKey)
    signature.update(message.toByteArray())
    return Base64.getUrlEncoder().encodeToString(signature.sign())
}

// Verify the signature
@RequiresApi(Build.VERSION_CODES.O)
fun verifySignature(publicKey: PublicKey, message: String, signedMessage: String): Boolean {
    val signature = Signature.getInstance("Ed25519")
    signature.initVerify(publicKey)
    signature.update(message.toByteArray())
    return signature.verify(Base64.getUrlDecoder().decode(signedMessage))
}

// Encrypt with RSA
fun encryptRSA(publicKey: PublicKey, plaintext: String): ByteArray {
    val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
    cipher.init(Cipher.ENCRYPT_MODE, publicKey)
    return cipher.doFinal(plaintext.toByteArray())
}

// Decrypt with RSA
fun decryptRSA(privateKey: PrivateKey, ciphertext: ByteArray): String {
    val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
    cipher.init(Cipher.DECRYPT_MODE, privateKey)
    return String(cipher.doFinal(ciphertext))
}