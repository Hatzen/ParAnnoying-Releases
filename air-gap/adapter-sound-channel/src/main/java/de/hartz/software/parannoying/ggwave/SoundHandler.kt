package de.hartz.software.parannoying.ggwave

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import de.hartz.software.parannoying.ggwave.SoundHandlerApi.REQUEST_RECORD_AUDIO
import de.hartz.software.parannoying.ggwave.helper.CapturingThread
import de.hartz.software.parannoying.ggwave.helper.PlaybackThread
import de.hartz.software.parannoying.ggwave.interfaces.ReceivedMessageCallback
import de.hartz.software.parannoying.ggwave.interfaces.internal.AudioDataReceivedListener
import de.hartz.software.parannoying.ggwave.interfaces.internal.PlaybackListener
import de.hartz.software.parannoying.ggwave.model.ChannelOption
import java.nio.ByteBuffer



//TODO: why there is the last data missing? when receving... seVQUN5XWaVApIo=owpMIoO
class SoundChannelHandler {
    var mCapturingThread: CapturingThread? = null
    var mPlaybackThread: PlaybackThread? = null

    var callback: ReceivedMessageCallback? = null

    fun init() {
        System.loadLibrary("ggwave-jni")
        initNative()
    }

    // Native interface:
    private external fun initNative()
    external fun processCaptureData(data: ShortArray)
    external fun sendMessage(message: String, option: Int)

    fun sendMessage(message: String, option: ChannelOption) {
        sendMessage(message, option.value)
    }


    // Native callbacks:
    private fun onNativeReceivedMessage(c_message: ByteArray) {
        val message = String(c_message)
        Log.v("ggwave", "Received message: $message")

        callback?.receivedMessage(message)
        callback = null
    }

    private fun onNativeMessageEncoded(c_message: ShortArray) {
        Log.v("ggwave", "Playing encoded message ..")
        mPlaybackThread = PlaybackThread(c_message, object : PlaybackListener {
            override fun process(progress: ByteArray) {
                callback?.process(progress)
            }

            override fun onProgress(progress: Int) {
                // todo : progress updates
            }

            override fun onCompletion() {
                mPlaybackThread!!.stopPlayback()
                /*
                // TODO: Callback?
                (findViewById<View>(R.id.buttonTogglePlayback) as Button).text = "Send Message"
                (findViewById<View>(R.id.textViewStatusOut) as TextView).text = "Status: Idle"

                 */
            }
        })
    }
    fun startAudioCapturingSafe(context: Activity) {
        mCapturingThread = CapturingThread(object : AudioDataReceivedListener {
            override fun onAudioDataReceived(data: ShortArray?) {
                //Log.v("ggwave", "java: 0 = " + data[0]);
                processCaptureData(data!!)
                callback?.process(convert(data))
            }
        })
        Log.i("ggwave", "startAudioCapturingSafe")
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            Log.i("ggwave", " - record permission granted")
            mCapturingThread!!.startCapturing(context)
        } else {
            Log.i("ggwave", " - record permission NOT granted")
            requestMicrophonePermission(context)
        }
    }

    private fun requestMicrophonePermission(context: Activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.RECORD_AUDIO)) {
            AlertDialog.Builder(context)
                    .setTitle("Microphone Access Requested")
                    .setMessage("Microphone access is required in order to receive audio messages")
                    .setPositiveButton(android.R.string.yes, DialogInterface.OnClickListener { dialog, which ->
                        ActivityCompat.requestPermissions(context, arrayOf(
                                Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO)
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
        } else {
            ActivityCompat.requestPermissions(context, arrayOf(
                    Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO)
        }
    }


    fun checkPermissions(context: Activity) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        Log.v(CapturingThread.LOG_TAG, "Start")
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO)
    }
    private fun convert(input: ShortArray): ByteArray {
        var index: Int
        val iterations = input.size
        val bb = ByteBuffer.allocate(input.size * 2)
        index = 0
        while (index != iterations) {
            bb.put(input[index].toByte())
            ++index
        }
        return bb.array()
    }


}

