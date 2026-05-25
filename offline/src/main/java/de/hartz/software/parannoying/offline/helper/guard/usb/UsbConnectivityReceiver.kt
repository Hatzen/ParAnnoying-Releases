package de.hartz.software.parannoying.offline.helper.guard.usb

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import de.hartz.software.parannoying.offline.helper.guard.ConnectionGuard

// https://stackoverflow.com/a/56556138/8524651
class UsbConnectivityReceiver() : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver") // We just check the connection state so it is ok, to be called more often.
    override fun onReceive(context: Context, intent: Intent) {
        ConnectionGuard.decideToKillApp(context)
        /*
        // Currently we dont need to differentiate the different tasks.
        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED ->
                mConnectivityReceiverListener.onUsbConnectionChanged(isConnected(context))
            Intent.ACTION_POWER_DISCONNECTED ->
                mConnectivityReceiverListener.onUsbConnectionChanged(isConnected(context))
            Intent.ACTION_BATTERY_CHANGED ->
                mConnectivityReceiverListener.onUsbConnectionChanged(isConnected(context))
        }
         */
    }

    companion object {

        fun hasConnection(context: Context): Boolean {
            // https://stackoverflow.com/a/10153677/8524651
            // TODO: Verify this is correct for all apis.. Are there smartphones without external sd card?
            val state: String = Environment.getExternalStorageState()
            if (Environment.MEDIA_SHARED == state) {
                // Sd card has connected to PC in MSC mode
                return true
            }
            return detectByCharging(context)
        }

        // https://stackoverflow.com/questions/19227464/detect-usb-connection-and-check-if-it-is-a-pc/19229024
        private fun detectByCharging (context: Context): Boolean {
            // https://developer.android.com/training/monitoring-device-state/battery-monitoring#DetermineChargeState
            val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus: Intent = context.registerReceiver(null, ifilter)!!

            // Are we charging / charged?
            val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            // How are we charging?
            val chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            val usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB

            return usbCharge
        }
    }
}