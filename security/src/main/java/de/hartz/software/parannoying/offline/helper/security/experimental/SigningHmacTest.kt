
import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.digests.Blake2bDigest
import org.bouncycastle.crypto.engines.ThreefishEngine
import org.bouncycastle.crypto.generators.ElGamalKeyPairGenerator
import org.bouncycastle.crypto.macs.HMac
import org.bouncycastle.crypto.modes.SICBlockCipher
import org.bouncycastle.crypto.params.ElGamalKeyGenerationParameters
import org.bouncycastle.crypto.params.ElGamalParameters
import org.bouncycastle.crypto.params.ElGamalPrivateKeyParameters
import org.bouncycastle.crypto.params.ElGamalPublicKeyParameters
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV
import org.bouncycastle.crypto.prng.DigestRandomGenerator
import org.bouncycastle.crypto.signers.DSASigner
import org.bouncycastle.crypto.signers.HMacDSAKCalculator
import java.math.BigInteger
import java.security.SecureRandom

// Secure PRNG
val prng = DigestRandomGenerator(Blake2bDigest(512))
val secureRandom = SecureRandom()

// Generate ElGamal Parameters
fun generateElGamalParameters(): ElGamalParameters {
    val p = BigInteger.probablePrime(2048, secureRandom)
    val g = BigInteger.probablePrime(512, secureRandom)
    return ElGamalParameters(p, g)
}

// Generate ElGamal Key Pair
fun generateElGamalKeyPair(): AsymmetricCipherKeyPair {
    val params = generateElGamalParameters()
    val keyGenParams = ElGamalKeyGenerationParameters(secureRandom, params)
    val generator = ElGamalKeyPairGenerator()
    generator.init(keyGenParams)
    return generator.generateKeyPair()
}

// Encrypt with Threefish in CTR Mode
fun encryptThreefish(key: ByteArray, plaintext: ByteArray): ByteArray {
    val cipher = SICBlockCipher(ThreefishEngine(256))
    val nonce = ByteArray(32).also { prng.nextBytes(it) } // IV/Nonce
    cipher.init(true, ParametersWithIV(KeyParameter(key), nonce))
    val encrypted = ByteArray(plaintext.size)
    cipher.processBytes(plaintext, 0, plaintext.size, encrypted, 0)
    return nonce + encrypted // Store nonce with ciphertext
}

// Decrypt with Threefish in CTR Mode
fun decryptThreefish(key: ByteArray, ciphertext: ByteArray): ByteArray {
    val nonce = ciphertext.copyOfRange(0, 32)
    val actualCiphertext = ciphertext.copyOfRange(32, ciphertext.size)
    val cipher = SICBlockCipher(ThreefishEngine(256))
    cipher.init(false, ParametersWithIV(KeyParameter(key), nonce))
    val decrypted = ByteArray(actualCiphertext.size)
    cipher.processBytes(actualCiphertext, 0, actualCiphertext.size, decrypted, 0)
    return decrypted
}

// Hash with BLAKE2b
fun hashBlake2b(data: ByteArray): ByteArray {
    val digest = Blake2bDigest(512)
    digest.update(data, 0, data.size)
    val hash = ByteArray(digest.digestSize)
    digest.doFinal(hash, 0)
    return hash
}

// Generate MAC with HMAC-BLAKE2b
fun generateMac(key: ByteArray, data: ByteArray): ByteArray {
    val mac = HMac(Blake2bDigest(512))
    mac.init(KeyParameter(key))
    mac.update(data, 0, data.size)
    val output = ByteArray(mac.macSize)
    mac.doFinal(output, 0)
    return output
}

// Sign Message with ElGamal
fun signMessage(privateKey: ElGamalPrivateKeyParameters, message: ByteArray): Pair<BigInteger, BigInteger> {
    val signer = DSASigner(HMacDSAKCalculator(Blake2bDigest(512)))
    signer.init(true, privateKey)
    return signer.generateSignature(message).let { it[0] to it[1] }
}

// Verify Signature
fun verifySignature(publicKey: ElGamalPublicKeyParameters, message: ByteArray, signature: Pair<BigInteger, BigInteger>): Boolean {
    val signer = DSASigner(HMacDSAKCalculator(Blake2bDigest(512)))
    signer.init(false, publicKey)
    return signer.verifySignature(message, signature.first, signature.second)
}

fun main() {
    // Generate keys
    val keyPair = generateElGamalKeyPair()
    val publicKey = keyPair.public as ElGamalPublicKeyParameters
    val privateKey = keyPair.private as ElGamalPrivateKeyParameters

    // Example message
    val message = "Secure message".toByteArray()

    // Generate encryption key using PRNG
    val encryptionKey = ByteArray(32)
    prng.nextBytes(encryptionKey)

    // Encrypt
    val encrypted = encryptThreefish(encryptionKey, message)

    // Decrypt
    val decrypted = decryptThreefish(encryptionKey, encrypted)

    // Compute hash
    val hash = hashBlake2b(message)

    // Generate MAC
    val mac = generateMac(encryptionKey, message)

    // Sign
    val signature = signMessage(privateKey, message)

    // Verify Signature
    val isValid = verifySignature(publicKey, message, signature)

    println("Original: ${String(message)}")
    println("Decrypted: ${String(decrypted)}")
    println("Hash: ${hash.joinToString()}")
    println("MAC: ${mac.joinToString()}")
    println("Signature Valid: $isValid")
}
