package de.hartz.software.parannoying.air.gap.nfc.send


import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.os.Messenger
import android.util.Log
import de.hartz.software.parannoying.air.gap.nfc.helper.NFCDataHelper

class NfcHostApduService : HostApduService() {
    private val TAG = "NfcHostApduService"

    companion object {

        fun initTest() {
            messageQueue.addAll(mutableListOf(
                "Very long string 1 ...".repeat(1000),
                "Very long string 2 ...".repeat(1000)
            ))
        }

        fun reset() {
            finish = false
            messageQueue.clear()
            currentChunkIndex = 0
        }

        fun finish() {
            finish = true
        }

        var messenger: Messenger? = null  // Reference to UI messenger
        val messageQueue = ArrayDeque<String>()
        var currentMessage: String? = null
        var finish = false

        private var currentChunkIndex = 0
    }

    /*
    Returns
      SW1 SW2
       90 00	Erfolg (APDU-Befehl wurde korrekt verarbeitet)
       61 XX	Weitere Daten sind verfügbar (XX gibt die Anzahl der verbleibenden Bytes an)
       6A 82	Datei oder Anwendung nicht gefunden
       6A 86	Falscher Parameter (P1/P2 ungültig)
       6D 00	Falscher INS-Code (Befehl nicht unterstützt)
       6E 00	Falsche CLA (Klasse des Kommandos nicht erkannt)
       67 00	Falsche Länge (Lc oder Le)
       69 85	Befehl nicht erlaubt (Sicherheitsrichtlinie verletzt)
    */
    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) return NFCDataHelper.STATUS_ERROR

        val commandHex = commandApdu.joinToString(" ") { "%02X".format(it) }
        Log.d("HCE", "Received APDU: $commandHex")

        return when {
            commandApdu.contentEquals(NFCDataHelper.SELECT_APDU) -> {
                Log.d("HCE", "SELECT AID received, resetting state.")
                sendNextChunk()
            }

            commandApdu.contentEquals(NFCDataHelper.REQUEST_NEXT_CHUNK) -> sendNextChunk()

            finish -> NFCDataHelper.STATUS_SUCCESS

            else -> NFCDataHelper.STATUS_ERROR
        }
    }

    private fun sendNextChunk(): ByteArray {
        if (messageQueue.isEmpty()) return NFCDataHelper.STATUS_IDLE

        if (currentMessage == null) {
            currentMessage = messageQueue.removeFirstOrNull()
        }

        val message = currentMessage!!
        val start = currentChunkIndex * NFCDataHelper.CHUNK_SIZE
        val end = minOf((currentChunkIndex + 1) * NFCDataHelper.CHUNK_SIZE, message.length)
        val chunk = message.substring(start, end).toByteArray(Charsets.UTF_8)

        val isLastChunk = end >= message.length
        if (isLastChunk) {
            currentChunkIndex = 0
        } else {
            currentChunkIndex++
        }

        return chunk + if (isLastChunk) NFCDataHelper.STATUS_FINISHED_MESSAGE else NFCDataHelper.STATUS_CONTINUE
    }

    override fun onDeactivated(reason: Int) {
        Log.d("HCE", "HCE service deactivated: $reason")
    }
}