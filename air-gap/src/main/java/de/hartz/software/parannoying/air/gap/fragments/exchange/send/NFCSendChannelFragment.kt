package de.hartz.software.parannoying.air.gap.fragments.exchange.send

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.material.snackbar.Snackbar
import de.hartz.software.parannoying.air.gap.fragments.exchange.AbstractSendChannelFragment
import de.hartz.software.parannoying.air.gap.nfc.NFCHandlerApi
import de.hartz.software.parannoying.air.gap.nfc.helper.NFCHelper
import de.hartz.software.parannoying.air.gap.nfc.interfaces.SendMessageCallback
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.core.helper.ui.getNfcIcon
import de.hartz.software.parannoying.core.model.domain.settings.Channels
import java.util.Timer


class NFCSendChannelFragment: AbstractSendChannelFragment(), SendMessageCallback {
    override val channel: Channels
        get() = Channels.NFC

    override var buttonResource = { context: Context -> context.getNfcIcon(IconHelper.SMALL_ICON_WHITE) }
    private lateinit var mainView: ImageView
    // NFC vars
    lateinit var nfcDrawables: Array<Drawable>
    lateinit var nfcHandlerApi: NFCHandlerApi
    var imageSwitcherTimer: Timer? = null


    // NDEF record payloads are limited in size to 2^32–1 bytes long
    override val maxDataSize = 0xFF_FF_FF

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
        Snackbar.make(requireView(), "Prepare Nfc to be able to send messages from device.", Snackbar.LENGTH_LONG)
                .show()
    }

    override fun isStatusSupported(): Boolean {
        return false
    }

    override fun init() {
        nfcHandlerApi.startSending(this)
        nfcDrawables = arrayOf(
            requireContext().getNfcIcon(IconHelper.LARGE_ICON_PRIMARY),
            requireContext().getNfcIcon(IconHelper.LARGE_ICON_WHITE)
        )
        mainView.setImageDrawable(requireContext().getNfcIcon(IconHelper.LARGE_ICON_WHITE))

        if (NFCHelper.isNFCSupported(requireActivity())) {
            nfcHandlerApi = NFCHandlerApi(requireActivity())
            UiHelper.initImageSwitcher(requireActivity(), mainView, nfcDrawables, 2)
        }
    }

    override fun deinit() {
        imageSwitcherTimer?.cancel()

        if (NFCHelper.isNFCSupported(requireActivity())) {
            nfcHandlerApi.stopSendingData()
            UiHelper.initImageSwitcher(requireActivity(), mainView, nfcDrawables, 2)
        }
    }

    override fun messageSend() {
        fragmentResultListener.processNextItem()
    }

    override fun startTransferDataSet(newData: String) {
        nfcHandlerApi.sendData(newData)
    }
}