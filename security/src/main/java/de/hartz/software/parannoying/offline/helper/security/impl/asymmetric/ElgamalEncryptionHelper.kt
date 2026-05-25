package de.hartz.software.parannoying.offline.helper.security.impl.asymmetric

import org.bouncycastle.crypto.AsymmetricBlockCipher
import org.bouncycastle.crypto.engines.ElGamalEngine
import org.bouncycastle.crypto.generators.ElGamalKeyPairGenerator
import org.bouncycastle.crypto.params.ElGamalKeyGenerationParameters
import org.bouncycastle.crypto.params.ElGamalParameters
import org.bouncycastle.crypto.params.ElGamalPrivateKeyParameters
import org.bouncycastle.crypto.params.ElGamalPublicKeyParameters
import org.bouncycastle.crypto.params.ParametersWithRandom
import java.math.BigInteger
import java.security.SecureRandom


// Discrete Logarithm not quantum safe.
/*
ElGamal and Curve25519:
ElGamal encryption is typically based on the Discrete Logarithm Problem (DLP), which in turn is based on the difficulty of solving the discrete logarithm (finding
𝑥x such that𝑔𝑥=ℎ mod 𝑝 gx=h mod p) in a finite field or elliptic curve. ElGamal encryption, as it is commonly implemented over elliptic curves (e.g., ElGamal over Curve25519), inherits the same vulnerabilities as standard ElGamal in terms of quantum security because DLP is not quantum-safe.

Quantum threat: If a sufficiently powerful quantum computer were available, it could use Shor’s Algorithm to solve the discrete logarithm efficiently, breaking both traditional ElGamal encryption and other DLP-based systems.
So, even if ElGamal is modified for Curve25519, it is still based on discrete logarithms, and thus, it would not be quantum-safe.
 */
object ElgamalEncryptionHelper {

    val BigIntegerTWO = BigInteger.valueOf(2)

    fun maxPlaintextSize(bitLength: Int): Int {
        return (bitLength - 1) / 8
    }

    fun generateElGamalKeyPair(bitLength: Int = 8192): Pair<ElGamalPublicKeyParameters, ElGamalPrivateKeyParameters> {
        val random = SecureRandom()
        val p = BigInteger.probablePrime(bitLength, random)  // Secure prime p

        /*
        Summary
        Generator Choice	Security
        g = 2	✅ Secure if p is a safe prime and 2 is a primitive root
        g = 3, 5	✅ Also common, but still needs verification
        g = h^2 mod p	✅ Safer, avoids weak generators
        So, instead of blindly using g = 2, we should compute g based on p for better security! 🔥
         */
        // val g = BigInteger("2") // Use 2 as generator
        val g = findGenerator(p)


        val params = ElGamalParameters(p, g)

        val keyGenParams = ElGamalKeyGenerationParameters(random, params)
        val keyGen = ElGamalKeyPairGenerator()
        keyGen.init(keyGenParams)

        val keyPair = keyGen.generateKeyPair()
        val publicKey = keyPair.public as ElGamalPublicKeyParameters
        val privateKey = keyPair.private as ElGamalPrivateKeyParameters

        return Pair(publicKey, privateKey)
    }

    fun encryptMessage(message: String, publicKey: ElGamalPublicKeyParameters): ByteArray {
        val engine: AsymmetricBlockCipher = ElGamalEngine()
        engine.init(true, ParametersWithRandom(publicKey, SecureRandom()))

        val messageBytes = message.toByteArray()
        val plaintextBigInt = BigInteger(1, messageBytes) // Convert message to BigInteger

        return engine.processBlock(plaintextBigInt.toByteArray(), 0, plaintextBigInt.toByteArray().size)
    }


    fun decryptMessage(encryptedData: ByteArray, privateKey: ElGamalPrivateKeyParameters): String {
        val engine: AsymmetricBlockCipher = ElGamalEngine()
        engine.init(false, privateKey)

        val decryptedBytes = engine.processBlock(encryptedData, 0, encryptedData.size)
        return String(decryptedBytes)
    }


    fun findGenerator(p: BigInteger): BigInteger {
        val q = p.subtract(BigInteger.ONE).divide(BigIntegerTWO) // q = (p-1)/2
        val random = SecureRandom()

        while (true) {
            val h = BigInteger(p.bitLength(), random).mod(p.subtract(BigIntegerTWO)).add(
                BigIntegerTWO
            ) // 2 ≤ h ≤ p-2
            val g = h.modPow(BigIntegerTWO, p) // g = h^2 mod p
            if (g != BigInteger.ONE) return g
        }
    }
}
