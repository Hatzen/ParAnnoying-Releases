package de.hartz.software.parannoying.ggwave

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.widget.Toast
import de.hartz.software.parannoying.ggwave.interfaces.ReceivedMessageCallback
import de.hartz.software.parannoying.ggwave.interfaces.SendMessageCallback
import de.hartz.software.parannoying.ggwave.model.ChannelOption


object SoundHandlerApi {

    const val REQUEST_RECORD_AUDIO = 13
    private val soundChannelHandler = SoundChannelHandler()

    init {
        soundChannelHandler.init()
    }

    /**
     * Should be called by calling activity.
     * TODO: Needs to be called!
     */
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_RECORD_AUDIO && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            soundChannelHandler.mCapturingThread!!.stopCapturing()
        }
    }

    fun receiveData(context: Activity, callback: ReceivedMessageCallback) {
        soundChannelHandler.callback = callback

        if (soundChannelHandler.mCapturingThread == null ||
                !soundChannelHandler.mCapturingThread!!.capturing()) {
            soundChannelHandler.startAudioCapturingSafe(context)
        } else {
            soundChannelHandler.mCapturingThread!!.stopCapturing()
        }
    }

    fun stopReceive() {
        soundChannelHandler.mCapturingThread?.stopCapturing()
        soundChannelHandler.mCapturingThread = null
    }

    fun sendData(context: Context, dataToSend: String, callback: SendMessageCallback? = null, option: ChannelOption) {
        if (isPlaying()) {
            throw IllegalStateException("Sound is already playing")
        }
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentVolume = audio!!.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audio!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val percent = currentVolume / maxVolume
        if (percent < 0.3f) {
            Toast.makeText(context, "The volume is low, make it louder for better results", Toast.LENGTH_LONG)
            // TODO: Offer dialog to set loudness instantly
            // val percent = 0.7f
            // val seventyVolume = (maxVolume * percent).toInt()
            // audio!!.setStreamVolume(AudioManager.STREAM_MUSIC, seventyVolume, 0)
        }

        soundChannelHandler.sendMessage(dataToSend, option)
        soundChannelHandler.mPlaybackThread!!.startPlayback()

        if (callback != null) {
            // TODO: optimize. use awaitlity or at least some scheduling or calculate waiting on data size?
            Thread {
                do {
                    Thread.sleep(1000)
                } while(isPlaying())
                callback.playbackFinish()
            }.start()
        }
    }

    fun stopSendingData() {
        if (isPlaying()) {
            soundChannelHandler.mPlaybackThread!!.stopPlayback()
        }
    }

    fun isPlaying(): Boolean {
      return soundChannelHandler.mPlaybackThread != null && soundChannelHandler.mPlaybackThread!!.playing()
    }

    fun sendData(context: Context, dataToSend: String, callback: SendMessageCallback) {

    }

}