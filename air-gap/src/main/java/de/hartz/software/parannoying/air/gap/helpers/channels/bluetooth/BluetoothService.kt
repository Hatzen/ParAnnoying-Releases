package de.hartz.software.parannoying.air.gap.helpers.channels.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.provider.Settings.Global.DEVICE_NAME
import android.util.Log
import de.hartz.software.parannoying.air.gap.helpers.channels.bluetooth.BluetoothHelper.MESSAGE_DEVICE_NAME
import de.hartz.software.parannoying.air.gap.helpers.channels.bluetooth.BluetoothHelper.MESSAGE_READ
import de.hartz.software.parannoying.air.gap.helpers.channels.bluetooth.BluetoothHelper.MESSAGE_STATE_CHANGE
import de.hartz.software.parannoying.air.gap.helpers.channels.bluetooth.BluetoothHelper.MESSAGE_TOAST
import de.hartz.software.parannoying.air.gap.helpers.channels.bluetooth.BluetoothHelper.MESSAGE_WRITE
import de.hartz.software.parannoying.air.gap.helpers.channels.bluetooth.BluetoothHelper.MY_UUID_INSECURE
import de.hartz.software.parannoying.air.gap.helpers.channels.bluetooth.BluetoothHelper.MY_UUID_SECURE
import de.hartz.software.parannoying.air.gap.helpers.channels.bluetooth.BluetoothHelper.NAME_INSECURE
import de.hartz.software.parannoying.air.gap.helpers.channels.bluetooth.BluetoothHelper.NAME_SECURE
import de.hartz.software.parannoying.air.gap.helpers.channels.bluetooth.BluetoothHelper.STATE_LISTEN
import de.hartz.software.parannoying.air.gap.helpers.channels.bluetooth.BluetoothHelper.TOAST
import de.hartz.software.parannoying.core.helper.security.SecureStreamDeserialisation
import java.io.IOException
import java.io.InputStream
import java.io.ObjectOutputStream
import java.io.OutputStream


/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
// https://github.com/googlesamples/android-BluetoothChat/tree/master/Application/src/main/java/com/example/android/bluetoothchat


/**
 * Constructor. Prepares a new BluetoothChat session.
 *
 * @param context The UI Activity Context
 * @param handler A Handler to send messages back to the UI Activity
 */
@SuppressLint("MissingPermission")
class BluetoothService(context: Context, val mHandler: Handler) {

    // Member fields
    private val mAdapter: BluetoothAdapter
    private var mSecureAcceptThread: AcceptThread? = null
    private var mInsecureAcceptThread: AcceptThread? = null
    private var mConnectThread: ConnectThread? = null
    private var mConnectedThread: ConnectedThread? = null
    /**
     * Return the current connection currentMode.
     */
    @get:Synchronized
    var currentMode: Int = 0
        private set
    private var mNewState: Int = 0

