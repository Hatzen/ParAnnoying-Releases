package de.hartz.software.parannoying.air.gap.nfc.send

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.nfc.NfcManager
import android.nfc.cardemulation.CardEmulation
import android.os.IBinder
import android.util.Log

// TODO: Add connection status
// TODO: Add display lock?
object NFCSender {


    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            // TODO: Connect and disconnect notice..
        }

        override fun onServiceDisconnected(name: ComponentName) {
        }
    }


    fun startNFC(activity: Activity) {
        val nfcManager = activity.getSystemService(Context.NFC_SERVICE) as NfcManager
        val nfcAdapter = nfcManager.defaultAdapter
        val cardEmulation = CardEmulation.getInstance(nfcAdapter)
        val component = ComponentName(activity, NfcHostApduService::class.java)

        if (nfcAdapter == null) {
            throw RuntimeException("Failed to start nfc sender")
        }

        // TODO: Do we need to have this? Evaluate..
        if (!cardEmulation.isDefaultServiceForCategory(component, CardEmulation.CATEGORY_OTHER)) {
            Log.e(javaClass.simpleName, "Seems like NFC card emulation will not work..")
            val intent = Intent(CardEmulation.ACTION_CHANGE_DEFAULT)
            intent.putExtra(CardEmulation.EXTRA_CATEGORY, CardEmulation.CATEGORY_OTHER)
            intent.putExtra(CardEmulation.EXTRA_SERVICE_COMPONENT, component)
            activity.startActivity(intent)
            return
        }

        // TODO: Remove android will start this service. remove
        val intent = Intent(activity, NfcHostApduService::class.java)
        activity.bindService(
            intent, serviceConnection,
            Context.BIND_AUTO_CREATE
        )
        activity.startService(intent)
    }

    fun sendMessage(message: String) {
        NfcHostApduService.messageQueue.add(message)
    }
}