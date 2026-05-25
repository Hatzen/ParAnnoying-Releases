package de.hartz.software.parannoying.air.gap.activities.dummy

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import de.hartz.software.parannoying.air.gap.nfc.NFCHandlerApi
import de.hartz.software.parannoying.air.gap.nfc.R
import de.hartz.software.parannoying.air.gap.nfc.interfaces.SendMessageCallback
import java.text.SimpleDateFormat
import java.util.Date

class NfcSenderActivity : Activity(), SendMessageCallback {

    lateinit var handler: NFCHandlerApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_nfc_sender)
        handler = NFCHandlerApi(this)

        val messageInput = findViewById<EditText>(R.id.messageInput)
        val sendButton = findViewById<Button>(R.id.sendButton)
        val generateButton = findViewById<Button>(R.id.sendManyButton)

        sendButton.setOnClickListener {
            val message = messageInput.text.toString()
            if (message.isNotEmpty()) {
                handler.sendData(message)
                messageInput.text.clear()
            }
        }

        generateButton.setOnClickListener {
            Handler().post {
                for (i in 0..10) {
                    handler.sendData("Send message $i")
                    addLog("Message added: $i")
                    Thread.sleep(500)
                }
                for (i in 0..10) {
                    handler.sendData("Send message $i")
                    addLog("Message added: $i")
                }
            }
        }

        handler.startSending(this)
        addLog("Send start")
    }

    override fun onPause() {
        super.onPause()
        handler.stopSendingData()
    }

    private fun addLog(message: String) {
        runOnUiThread {
            val textView = findViewById<TextView>(R.id.messageLog)
            val time = SimpleDateFormat("HH:mm:ss").format(Date())
            textView.append("\n$time: $message")
        }
    }

    override fun messageSend() {
        TODO("Not yet implemented")
    }
}