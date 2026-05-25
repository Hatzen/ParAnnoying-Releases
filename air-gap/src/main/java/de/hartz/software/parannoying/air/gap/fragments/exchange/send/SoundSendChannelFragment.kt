package de.hartz.software.parannoying.air.gap.fragments.exchange.send

import android.content.Context
import android.media.MediaRecorder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gauravk.audiovisualizer.base.BaseVisualizer
import com.google.android.material.snackbar.Snackbar
import de.hartz.software.parannoying.air.gap.R
import de.hartz.software.parannoying.air.gap.fragments.exchange.AbstractSendChannelFragment
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.core.helper.ui.getSoundIcon
import de.hartz.software.parannoying.core.model.domain.settings.Channels
import de.hartz.software.parannoying.ggwave.SoundHandlerApi
import de.hartz.software.parannoying.ggwave.interfaces.SendMessageCallback
import de.hartz.software.parannoying.ggwave.model.ChannelOption
import java.util.Timer
import java.util.TimerTask
import kotlin.random.Random


class SoundSendChannelFragment: AbstractSendChannelFragment() {
    override val channel: Channels
        get() = Channels.SOUND

    override var buttonResource = { context: Context -> context.getSoundIcon(IconHelper.SMALL_ICON_WHITE) }
    private lateinit var mainView: View
    private lateinit var mVisualizer: BaseVisualizer
    private var timer: Timer? = null

    override fun startTransferDataSet(newData: String) {
        val speed = requireActivity().Storage.readSettings().soundSpectrumAndSpeed.speed
        val channelOption = ChannelOption.getByValue(speed)

        val callback =  object: SendMessageCallback {
            override fun playbackFinish() {
                timer?.cancel()
                timer = null
                mVisualizer.setRawAudioBytes(ByteArray(128))
            }
        }

        SoundHandlerApi.sendData(requireContext(), currentData, callback, channelOption)
    }

    override fun init() {
        super.init()
        timer = Timer() // At this line a new Thread will be created
        val MILLISECONDS_FOR_FLUENT_ANIMATION = 30L
        timer!!.schedule(object : TimerTask() {
            override fun run() {

                // TODO: Note: Currently, MediaRecorder does not work on the emulator.
                var byte = (MediaRecorder().maxAmplitude % Byte.MAX_VALUE).toByte()
                // When not working show random animation..
                if (byte == 0.toByte()) {
                    byte = (Random.nextInt() % Byte.MAX_VALUE).toByte()
                }
                val bytes = ByteArray(101)
                for (i in 0..100) {
                    // toByte for large numbers leads to 0, so modulo max value..
                    bytes[i] = (byte * byte * i % Byte.MAX_VALUE).toByte()
                }

                activity?.runOnUiThread {
                    mVisualizer.setRawAudioBytes(bytes)
                }
            }
        }, 0, MILLISECONDS_FOR_FLUENT_ANIMATION)
        if (!isRetrievingLastSoundSupported()) {
            UiHelper.showToastFromBackgroundTask(requireContext(), "Retrieving sound is not supported, animation is just random")
        }
    }

    override fun deinit () {
        SoundHandlerApi.stopSendingData()
        timer?.cancel()
        timer = null
        mVisualizer.release()
    }


    override fun getMainView(): View {
        return mainView
    }

    override fun createMainView() {
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainView = inflater.inflate(R.layout.fragment_sound, container, false)
        mVisualizer = mainView.findViewById(R.id.blob);
        return mainView
    }

    override fun runAdditionalAction() {
        SoundHandlerApi.stopSendingData()
        startTransferDataSet(currentData!!)
    }

    override fun additionalActionDescription() {
        Snackbar.make(requireView(), "Send data via sound, please try in a quiete environment.", Snackbar.LENGTH_LONG)
                .show()
    }

    override fun isStatusSupported(): Boolean {
        return false
    }

    private fun isRetrievingLastSoundSupported(): Boolean {
        var byte = (MediaRecorder().maxAmplitude % Byte.MAX_VALUE).toByte()
        if (byte == 0.toByte()) {
            return false
        }
        return true
    }


}