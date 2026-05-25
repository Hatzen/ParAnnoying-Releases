package de.hartz.software.parannoying.air.gap.nfc.helper

import android.content.Context
import android.nfc.NfcAdapter

object NFCHelper {

    // TODO: Yota3 has no NFC but this still returns true.. But seems to be device specific as emulator does recognize correctly.
    fun isNFCSupported(context: Context) : Boolean {
        if (getAdapter(context) !== null)
            return true
        return false
    }

    fun isEnabled (context: Context): Boolean {
        return getAdapter(context)!!.isEnabled
    }

    private fun getAdapter(context: Context) : NfcAdapter? {
        return NfcAdapter.getDefaultAdapter(context)
    }


}