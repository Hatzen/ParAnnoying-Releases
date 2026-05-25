package de.hartz.software.parannoying.air.gap.fragments.exchange.receive

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.material.snackbar.Snackbar
import de.hartz.software.parannoying.air.gap.fragments.exchange.AbstractReceiveChannelFragment
import de.hartz.software.parannoying.air.gap.nfc.NFCHandlerApi
import de.hartz.software.parannoying.air.gap.nfc.helper.NFCHelper
import de.hartz.software.parannoying.air.gap.nfc.interfaces.ReceivedMessageCallback
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.core.helper.ui.getNfcIcon
import de.hartz.software.parannoying.core.model.domain.settings.Channels
import java.util.Timer


class NFCReceiveChannelFragment: AbstractReceiveChannelFragment(), ReceivedMessageCallback {
    override val channel: Channels
        get() = Channels.NFC

    override var buttonResource = { context: Context -> context.getNfcIcon(IconHelper.SMALL_ICON_WHITE) }
    private lateinit var mainView: ImageView
    // NFC vars
    lateinit var nfcDrawables: Array<Drawable>
    lateinit var nfcHandlerApi: NFCHandlerApi

    var imageSwitcherTimer: Timer? = null

    override fun getMainView(): View {
        return mainView
    }

    override fun createMainView () {
        mainView = ImageView(activity)
        mainView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
    }

    override fun runAdditionalAction() {
        imageSwitcherTimer = UiHelper.initImageSwitcher(requireActivity(), mainView, nfcDrawables)
        if (!NFCHelper.isNFCSupported(requireActivity())) {
            DialogHelper.showDialog(requireContext(), "NFC not Supported", "Please use an other channel.")
            return
        }
        if (!NFCHelper.isEnabled(requireActivity())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                val intent = Intent(Settings.ACTION_NFC_SETTINGS)
                startActivity(intent)
            } else {
                val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(intent)
            }
        }
    }

    override fun additionalActionDescription() {
        Snackbar.make(requireView(), "Enable NFC to receive messages from other devices.", Snackbar.LENGTH_LONG)
                .show()
    }

    override fun isStatusSupported(): Boolean {
        return false
    }

    override fun init() {
        nfcDrawables = arrayOf(
            requireContext().getNfcIcon(IconHelper.LARGE_ICON_PRIMARY),
            requireContext().getNfcIcon(IconHelper.LARGE_ICON_WHITE)
        )
        mainView.setImageDrawable(requireContext().getNfcIcon(IconHelper.LARGE_ICON_WHITE))

        nfcHandlerApi = NFCHandlerApi(requireActivity())
        nfcHandlerApi.receiveData(this)
    }

    override fun deinit() {
        imageSwitcherTimer?.cancel()
        if (NFCHelper.isNFCSupported(requireActivity())) {
            nfcHandlerApi.stopReceive()
        }
    }

    override fun receiveMessage(message: String) {
        fragmentReceivedSomeData(message)
    }

    // TODO: Is this really needed? Either we have message count or receive just one item?
    override fun disconnect() {
        TODO("Not yet implemented")
    }

}