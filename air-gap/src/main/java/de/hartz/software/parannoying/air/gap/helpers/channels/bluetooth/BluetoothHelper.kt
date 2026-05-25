package de.hartz.software.parannoying.air.gap.helpers.channels.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.legacy.app.ActivityCompat
import java.nio.charset.StandardCharsets
import java.util.*


@SuppressLint(*["StaticFieldLeak", "MissingPermission"]) // As activity is hold here but will be cleaned in onDestroy
object BluetoothHelper {

    // Debugging
    val TAG = "BluetoothChatService"

    // Name for the SDP record when creating server socket
    val NAME_SECURE = "BluetoothChatSecure"
    val NAME_INSECURE = "BluetoothChatInsecure"

    // Unique UUID for this application
    // TODO: Should we use a list of uuids to make duplications impossilbe?
    val MY_UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")
    val MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")

    // Constants that indicate the current connection currentMode
    val STATE_NONE = 0       // we're doing nothing
    val STATE_LISTEN = 1     // now listening for incoming connections
    val STATE_CONNECTING = 2 // now initiating an outgoing connection
    val STATE_CONNECTED = 3  // now connected to a remote device

    // Message types sent from the BluetoothChatService Handler
    val MESSAGE_STATE_CHANGE = 1
    val MESSAGE_READ = 2
    val MESSAGE_WRITE = 3
    val MESSAGE_DEVICE_NAME = 4
    val MESSAGE_TOAST = 5

    // Key names received from the BluetoothChatService Handler
    val DEVICE_NAME = "device_name"
    val TOAST = "toast"

    val REQUEST_ENABLE_BT = 8125
    val REQUEST_CONNECT_DEVICE_SECURE = 1244

    fun requestBluetooth() {
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity!!.startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
            // Otherwise, setup the chat session
        }
    }

    @Synchronized
    fun initBluetoothService(activity: Activity, connectedCallbackActivity: ConnectedCallbackActivity) : BluetoothService {
        if (mChatService == null) {
            mChatService = BluetoothService(activity, BluetoothHandler(activity, connectedCallbackActivity))
            BluetoothHelper.activity = activity
        }
        return mChatService!!
    }

    fun isBluetoothActive(): Boolean {
        return mBluetoothAdapter.isEnabled()
    }

    fun isConnectedToDevice() : Boolean {
        return mChatService != null && (mChatService!!.currentMode == STATE_CONNECTED ||  mChatService!!.currentMode == STATE_CONNECTING)
    }

    private var mChatService: BluetoothService? = null
    private var activity: Activity? = null

    private val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    fun isSupported() : Boolean {
        return mBluetoothAdapter != null
    }

    fun onDestroy() {
        if (mChatService != null) {
            mChatService!!.stop()
            mChatService = null
        }
        if (activity != null) {
            activity = null
        }
    }

    fun onResume() {
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService!!.currentMode == STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService!!.start()
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) : Boolean{
        if (requestCode == REQUEST_CONNECT_DEVICE_SECURE) {
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data!!)
                return true
            }
        }
        return false
    }

    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    fun ensureDiscoverable() {
        val REQUEST_ACCESS_COARSE_LOCATION = 1233
        val context = activity!!
        // https://stackoverflow.com/a/36177638/8524651 // TODO: Permission check doesnt work right now.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  // Only ask for these permissions on runtime when running Android 6.0 or higher
            when (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                PackageManager.PERMISSION_DENIED -> {
                    (AlertDialog.Builder(context)
                            .setTitle("Runtime Permissions up ahead")
                            .setMessage(Html.fromHtml("<p>To find nearby bluetooth devices please click \"Allow\" on the runtime permissions popup.</p>" + "<p>For more info see <a href=\"http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id\">here</a>.</p>"))
                            .setNeutralButton("Okay", DialogInterface.OnClickListener { dialog, which ->
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(context,
                                            arrayOf<String>(Manifest.permission.ACCESS_COARSE_LOCATION),
                                            REQUEST_ACCESS_COARSE_LOCATION)
                                }
                            })
                            .setNegativeButton("Cancel") { _, _ ->}
                            .show().findViewById<TextView>(android.R.id.message) as TextView).movementMethod = LinkMovementMethod.getInstance()       // Make the link clickable. Needs to be called after show(), in order to generate hyperlinks
                }
                PackageManager.PERMISSION_GRANTED -> {
                    discover()
                }
            }
        }
    }

    private fun discover() {
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            activity!!.startActivity(discoverableIntent)
        }
    }

    /**
     * Establish connection with other device
     *
     * @param data   An [Intent] with [DeviceListActivity.EXTRA_DEVICE_ADDRESS] extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    fun connectDevice(data: Intent, secure: Boolean = false) {
        // TODO: maybe secure connection is a problem for

        // Get the device MAC address
        val address = data.extras!!
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS)
        // Get the BluetoothDevice object
        val device = mBluetoothAdapter.getRemoteDevice(address)
        // Attempt to connect to the device
        mChatService!!.connect(device, secure)
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    fun sendMessage(message: String, callback: ConnectedCallbackActivity) {
        // Check that we're actually connected before trying anything
        if (mChatService!!.currentMode != STATE_CONNECTED) {
            Toast.makeText(activity, "Not connected", Toast.LENGTH_SHORT).show()
            return
        }

        // Check that there's actually something to send
        if (message.isNotEmpty()) {
            // Get the message bytes and tell the BluetoothChatService to write
            val send = message.toByteArray(StandardCharsets.UTF_8)
            Log.e(javaClass.simpleName, "Send Message: " + message + " as bytes with size" + send.size)

            mChatService!!.write(message)
            callback.onBluetoothMessageReceived(message)
        }
    }

    fun startDiscoverActivity () {
        if (!isBluetoothActive()) {
            requestBluetooth()
            return
        }
        // Launch the DeviceListActivity to see devices and do scan
        val serverIntent = Intent(activity, DeviceListActivity::class.java)
        activity!!.startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE)
    }

}