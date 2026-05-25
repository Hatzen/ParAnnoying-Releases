package de.hartz.software.parannoying.ggwave.helper

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import de.hartz.software.parannoying.ggwave.interfaces.internal.PlaybackListener
import java.nio.ShortBuffer


class PlaybackThread(samples: ShortArray, listener: PlaybackListener?) {
    private var mThread: Thread? = null
    private var mShouldContinue = false
    private val mSamples: ShortBuffer
    private val mNumSamples: Int
    private val mListener: PlaybackListener?

    init {
        mSamples = ShortBuffer.wrap(samples)
        mNumSamples = samples.size
        mListener = listener
    }

    fun playing(): Boolean {
        return mThread != null
    }

    fun startPlayback() {
        if (mThread != null) return

        // Start streaming in a thread
        mShouldContinue = true
        mThread = Thread { play() }
        mThread!!.start()
    }

    fun stopPlayback() {
        if (mThread == null) return
        mShouldContinue = false
        mThread = null
    }

    private fun play() {
        var bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT)
        if (bufferSize == AudioTrack.ERROR || bufferSize == AudioTrack.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2
        }
        bufferSize = 16 * 1024
        val audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM)
        audioTrack.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onPeriodicNotification(track: AudioTrack) {
                if (mListener != null && track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    mListener.onProgress(track.playbackHeadPosition * 1000 / SAMPLE_RATE)
                }
            }

            override fun onMarkerReached(track: AudioTrack) {
                Log.v(LOG_TAG, "Audio file end reached")
                track.release()
                mListener?.onCompletion()
            }
        })
        audioTrack.positionNotificationPeriod = SAMPLE_RATE / 30 // 30 times per second
        audioTrack.notificationMarkerPosition = mNumSamples
        audioTrack.play()
        Log.v(LOG_TAG, "Audio streaming started")
        val buffer = ShortArray(bufferSize)
        mSamples.rewind()
        val limit = mNumSamples
        var totalWritten = 0
        while (mSamples.position() < limit && mShouldContinue) {
            val numSamplesLeft = limit - mSamples.position()
            var samplesToWrite: Int
            if (numSamplesLeft >= buffer.size) {
                mSamples[buffer]
                samplesToWrite = buffer.size
            } else {
                for (i in numSamplesLeft until buffer.size) {
                    buffer[i] = 0
                }
                mSamples[buffer, 0, numSamplesLeft]
                samplesToWrite = numSamplesLeft
            }

            mListener?.process(getBytes(buffer))

            totalWritten += samplesToWrite
            audioTrack.write(buffer, 0, samplesToWrite)
        }
        if (!mShouldContinue) {
            audioTrack.release()
        }
        Log.v(LOG_TAG, "Audio streaming finished. Samples written: $totalWritten")
    }

    fun getBytes(data: ShortArray): ByteArray {
        val resultData = ByteArray(data.size * 2)
        var c = 0
        for (i in data.indices) {

            // TODO: remove +100 as test to show something..
            resultData[c++] = (data[i].toInt() + 100 and 0xFF).toByte()
            resultData[c++] = (data[i].toInt() + 100 ushr 8 and 0xFF).toByte()

            // resultData[c++] = (data[i].toInt() and 0xFF).toByte()
            // resultData[c++] = (data[i].toInt() ushr 8 and 0xFF).toByte()
        }
        return resultData
    }

    companion object {
        const val SAMPLE_RATE = 48000
        private val LOG_TAG = PlaybackThread::class.java.simpleName
    }
}