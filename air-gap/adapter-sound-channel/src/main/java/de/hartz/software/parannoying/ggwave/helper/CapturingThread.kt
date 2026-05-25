package de.hartz.software.parannoying.ggwave.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import de.hartz.software.parannoying.ggwave.interfaces.internal.AudioDataReceivedListener

class CapturingThread(private val mListener: AudioDataReceivedListener) {
    private var mShouldContinue = false
    private var mThread: Thread? = null

    fun capturing(): Boolean {
        return mThread != null
    }

    fun startCapturing(context: Activity) {
        if (mThread != null) return
        mShouldContinue = true
        mThread = Thread { capture(context) }
        mThread!!.start()
    }

    fun stopCapturing() {
        if (mThread == null) return
        mShouldContinue = false
        mThread = null
    }

    @SuppressLint("MissingPermission")
    private fun capture(context: Activity) {
        // buffer size in bytes
        var bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT)
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2
        }
        bufferSize = 4 * 1024
        val audioBuffer = ShortArray(bufferSize / 2)
        val record =
                AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize)
        Log.d("ggwave", "buffer size = $bufferSize")
        Log.d("ggwave", "Sample rate = " + record.sampleRate)
        if (record.state != AudioRecord.STATE_INITIALIZED) {
            Log.e(LOG_TAG, "Audio Record can't initialize!")
            return
        }
        record.startRecording()
        Log.v(LOG_TAG, "Start capturing")
        var shortsRead: Long = 0
        while (mShouldContinue) {
            val numberOfShort = record.read(audioBuffer, 0, audioBuffer.size)
            shortsRead += numberOfShort.toLong()
            mListener.onAudioDataReceived(audioBuffer)
        }
        record.stop()
        record.release()
        Log.v(LOG_TAG, String.format("Capturing stopped. Samples read: %d", shortsRead))
    }

    companion object {
        val LOG_TAG = CapturingThread::class.java.simpleName
         const val SAMPLE_RATE = 48000
    }
}
