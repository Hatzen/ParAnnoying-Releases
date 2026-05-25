package de.hartz.software.parannoying.air.gap.helpers.channels.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import de.hartz.software.parannoying.air.gap.R
import de.hartz.software.parannoying.core.extensions.Storage

/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the scanResult Intent.
 */
class DeviceListActivity : Activity() {


    companion object {

        /**
         * Tag for Log
         */
        private val TAG = "DeviceListActivity"

        /**
         * Return Intent extra
         */
        var EXTRA_DEVICE_ADDRESS = "device_address"
    }

    /**
     * Member fields
     */
    private var mBtAdapter: BluetoothAdapter? = null

    /**
     * Newly discovered devices
     */
    private var mNewDevicesArrayAdapter: ArrayAdapter<String>? = null

    /**
     * The on-click listener for all devices in the ListViews
     */
    private val mDeviceClickListener = AdapterView.OnItemClickListener { av, v, arg2, arg3 ->
        // Cancel discovery because it's costly and we're about to connect
        mBtAdapter!!.cancelDiscovery()

        // Get the device MAC address, which is the last 17 chars in the View
        val info = (v.findViewById<TextView>(R.id.text)).text.toString()
        if (info.isEmpty() || info == noDevices) {
            return@OnItemClickListener
        }

        val address = info.substring(info.length - 17)

        Storage.updateSettings {
            if (it.storeLastBluetoothMac) {
                it.storedBluetoothMac = address
            }
        }
        connect(address)
    }

    private fun connect(mac: String) {

        // Create the scanResult Intent and include the MAC address
        val intent = Intent()
        intent.putExtra(EXTRA_DEVICE_ADDRESS, mac)

        // Set scanResult and finish this Activity
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            // TODO: Does this get called? Doesnt look like
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED == action) {
                mNewDevicesArrayAdapter!!.clear()
            }

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND == action) {
                // Get the BluetoothDevice object from the Intent
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                // If it's already paired, skip it, because it's been listed already
                if (device!!.bondState != BluetoothDevice.BOND_BONDED) {
                    // Avoid duplicates and name as null.
                    var name = device.name
                    if (name == "null" || name == null) {
                        name = "-No Name-"
                    }

                    val settings = Storage.readSettings()
                    if (settings.storeLastBluetoothMac && settings.storedBluetoothMac == device.address) {
                        connect(device.address)
                        return
                    }

                    val value = name + "\n" + device.address
                    for (i in 0 until mNewDevicesArrayAdapter!!.count) {
                        if (mNewDevicesArrayAdapter?.getItem(i) == value) {
                            return
                        }
                    }
                    mNewDevicesArrayAdapter!!.add(value)
                    mNewDevicesArrayAdapter!!.notifyDataSetInvalidated()
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                findViewById<View>(R.id.loading).visibility = View.INVISIBLE
                if (mNewDevicesArrayAdapter!!.count == 0) {
                    mNewDevicesArrayAdapter!!.add(noDevices)
                }
            }
        }
    }

    private val noDevices = "No Devices"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup the window
        setContentView(R.layout.activity_device_list)

        var deviceRoleText = "offline"
        if (Storage.isOfflineDevice()) {
            deviceRoleText = "online"
        }
        findViewById<TextView>(R.id.header).text = "Select your $deviceRoleText device"

        // Set scanResult CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED)

        // Initialize the button to perform device discovery
        val scanButton = findViewById<View>(R.id.button_scan) as Button
        scanButton.setOnClickListener { v ->
            doDiscovery()
            findViewById<View>(R.id.loading).visibility = View.VISIBLE
        }

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        val pairedDevicesArrayAdapter = ArrayAdapter<String>(this, R.layout.row_bluetooth_device, R.id.text)
        mNewDevicesArrayAdapter = ArrayAdapter(this, R.layout.row_bluetooth_device, R.id.text)

        // Find and set up the ListView for paired devices
        val pairedListView = findViewById<View>(R.id.paired_devices) as ListView
        pairedListView.adapter = pairedDevicesArrayAdapter
        pairedListView.onItemClickListener = mDeviceClickListener

        // Find and set up the ListView for newly discovered devices
        val newDevicesListView = findViewById<View>(R.id.new_devices) as ListView
        newDevicesListView.adapter = mNewDevicesArrayAdapter
        newDevicesListView.onItemClickListener = mDeviceClickListener
        newDevicesListView.emptyView = findViewById<View>(R.id.emptyList)

        // Register for broadcasts when a device is discovered
        var filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        this.registerReceiver(mReceiver, filter)

        // Register for broadcasts when discovery has finished
        filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        this.registerReceiver(mReceiver, filter)

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter()

        // Get a set of currently paired devices
        val pairedDevices = mBtAdapter!!.bondedDevices

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size > 0) {
            findViewById<View>(R.id.title_paired_devices).visibility = View.VISIBLE
            for (device in pairedDevices) {
                pairedDevicesArrayAdapter.add(device.name + "\n" + device.address)
            }
        } else {
            pairedDevicesArrayAdapter.add(noDevices)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Make sure we're not doing discovery anymore
        mBtAdapter?.cancelDiscovery()

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver)
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private fun doDiscovery() {
        Log.d(TAG, "doDiscovery()")

        // If we're already discovering, stop it
        if (mBtAdapter!!.isDiscovering) {
            mBtAdapter!!.cancelDiscovery()
        }

        // Request discover from BluetoothAdapter
        mBtAdapter!!.startDiscovery()
    }

}