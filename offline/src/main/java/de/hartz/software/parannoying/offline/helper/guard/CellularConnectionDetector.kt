package de.hartz.software.parannoying.offline.helper.guard

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoWcdma
import android.telephony.CellSignalStrength
import android.telephony.ServiceState
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.offline.model.OfflineStorage

class CellularConnectionDetector(val context: Context) {

    fun hasConnection (): Boolean {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return true // When no permission is granted handle as if it is connected.
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (telephonyManager.serviceState == null) {
                // TODO: For some reasons the service state is null on some emulators.
                UiHelper.showToastFromBackgroundTask(context, "CellularServiceState: is null.")
                return checkWorkaround()
            }
            val serviceState = telephonyManager.serviceState!!.state
            if(serviceState != ServiceState.STATE_POWER_OFF
                    && serviceState == ServiceState.STATE_OUT_OF_SERVICE) {
                if (OfflineStorage.INSTANCE.DEVELOPER_MODE) {
                    UiHelper.showToastFromBackgroundTask(context, "Parannyoing cleaned for cellular value: " + serviceState)
                }
                return true
            } else {
                if (OfflineStorage.INSTANCE.DEVELOPER_MODE) {
                    UiHelper.showToastFromBackgroundTask(context, "Not killed for cellular value: " + serviceState)
                }
            }
        } else {
            return checkWorkaround()
        }
        return false
    }

    private fun checkWorkaround (): Boolean {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            UiHelper.showToastFromBackgroundTask(context, "No fine location permission granted. ")
            return false
        }
        // Detect for pre Oreo devices in hacky way: https://stackoverflow.com/questions/66878643/no-virtual-method-getcellsignalstrength?noredirect=1#comment121514788_66878643
        val cellInfo = telephonyManager.allCellInfo

        val signalStrengths: MutableList<HashMap<String, Any?>> = ArrayList()
        for (cell in cellInfo) {
            val signalStrength: HashMap<String, Any?> = HashMap()
            val class_ = cell.javaClass.simpleName
            signalStrength["class"] = class_
            when (class_) {
                "CellInfoLte" -> {
                    val lteSS = (cell as CellInfoLte).cellSignalStrength
                    signalStrength["asu"] = lteSS.asuLevel
                    signalStrength["dbm"] = lteSS.dbm
                    signalStrength["level"] = lteSS.level
                }
                "CellInfoWcdma" -> {
                    val wcdmaSS = (cell as CellInfoWcdma).cellSignalStrength
                    signalStrength["asu"] = wcdmaSS.asuLevel
                    signalStrength["dbm"] = wcdmaSS.dbm
                    signalStrength["level"] = wcdmaSS.level
                }
                "CellInfoGsm" -> {
                    val gsmSS = (cell as CellInfoGsm).cellSignalStrength
                    signalStrength["asu"] = gsmSS.asuLevel
                    signalStrength["dbm"] = gsmSS.dbm
                    signalStrength["level"] = gsmSS.level
                }
                else -> {
                    signalStrength["asu"] = null
                    signalStrength["dbm"] = null
                    signalStrength["level"] = null
                }
            }
            signalStrengths.add(signalStrength)
        }
        if (signalStrengths.any { it["level"] != CellSignalStrength.SIGNAL_STRENGTH_NONE_OR_UNKNOWN }) {
            return true
        }
        return false
    }
}