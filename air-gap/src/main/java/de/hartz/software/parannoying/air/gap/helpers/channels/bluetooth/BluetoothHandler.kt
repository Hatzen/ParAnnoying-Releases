package de.hartz.software.parannoying.air.gap.helpers.channels.bluetooth

import android.app.Activity
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Toast
import de.hartz.software.parannoying.air.gap.helpers.channels.bluetooth.BluetoothHelper.MESSAGE_STATE_CHANGE
import de.hartz.software.parannoying.air.gap.helpers.channels.bluetooth.BluetoothHelper.STATE_CONNECTED
import de.hartz.software.parannoying.air.gap.helpers.channels.bluetooth.BluetoothHelper.STATE_CONNECTING
import de.hartz.software.parannoying.air.gap.helpers.channels.bluetooth.BluetoothHelper.STATE_LISTEN
import de.hartz.software.parannoying.air.gap.helpers.channels.bluetooth.BluetoothHelper.STATE_NONE
import de.hartz.software.parannoying.core.extensions.toast

interface ConnectedCallbackActivity {

    fun onBluetoothConnect()

    fun onBluetoothConnectionLost()

    fun onBluetoothMessageReceived(message: String)

}

class BluetoothHandler(val activity: Activity, val connectedCallbackActivity: ConnectedCallbackActivity) : Handler() {
    override fun handleMessage(msg: Message) {
        when (msg.what) {
            MESSAGE_STATE_CHANGE -> when (msg.arg1) {
                STATE_CONNECTED -> {
                    //setStatus("connect to " mConnectedDeviceName))
                    Log.e("BluetoothHandler", "STATE_CONNECTED")
                    activity.toast("STATE_CONNECTED")
                    connectedCallbackActivity.onBluetoothConnect()
                }
                STATE_CONNECTING -> {
                    // setStatus(de.hartz.software.parannoying.R.string.title_connecting)
                    Log.e("BluetoothHandler", "STATE_CONNECTING")
                    activity.toast("STATE_CONNECTING")
                }
                STATE_LISTEN, STATE_NONE -> {
                    // setStatus(de.hartz.software.parannoying.R.string.title_not_connected)
                    Log.e("BluetoothHandler", "STATE_LISTEN")
                    activity.toast("STATE_LISTEN")
                    connectedCallbackActivity.onBluetoothConnectionLost()
                }
            }
            BluetoothHelper.MESSAGE_WRITE -> {
                val writeMessage =  msg.obj as String
                // val writeBuf = msg.obj as ByteArray
                // construct a string from the buffer
                // val writeMessage = String(writeBuf)
                // mConversationArrayAdapter.add("Me:  $writeMessage")
                Log.e("BluetoothHandler", "MESSAGE_WRITE")
                activity.toast("MESSAGE_WRITE")
            }
            BluetoothHelper.MESSAGE_READ -> {
                val readMessage = msg.obj as String
                // val readBuf = msg.obj as ByteArray
                // construct a string from the valid bytes in the buffer
                // TODO: Why limit those bytes?
                // val readMessage = String(readBuf, 0, msg.arg1)
                //val readMessage = String(readBuf, StandardCharsets.UTF_8)
                // Log.e(javaClass.simpleName, "Received Message: " + readMessage + " as bytes with size" + readBuf.size + " and received size " +  msg.arg1)
                Log.e(javaClass.simpleName, "Received Message: " + readMessage)
                connectedCallbackActivity.onBluetoothMessageReceived(readMessage)
                activity.toast("MESSAGE_READ")
            }
            BluetoothHelper.MESSAGE_DEVICE_NAME -> {
                // save the connected device's name
                val mConnectedDeviceName = msg.getData().getString(BluetoothHelper.DEVICE_NAME)
                activity.toast("Connected to $mConnectedDeviceName")
                Log.e("BluetoothHandler", "MESSAGE_DEVICE_NAME")
                activity.toast("MESSAGE_DEVICE_NAME")
            }
            BluetoothHelper.MESSAGE_TOAST -> if (null != activity) {
                Toast.makeText(activity, msg.getData().getString(BluetoothHelper.TOAST),
                        Toast.LENGTH_SHORT).show()
            }
        }
    }
}