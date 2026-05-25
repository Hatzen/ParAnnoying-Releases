package de.hartz.software.parannoying.air.gap.activities.dummy
import android.app.Activity
import android.os.Bundle
import android.widget.EditText
import de.hartz.software.parannoying.air.gap.nfc.NFCHandlerApi
import de.hartz.software.parannoying.air.gap.nfc.R
import de.hartz.software.parannoying.air.gap.nfc.interfaces.ReceivedMessageCallback
import java.text.SimpleDateFormat
import java.util.Date

class NfcReaderActivity : Activity(), ReceivedMessageCallback {

    lateinit var handler: NFCHandlerApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_nfc_reader)
        handler = NFCHandlerApi(this)
    }

    override fun onResume() {
        super.onResume()
        handler.receiveData(this)
        addLog("START READER")
    }

    override fun onPause() {
        super.onPause()
        addLog("STOP READER")
        handler.stopReceive()
    }

    private fun addLog(message: String) {
        runOnUiThread {
            val textView = findViewById<EditText>(R.id.messageLog)
            val time = SimpleDateFormat("HH:mm:ss").format(Date())
            textView.append("\n$time: $message")
        }
    }

    override fun receiveMessage(message: String) {
        addLog(message)
    }

    override fun disconnect() {

    }
}