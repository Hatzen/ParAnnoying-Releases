package de.hartz.software.parannoying.air.gap.nfc.helper

import android.util.Base64
import kotlin.experimental.xor

object NFCDataHelper {

    /*
    Format eines SELECT_APDU-Befehls (ISO 7816-4):
    | CLA  | INS  | P1   | P2   | Lc   | AID... |
    ------------------------------------------------
    | 0x00 | 0xA4 | 0x04 | 0x00 | 0x07 | F0010203040506 |
    CLA (Class Byte) = 0x00 → Standard
    INS (Instruction Byte) = 0xA4 → Wählt eine App aus
    P1 (Parameter 1) = 0x04 → Sucht nach einer App-ID (AID)
    P2 (Parameter 2) = 0x00 → Normale Auswahl
    Lc (Length of Command Data) = 0x07 → Länge der AID (7 Bytes)
    AID (Application Identifier) = F0010203040506 → Die ID der App

    Die AID (Application Identifier) muss eindeutig sein
    Jede HCE-App wird über ihre AID identifiziert.
    Android-Geräte akzeptieren nur AIDs mit 5 bis 16 Bytes.
    Eine offiziell registrierte AID ist die beste Wahl (ISO/IEC 7816-5).
    Alternativ kannst du eine zufällige, nicht registrierte AID nutzen.

    Registrierte AIDs starten mit A0 oder einem zugewiesenen Präfix.
    Unregistrierte AIDs sollten mit F0 beginnen (für private Apps).

    private val SELECT_APDU = byteArrayOf(
        0x00, 0xA4.toByte(), 0x04, 0x00, 0x08,  // Standard APDU-Header
        0xF0.toByte(), 0x12, 0x34, 0x56, 0x78, 0x90, 0xAB, 0xCD // Eindeutige private AID
    )
    Beginnt mit F0, damit es als private AID erkannt wird.
    Zufällige Werte für mehr Sicherheit, aber innerhalb der 5-16-Byte-Regel.
    Falls du einen offiziellen AID-Block registrierst, kannst du stattdessen A0... verwenden.
     */
    val SELECT_APDU = byteArrayOf(
        0x00, 0xA4.toByte(), 0x04, 0x00, 0x07,
        0xF0.toByte(), 0x29, 0x57, 0x19, 0x67, 0x00, 0x36
    )
    // APDU Commands
    val SELECT_AID = byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00)  // Select HCE App
    val REQUEST_NEXT_CHUNK = byteArrayOf(0x00, 0xB0.toByte())     // Request next data chunk

    // APDU Response Status Codes
    val STATUS_SUCCESS = byteArrayOf(0x90.toByte(), 0x00.toByte()) // OK, end of transmission
    val STATUS_FINISHED_MESSAGE = byteArrayOf(0x90.toByte(), 0x01.toByte())  // Error
    val STATUS_CONTINUE = byteArrayOf(0x61.toByte(), 0x00.toByte()) // More data available
    val STATUS_IDLE = byteArrayOf(0x61.toByte(), 0xFF.toByte())  // Wait for more data.
    val STATUS_ERROR = byteArrayOf(0x6A.toByte(), 0x82.toByte())  // Error

    // Chunk size for message transfer
    const val CHUNK_SIZE = 200

    fun encodeDataForNFC(data: String): ByteArray {
        return Base64.encode(data.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }

    fun decodeDataFromNFC(encodedData: ByteArray): String {
        return String(Base64.decode(encodedData, Base64.NO_WRAP), Charsets.UTF_8)
    }

    fun encodeSecureData(data: String): ByteArray {
        val rawData = data.toByteArray(Charsets.UTF_8)

        // XOR Masking gegen Steuerzeichen
        val maskedData = rawData.map { it xor 0xAA.toByte() }.toByteArray()

        // Länge voranstellen, um falsche Interpretationen zu verhindern
        return byteArrayOf(maskedData.size.toByte()) + maskedData
    }

    fun decodeSecureData(encodedData: ByteArray): String {
        if (encodedData.isEmpty()) return ""

        // Länge extrahieren
        val length = encodedData[0].toInt() and 0xFF
        if (length != encodedData.size - 1) return ""

        // XOR Demasking
        val originalData = encodedData.drop(1).map { it xor 0xAA.toByte() }.toByteArray()

        return String(originalData, Charsets.UTF_8)
    }
}