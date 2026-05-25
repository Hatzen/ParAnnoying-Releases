package de.hartz.software.parannoying.air.gap.nfc.receive

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.NfcAdapter.ReaderCallback
import android.os.Bundle

object NFCReceiver {


    fun startNFC(activity: Activity, callback: ReaderCallback) {
        val options = Bundle().apply { putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250) }
        val flags = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
        // TODO: maybe add FLAG_READER_NO_PLATFORM_SOUNDS
        NfcAdapter.getDefaultAdapter(activity).enableReaderMode(activity, callback, flags, options)
    }

    fun stopNFC(activity: Activity) {
        NfcAdapter.getDefaultAdapter(activity).disableReaderMode(activity)
    }



}