    init {
        /*
        // TODO: Nullpointer on SDK 29, probably missing bluetooth permissions?
        java.lang.NullPointerException: getDefaultAdapter() must not be null
                                                                                                    	at de.hartz.software.parannoying.air.gap.helpers.channels.bluetooth.BluetoothService.<init>(BluetoothService.kt:64)
                                                                                                    	at de.hartz.software.parannoying.air.gap.helpers.channels.bluetooth.BluetoothHelper.initBluetoothService(BluetoothHelper.kt:71)
                                                                                                    	at de.hartz.software.parannoying.air.gap.fragments.exchange.send.BluetoothSendChannelFragment.init(BluetoothSendChannelFragment.kt:64)
                                                                                                    	at de.hartz.software.parannoying.air.gap.fragments.exchange.AbstractExchangeChannelFragment.onViewCreated(AbstractExchangeChannelFragment.kt:63)
                                                                                                    	at androidx.fragment.app.Fragment.performViewCreated(Fragment.java:3128)
                                                                                                    	at androidx.fragment.app.FragmentStateManager.createView(FragmentStateManager.java:552)
                                                                                                    	at androidx.fragment.app.FragmentStateManager.moveToExpectedState(FragmentStateManager.java:261)
                                                                                                    	at androidx.fragment.app.FragmentManager.executeOpsTogether(FragmentManager.java:1890)
                                                                                                    	at androidx.fragment.app.FragmentManager.removeRedundantOperationsAndExecute(FragmentManager.java:1808)
                                                                                                    	at androidx.fragment.app.FragmentManager.execPendingActions(FragmentManager.java:1751)
                                                                                                    	at androidx.fragment.app.FragmentManager$5.run(FragmentManager.java:538)
                                                                                                    	at android.os.Handler.handleCallback(Handler.java:883)
                                                                                                    	at android.os.Handler.dispatchMessage(Handler.java:100)
                                                                                                    	at android.os.Looper.loop(Looper.java:214)
                                                                                                    	at android.app.ActivityThread.main(ActivityThread.java:7356)
                                                                                                    	at java.lang.reflect.Method.invoke(Native Method)
                                                                                                    	at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:492)
                                                                                                    	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:930)
2024-01-30 20:01:03.516 31065-31260 CrashReportHandler      de.hartz.software.parannoying.dev    E  Exception not handled.
2024-01-30 20:01:03.517 31065-31260 ACRA                    de.hartz.software.parannoying.dev    D  Sent report using de.hartz.software.parannoying.online.helper.crash.CrashReportHandler
2024-01-30 20:01:03.517 31065-31260 ACRA                    de.hartz.software.parannoying.dev    D  Report was sent by all senders
---------------------------- PROCESS ENDED (31083) for package de.hartz.software.parannoying.dev ----------------------------
2024-01-30 20:01:03.517 31065-31260 ACRA                    de.hartz.software.parannoying.dev    D  Finished sending reports from SenderService
2024-01-30 20:01:03.563  2046-2141  InputDispatcher         system_server                        E  channel '7f71626 de.hartz.software.parannoying.dev/de.hartz.software.parannoying.air.gap.activities.SendActivity (server)' ~ Channel is unrecoverably broken and will be disposed!
2024-01-30 20:01:03.563  2046-2141  InputDispatcher         system_server                        E  channel 'e8d9a85 de.hartz.software.parannoying.dev/de.hartz.software.parannoying.air.gap.activities.dummy.DummyAirGapActivity (server)' ~ Channel is unrecoverably broken and will be disposed!
---------------------------- PROCESS STARTED (31261) for package de.hartz.software.parannoying.dev ----------------------------
         */
        mAdapter = BluetoothAdapter.getDefaultAdapter()
        currentMode = BluetoothHelper.STATE_NONE
        mNewState = currentMode
    }

