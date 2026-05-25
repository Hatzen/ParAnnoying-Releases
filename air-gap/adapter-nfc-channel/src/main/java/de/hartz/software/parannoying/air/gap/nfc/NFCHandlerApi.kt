package de.hartz.software.parannoying.air.gap.nfc

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Handler
import android.os.Messenger
import android.util.Log
import de.hartz.software.parannoying.air.gap.nfc.helper.NFCDataHelper
import de.hartz.software.parannoying.air.gap.nfc.interfaces.ReceivedMessageCallback
import de.hartz.software.parannoying.air.gap.nfc.interfaces.SendMessageCallback
import de.hartz.software.parannoying.air.gap.nfc.receive.NFCReceiver
import de.hartz.software.parannoying.air.gap.nfc.send.NFCSender
import de.hartz.software.parannoying.air.gap.nfc.send.NfcHostApduService
import java.io.IOException

class NFCHandlerApi(val activity: Activity): NfcAdapter.ReaderCallback {

    private val receivedMessages = mutableListOf<StringBuilder>()
    private var currentMessageIndex = 0

    var callback: ReceivedMessageCallback? = null
    var connected = false

    fun receiveData(callback: ReceivedMessageCallback) {
        NFCReceiver.startNFC(activity, this)
        this.callback = callback
        receivedMessages.clear()
        currentMessageIndex = 0
        receivedMessages.add(StringBuilder())
    }


    override fun onTagDiscovered(tag: Tag?) {
        connected = true
        Log.e(javaClass.simpleName, "handleNfcIntent")
        tag?.let {
            try {
                val tag = it
                handleTag(tag)
            } catch (e: IOException) {
                Log.e("NFC", "Error reading NFC tag", e)
            }
            finally {
                connected = false
            }
        }
    }

    private fun handleTag(tag: Tag) {
        val isoDep = IsoDep.get(tag)
        isoDep.connect()

        // Select the HCE Service (AID)
        val response = isoDep.transceive(NFCDataHelper.SELECT_APDU)
        Log.d("NFC", "AID Selection Response: ${response.toHex()}")


        // TODO: Does this freeze on mainthread?
        while (true) {
            val chunkResponse = isoDep.transceive(NFCDataHelper.REQUEST_NEXT_CHUNK)

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
            val statusCode = chunkResponse.takeLast(2).toByteArray()
            val dataChunk = chunkResponse.dropLast(2).toByteArray().toString(Charsets.UTF_8)

            receivedMessages[currentMessageIndex].append(dataChunk)

            if (statusCode.contentEquals(NFCDataHelper.STATUS_FINISHED_MESSAGE)) {
                val fullyReceivedMessage = receivedMessages[currentMessageIndex]
                Log.d("NFC", "Message fully received: ${fullyReceivedMessage}")
                currentMessageIndex++

                callback?.receiveMessage(fullyReceivedMessage.toString())
                receivedMessages.add(StringBuilder()) // Prepare for next message
            }

            if (statusCode.contentEquals(NFCDataHelper.STATUS_SUCCESS)) {
                Log.d("NFC", "Successfully delivered al messages.")
                break
            }
            if (statusCode.contentEquals(NFCDataHelper.STATUS_ERROR)) {
                Log.d("NFC", "No more messages.")
                break
            }
            if (statusCode.contentEquals(NFCDataHelper.STATUS_IDLE)) {
                Thread.sleep(500)
            }
        }

        isoDep.close()
    }

    fun stopReceive() {
        connected = false
        this.callback = null
        receivedMessages.clear()
        currentMessageIndex = 0
        NFCReceiver.stopNFC(activity)
    }

    fun startSending(callback: SendMessageCallback? = null) {
        NfcHostApduService.reset()

        NFCSender.startNFC(activity)
        val handler = Handler { msg ->
            val receivedApdu = msg.arg1 == 1
            Log.d("MainActivity", "Received APDU from HCE: $receivedApdu")
            callback?.messageSend()
            true
        }
        NfcHostApduService.messenger = Messenger(handler)
        connected = true
    }

    fun sendData(dataToSend: String) {
        NFCSender.sendMessage(dataToSend)
    }

    fun stopSendingData() {
        NfcHostApduService.messenger = null
        NfcHostApduService.finish()
        connected = false
    }

    fun isConnected(): Boolean {
        return connected
    }

    private fun ByteArray.toHex(): String = joinToString(" ") { "%02X".format(it) }
}