// AirgapKeyExchange.kt  – v2
// Hybrid, post‑quantum‑ready key exchange for two air‑gapped devices
// ------------------------------------------------------------------
// • Layer 1  (classical): X25519 ECDH
// • Layer 2 (post‑quantum): Kyber768 KEM  (BCPQC provider ≥ 1.78)
// • The two shared secrets are HKDF‑mixed → a single 256‑bit AES/GCM key.
// • Each exchange message carries *both* public keys plus a short fingerprint token.
// • A stable 32‑byte Session‑ID (SHA‑256 of sorted public keys) lets you
//   look up the correct shared secret even when many interleaved sessions
//   exist simultaneously.
//
// Gradle deps:
// implementation("org.bouncycastle:bcprov-jdk18on:1.78")  // classical + PQC primitives
// implementation("org.bouncycastle:bcpqc-jdk18on:1.78")   // post‑quantum provider
//
// NOTE:  Compile‑time PQC APIs in BC are still marked “experimental”.  This sample
// focuses on *concept & API usage* – tweak parameter specs to match the BC
// version you ship.

// import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider   // FIPS ok too
import org.bouncycastle.crypto.digests.Blake2sDigest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.params.HKDFParameters
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Security
import java.util.Base64
import javax.crypto.KeyAgreement
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object AirgapKeyExchange {
    // ────────────────────────────────────────────────────────────────────────────
    // Provider install – make sure it happens exactly once per process
    // ────────────────────────────────────────────────────────────────────────────
    init {
        Security.addProvider(BouncyCastleProvider())       // classical
        Security.addProvider(BouncyCastlePQCProvider())    // post‑quantum
    }

    // ╔══════════════════════════════════════════════════════════════════════╗
    // ║ 𝗗𝗮𝘁𝗮 𝗰𝗹𝗮𝘀𝘀𝗲𝘀                                                      ║
    // ╚══════════════════════════════════════════════════════════════════════╝
    data class ExchangeMessage(
        val x25519Pub: String,   // Base64(X509)
        val kyberPub: String,    // Base64(X509)
        val token: String        // 20‑char printable fingerprint
    )

    data class Result(
        val sessionId: ByteArray,    // 32‑byte SHA‑256 of sorted pubs → DB key
        val symmetricKey: SecretKey, // final 256‑bit AES key
        val publicMessage: ExchangeMessage
    )

    // ╔════════════════════════════════════════════════════╗
    // ║ 𝟭. Key‑pair generators                             ║
    // ╚════════════════════════════════════════════════════╝
    private fun genX25519(): KeyPair =
        KeyPairGenerator.getInstance("X25519", "BC").generateKeyPair()

    private fun genKyber(): KeyPair =
        KeyPairGenerator.getInstance("Kyber", "BCPQC").apply {
            // Optionally: initialize(KyberParameterSpec.kyber768, SecureRandom())
        }.generateKeyPair()

    // ╔════════════════════════════════════════════════════╗
    // ║ 𝟮. Shared‑secret computation                       ║
    // ╚════════════════════════════════════════════════════╝
    private fun sharedX25519(ownPriv: PrivateKey, peerPub: PublicKey): ByteArray {
        val ka = KeyAgreement.getInstance("X25519", "BC")
        ka.init(ownPriv)
        ka.doPhase(peerPub, true)
        return ka.generateSecret()
    }

    private fun sharedKyber(ownPriv: PrivateKey, peerPub: PublicKey): ByteArray {
        val ka = KeyAgreement.getInstance("Kyber", "BCPQC")
        ka.init(ownPriv)
        ka.doPhase(peerPub, true)
        return ka.generateSecret()
    }

    // ╔════════════════════════════════════════════════════╗
    // ║ 𝟯. Symmetric key derivation (HKDF‑Blake2s)         ║
    // ╚════════════════════════════════════════════════════╝
    private fun deriveSymKey(classical: ByteArray, pqc: ByteArray, token: ByteArray): SecretKey {
        val hkdf = HKDFBytesGenerator(Blake2sDigest())
        val concat = classical + pqc                        // 64‑byte input entropy
        hkdf.init(HKDFParameters(concat, token, "Airgap-Hybrid-KDF".toByteArray()))
        val out = ByteArray(32)
        hkdf.generateBytes(out, 0, 32)                     // 256‑bit key
        return SecretKeySpec(out, "AES")
    }

    // ╔════════════════════════════════════════════════════╗
    // ║ 𝟰. Utility helpers                                 ║
    // ╚════════════════════════════════════════════════════╝
    private fun b64(bytes: ByteArray) = Base64.getEncoder().encodeToString(bytes)
    private fun b64ToBytes(s: String) = Base64.getDecoder().decode(s)

    private fun parsePubX25519(s: String): PublicKey =
        KeyFactory.getInstance("X25519", "BC")
            .generatePublic(java.security.spec.X509EncodedKeySpec(b64ToBytes(s)))

    private fun parsePubKyber(s: String): PublicKey =
        KeyFactory.getInstance("Kyber", "BCPQC")
            .generatePublic(java.security.spec.X509EncodedKeySpec(b64ToBytes(s)))

    // ╔════════════════════════════════════════════════════╗
    // ║ 𝟱. Fingerprint & Session‑ID helpers                ║
    // ╚════════════════════════════════════════════════════╝
    private fun fingerprint(vararg data: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(data.reduce(ByteArray::plus))

    private fun shortToken(hash: ByteArray) = b64(hash).take(20)

    // ╔════════════════════════════════════════════════════╗
    // ║ 𝟲. Public message creation                         ║
    // ╚════════════════════════════════════════════════════╝
    fun createExchangeMessage(xPair: KeyPair, kPair: KeyPair, deviceId: String): ExchangeMessage {
        val tokenBytes = fingerprint(deviceId.toByteArray(), xPair.public.encoded, kPair.public.encoded)
        return ExchangeMessage(
            x25519Pub = b64(xPair.public.encoded),
            kyberPub  = b64(kPair.public.encoded),
            token     = shortToken(tokenBytes)
        )
    }

    // ╔════════════════════════════════════════════════════╗
    // ║ 𝟟. Master builder – called after peer message       ║
    // ╚════════════════════════════════════════════════════╝
    fun buildResult(
        myDeviceId: String,
        myX: KeyPair,
        myK: KeyPair,
        peerMsg: ExchangeMessage
    ): Result {
        // ❶ compute secrets
        val secX = sharedX25519(myX.private, parsePubX25519(peerMsg.x25519Pub))
        val secK = sharedKyber(myK.private,  parsePubKyber(peerMsg.kyberPub))

        // ❷ derive symmetric key
        val tokenBytes = fingerprint(myDeviceId.toByteArray(), myX.public.encoded, myK.public.encoded)
        val sym = deriveSymKey(secX, secK, tokenBytes)

        // ❸ session‑id (order‑independent) → map look‑ups
        val sessionId = fingerprint(
            *listOf(myX.public.encoded, parsePubX25519(peerMsg.x25519Pub).encoded).sortedWith(ByteArrayComparator).toTypedArray()
        )

        return Result(sessionId, sym, createExchangeMessage(myX, myK, myDeviceId))
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Comparator to keep session‑id deterministic regardless of who initiates.
    // ────────────────────────────────────────────────────────────────────────────
    private object ByteArrayComparator : Comparator<ByteArray> {
        override fun compare(a: ByteArray, b: ByteArray): Int {
            val len = minOf(a.size, b.size)
            for (i in 0 until len) {
                val diff = (a[i].toInt() and 0xff) - (b[i].toInt() and 0xff)
                if (diff != 0) return diff
            }
            return a.size - b.size
        }
    }

    // ╔════════════════════════════════════════════════════╗
    // ║ 𝟴. Demo main                                        ║
    // ╚════════════════════════════════════════════════════╝
    @JvmStatic
    fun main(args: Array<String>) {
        val devA = "Device‑A"
        val devB = "Device‑B"

        // A generates keys & message
        val aX = genX25519(); val aK = genKyber()
        val aMsg = createExchangeMessage(aX, aK, devA)

        // B generates keys & message
        val bX = genX25519(); val bK = genKyber()
        val bMsg = createExchangeMessage(bX, bK, devB)

        // Each side processes peer msg
        val aRes = buildResult(devA, aX, aK, bMsg)
        val bRes = buildResult(devB, bX, bK, aMsg)

        println("Symmetric keys equal : ${aRes.symmetricKey.encoded.contentEquals(bRes.symmetricKey.encoded)}")
        println("Session‑IDs equal   : ${aRes.sessionId.contentEquals(bRes.sessionId)}")
    }
}