    /**
     * Update UI title according to the current currentMode of the chat connection
     */
    @Synchronized
    private fun updateUserInterfaceTitle() {
        currentMode = currentMode
        Log.d(BluetoothHelper.TAG, "updateUserInterfaceTitle() $mNewState -> $currentMode")
        mNewState = currentMode

        // Give the new currentMode to the Handler so the UI Activity can update
        mHandler.obtainMessage(MESSAGE_STATE_CHANGE, mNewState, -1).sendToTarget()
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    @Synchronized
    fun start() {
        Log.d(BluetoothHelper.TAG, "start")

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = AcceptThread(true)
            mSecureAcceptThread!!.start()
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = AcceptThread(false)
            mInsecureAcceptThread!!.start()
        }
        // Update UI title
        updateUserInterfaceTitle()
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    @Synchronized
    fun connect(device: BluetoothDevice, secure: Boolean) {
        Log.d(BluetoothHelper.TAG, "connect to: $device")

        // Cancel any thread attempting to make a connection
        if (currentMode == BluetoothHelper.STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread!!.cancel()
                mConnectThread = null
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        // Start the thread to connect with the given device
        mConnectThread = ConnectThread(device, secure)
        mConnectThread!!.start()
        // Update UI title
        updateUserInterfaceTitle()
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    @Synchronized
    private fun connected(socket: BluetoothSocket, device: BluetoothDevice, socketType: String) {
        Log.d(BluetoothHelper.TAG, "connected, Socket Type:$socketType")

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread!!.cancel()
            mSecureAcceptThread = null
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread!!.cancel()
            mInsecureAcceptThread = null
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = ConnectedThread(socket, socketType)
        mConnectedThread!!.start()

        // Send the name of the connected device back to the UI Activity
        val msg = mHandler.obtainMessage(MESSAGE_DEVICE_NAME)
        val bundle = Bundle()
        bundle.putString(DEVICE_NAME, device.name)
        msg.data = bundle
        mHandler.sendMessage(msg)
        // Update UI title
        updateUserInterfaceTitle()
    }

    /**
     * Stop all threads
     */
    @Synchronized
    fun stop() {
        Log.d(BluetoothHelper.TAG, "stop")

        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }

        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread!!.cancel()
            mSecureAcceptThread = null
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread!!.cancel()
            mInsecureAcceptThread = null
        }
        currentMode = BluetoothHelper.STATE_NONE
        // Update UI title
        updateUserInterfaceTitle()
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread.write
     */
    fun write(out: String) {
        // Create temporary object
        val r: ConnectedThread?
        // Synchronize a copy of the ConnectedThread
        synchronized(this) {
            if (currentMode != BluetoothHelper.STATE_CONNECTED) return
            r = mConnectedThread
        }
        // Perform the write unsynchronized
        r!!.write(out)
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private fun connectionFailed() {
        // Send a failure message back to the Activity
        val msg = mHandler.obtainMessage(MESSAGE_TOAST)
        val bundle = Bundle()
        bundle.putString(TOAST, "Unable to connect device")
        msg.data = bundle
        mHandler.sendMessage(msg)

        currentMode = BluetoothHelper.STATE_NONE
        // Update UI title
        updateUserInterfaceTitle()

        // Start the service over to restart listening mode
        this.start()
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private fun connectionLost() {
        // Send a failure message back to the Activity
        val msg = mHandler.obtainMessage(MESSAGE_TOAST)
        val bundle = Bundle()
        bundle.putString(TOAST, "Device connection was lost")
        msg.data = bundle
        mHandler.sendMessage(msg)

        currentMode = BluetoothHelper.STATE_NONE
        // Update UI title
        updateUserInterfaceTitle()

        // Start the service over to restart listening mode
        this.start()
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private inner class AcceptThread(secure: Boolean) : Thread() {
        // The local server socket
        private val mmServerSocket: BluetoothServerSocket?
        private val mSocketType: String

        init {
            var tmp: BluetoothServerSocket? = null
            mSocketType = if (secure) "Secure" else "Insecure"

            // Create a new listening server socket
            try {
                if (secure) {
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
                            MY_UUID_SECURE)
                } else {
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
                            NAME_INSECURE, MY_UUID_INSECURE)
                }
            } catch (e: IOException) {
                Log.e(BluetoothHelper.TAG, "Socket Type: " + mSocketType + "listen() failed", e)
            }

            mmServerSocket = tmp
            currentMode = STATE_LISTEN
        }

        override fun run() {
            Log.d(BluetoothHelper.TAG, "Socket Type: " + mSocketType +
                    "BEGIN mAcceptThread" + this)
            name = "AcceptThread$mSocketType"

            var socket: BluetoothSocket? = null

            // Listen to the server socket if we're not connected
            while (currentMode != BluetoothHelper.STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket?.accept()
                } catch (e: IOException) {
                    Log.d(BluetoothHelper.TAG, "Socket Type: " + mSocketType + "accept() failed", e)
                    break
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized(this) {
                        when (currentMode) {
                            BluetoothHelper.STATE_LISTEN, BluetoothHelper.STATE_CONNECTING ->
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.remoteDevice,
                                        mSocketType)
                            BluetoothHelper.STATE_NONE, BluetoothHelper.STATE_CONNECTED ->
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close()
                                } catch (e: IOException) {
                                    Log.e(BluetoothHelper.TAG, "Could not close unwanted socket", e)
                                }

                            else -> {}
                        }
                    }
                }
            }
            Log.i(BluetoothHelper.TAG, "END mAcceptThread, socket Type: $mSocketType")

        }

        fun cancel() {
            Log.d(BluetoothHelper.TAG, "Socket Type" + mSocketType + "cancel " + this)
            try {
                mmServerSocket?.close()
            } catch (e: IOException) {
                Log.e(BluetoothHelper.TAG, "Socket Type" + mSocketType + "close() of server failed", e)
            }

        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private inner class ConnectThread(private val mmDevice: BluetoothDevice, secure: Boolean) : Thread() {
        private val mmSocket: BluetoothSocket?
        private val mSocketType: String

        init {
            var tmp: BluetoothSocket? = null
            mSocketType = if (secure) "Secure" else "Insecure"

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (secure) {
                    tmp = mmDevice.createRfcommSocketToServiceRecord(
                            MY_UUID_SECURE)
                } else {
                    tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(
                            MY_UUID_INSECURE)
                }
            } catch (e: IOException) {
                Log.e(BluetoothHelper.TAG, "Socket Type: " + mSocketType + "create() failed", e)
            }

            mmSocket = tmp
            currentMode = BluetoothHelper.STATE_CONNECTING
        }

        override fun run() {
            Log.i(BluetoothHelper.TAG, "BEGIN mConnectThread SocketType:$mSocketType")
            name = "ConnectThread$mSocketType"

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery()

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket!!.connect()
            } catch (e: IOException) {
                // Close the socket
                try {
                    mmSocket!!.close()
                } catch (e2: IOException) {
                    Log.e(BluetoothHelper.TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2)
                }

                connectionFailed()
                return
            }

            // Reset the ConnectThread because we're done
            synchronized(this) {
                mConnectThread = null
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType)
        }

        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: IOException) {
                Log.e(BluetoothHelper.TAG, "close() of connect $mSocketType socket failed", e)
            }

        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private inner class ConnectedThread(private val mmSocket: BluetoothSocket, socketType: String) : Thread() {
        private val mmInStream: InputStream?
        private val mmOutStream: ObjectOutputStream?

        init {
            Log.d(BluetoothHelper.TAG, "create ConnectedThread: $socketType")
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = mmSocket.inputStream
                tmpOut = mmSocket.outputStream
            } catch (e: IOException) {
                Log.e(BluetoothHelper.TAG, "temp sockets not created", e)
            }

            mmInStream = tmpIn
            mmOutStream = ObjectOutputStream(tmpOut)
            currentMode = BluetoothHelper.STATE_CONNECTED
        }

        override fun run() {
            Log.i(BluetoothHelper.TAG, "BEGIN mConnectedThread")

            // Keep listening to the InputStream while connected
            while (currentMode == BluetoothHelper.STATE_CONNECTED) {
                try {
                    val message = SecureStreamDeserialisation.safeReadObject<String>(String::class.java, listOf(), mmInStream)

                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(MESSAGE_READ, -1, -1, message)
                            .sendToTarget()
                } catch (e: IOException) {
                    Log.e(BluetoothHelper.TAG, "disconnected", e)
                    connectionLost()
                    break
                }

            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        fun write(message: String) {
            try {
                mmOutStream!!.writeObject(message)
                mmOutStream.flush()
                // mmOutStream.reset() // TODO: Should not be needed for Strings as content does not change.

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(MESSAGE_WRITE, -1, -1, message)
                        .sendToTarget()
            } catch (e: IOException) {
                Log.e(BluetoothHelper.TAG, "Exception during write", e)
            }

        }

        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(BluetoothHelper.TAG, "close() of connect socket failed", e)
            }

        }
    }

}