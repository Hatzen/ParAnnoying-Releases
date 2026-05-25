package de.hartz.software.parannoying.air.gap.fragments.exchange.receive

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.gauravk.audiovisualizer.visualizer.BlobVisualizer
import com.google.android.material.snackbar.Snackbar
import de.hartz.software.parannoying.air.gap.R
import de.hartz.software.parannoying.air.gap.fragments.exchange.AbstractReceiveChannelFragment
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.getSoundIcon
import de.hartz.software.parannoying.core.model.domain.settings.Channels
import de.hartz.software.parannoying.ggwave.SoundHandlerApi
import de.hartz.software.parannoying.ggwave.interfaces.ReceivedMessageCallback


class SoundReceiveChannelFragment: AbstractReceiveChannelFragment() {
    override val channel: Channels
        get() = Channels.SOUND

    override var buttonResource = { context: Context -> context.getSoundIcon(IconHelper.SMALL_ICON_WHITE) }
    private lateinit var mainView: View
    private lateinit var mVisualizer: BlobVisualizer


    override fun onStart() {
        super.onStart()
        // TODO: Is this the correct place for permission handling? It mustnt be within the init method!
        // General attempt would be showing fragment only when permission is granted!
        setupSoundPermissions()
    }

    private fun setupSoundPermissions() {
        val permission = ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.RECORD_AUDIO)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(Manifest.permission.CAMERA),
                    SoundHandlerApi.REQUEST_RECORD_AUDIO)
        }
    }

    override fun createMainView() { }

    override fun getMainView(): View {
        return mainView
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainView = inflater.inflate(R.layout.fragment_sound, container,
                false)
        mVisualizer = mainView.findViewById(R.id.blob);
        mVisualizer.show()
        return mainView
    }

    override fun init () {

        SoundHandlerApi.receiveData(requireActivity(), object: ReceivedMessageCallback {
            override fun receivedMessage(message: String) {
                Log.i(javaClass.simpleName, "received data via sound" + message)
                fragmentReceivedSomeData(message)
            }

            override fun process(array: ByteArray) {
                Log.i(javaClass.simpleName, "process data via sound " + array.size)
                mVisualizer.setRawAudioBytes(array)
            }
        })
    }

    override fun deinit() {
        SoundHandlerApi.stopReceive()
        mVisualizer.release()
    }

    override fun runAdditionalAction() {
        // TODO: evaluate resetting might help receiving proper data
        deinit()
        init()
    }

    override fun additionalActionDescription() {
        Snackbar.make(requireView(), "Receive data via sound, check volume wen nothing happens.", Snackbar.LENGTH_LONG)
                .show()
    }

    // TODO: Can be set to true when data is processed going yellow and when received going green.
    override fun isStatusSupported(): Boolean {
        return false
    }

}