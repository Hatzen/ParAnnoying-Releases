package de.hartz.software.parannoying.core.fragments

import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.usb.UsbManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import de.hartz.software.parannoying.core.R
import kotlin.random.Random

class ConnectionCheckStatusFragment : DialogFragment() {

    private lateinit var statusList: LinearLayout
    private lateinit var actionButton: Button
    private val handler = Handler(Looper.getMainLooper())

    // TODO: These checks are minimalistic, better unify with guard checks
    private val checks = listOf(
        "USB" to ::checkUsb,
        "WLAN" to ::checkWifi,
        "Bluetooth" to ::checkBluetooth,
        "NFC" to ::checkNfc,
        "GPS" to ::checkGps,
        "SIM" to ::checkSim,
        "Mobile Data" to ::checkMobileData
    )

    private val checkResults = mutableMapOf<String, Boolean?>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val root = LayoutInflater.from(context).inflate(R.layout.fragment_connection_check, null)
        statusList = root.findViewById(R.id.statusList)
        actionButton = root.findViewById(R.id.actionButton)
        root.findViewById<Button>(R.id.cancleButton).setOnClickListener {
            dismiss()
        }

        // Initialisiere Statuszeilen
        checks.forEach { (name, _) ->
            statusList.addView(makeLoadingView(name))
        }

        // Starte alle Prüfungen parallel
        checks.forEachIndexed { index, (name, checkFn) ->
            Thread {
                val result = try {
                    checkFn(requireContext())
                } catch (e: Exception) {
                    null
                }
                checkResults[name] = result
                handler.post {
                    updateStatusView(index, name, result)
                    updateActionButton()
                }
            }.start()
        }

        actionButton.setOnClickListener {
            val active = checkResults.filterValues { it == true }.keys
            when {
                active.size == 1 -> openSettingsFor(active.first())
                active.isEmpty() -> openAirplaneModeSettings()
                else -> openAirplaneModeSettings()
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(root)
            .create()
    }

    private fun makeLoadingView(name: String): TextView {
        val textView = TextView(requireContext())
        textView.text = "• checking $name"
        textView.setTextColor(Color.GRAY)
        animateEllipses(textView, name)
        return textView
    }

    private fun updateStatusView(index: Int, name: String, result: Boolean?) {
        val view = statusList.getChildAt(index) as TextView
        val statusText = when (result) {
            true -> "• $name activated"
            false -> "• $name deactivated"
            null -> "• $name error"
        }
        val color = when (result) {
            true -> Color.RED
            false -> Color.GREEN
            null -> Color.GRAY
        }
        view.text = statusText
        view.setTextColor(color)
    }

    private fun animateEllipses(textView: TextView, label: String) {
        var dots = 0
        val base = "• checking $label"
        handler.post(object : Runnable {
            override fun run() {
                if (checkResults.containsKey(label)) return
                dots = (dots + 1) % 4
                textView.text = base + ".".repeat(dots)
                handler.postDelayed(this, 500)
            }
        })
    }

    private fun updateActionButton() {
        val active = checkResults.filterValues { it == true }.keys
        actionButton.text = when {
            active.size == 1 -> "${active.first()} show settings to deactivate"
            active.isEmpty() -> "Enable airplanemode"
            else -> "Enable airplanemode"
        }
    }

    // ----------- Prüffunktionen -----------

    private fun checkUsb(context: Context): Boolean? {
        Thread.sleep(Random.nextLong(500,1500))
        return try {
            val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
            manager.deviceList.isNotEmpty()
        } catch (e: Exception) { null }
    }

    private fun checkWifi(context: Context): Boolean? {
        Thread.sleep(Random.nextLong(500,1500))
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        return info?.isConnected
    }

    private fun checkBluetooth(context: Context): Boolean? {
        Thread.sleep(Random.nextLong(500,1500))
        val adapter = BluetoothAdapter.getDefaultAdapter()
        return adapter?.isEnabled
    }

    private fun checkNfc(context: Context): Boolean? {
        Thread.sleep(Random.nextLong(500,1500))
        val adapter = NfcAdapter.getDefaultAdapter(context)
        return adapter?.isEnabled
    }

    private fun checkGps(context: Context): Boolean? {
        Thread.sleep(Random.nextLong(500,1500))
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun checkSim(context: Context): Boolean? {
        Thread.sleep(Random.nextLong(500,1500))
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tm.simState == TelephonyManager.SIM_STATE_READY
    }

    private fun checkMobileData(context: Context): Boolean? {
        Thread.sleep(Random.nextLong(500,1500))
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        return info?.isConnected
    }

    // ----------- Aktionen -----------

    private fun openAirplaneModeSettings() {
        startActivity(Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS))
    }

    private fun openSettingsFor(name: String) {
        val intent = when (name) {
            "WLAN" -> Intent(Settings.ACTION_WIFI_SETTINGS)
            "Bluetooth" -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            "NFC" -> Intent(Settings.ACTION_NFC_SETTINGS)
            "GPS" -> Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            "Mobile Data" -> Intent(Settings.ACTION_DATA_ROAMING_SETTINGS)
            "SIM" -> Intent(Settings.ACTION_WIRELESS_SETTINGS)
            "USB" -> Intent(Settings.ACTION_SETTINGS) // keine direkte USB-Seite
            else -> Intent(Settings.ACTION_SETTINGS)
        }
        startActivity(intent)
    }
}