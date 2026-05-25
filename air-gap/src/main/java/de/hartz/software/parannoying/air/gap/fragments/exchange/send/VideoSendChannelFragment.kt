package de.hartz.software.parannoying.air.gap.fragments.exchange.send

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.google.android.material.snackbar.Snackbar
import de.hartz.software.parannoying.air.gap.fragments.exchange.AbstractSendChannelFragment
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.getVideoIcon
import de.hartz.software.parannoying.core.model.domain.settings.Channels
import de.hartz.software.parannoying.core.model.domain.settings.QrVideoSpeed
import de.hartz.software.parannoying.txqr.VideoHandlerApi


class VideoSendChannelFragment: AbstractSendChannelFragment() {
    override val channel: Channels
        get() = Channels.VIDEO

    private lateinit var mainView: ImageView
    private lateinit var popupView: ImageView
    override var buttonResource = { context: Context -> context.getVideoIcon(IconHelper.SMALL_ICON_WHITE) }

    // A QR code is capable of encoding a maximum of 2953 bytes of data, 4296 alphanumeric
    // Minus header and animate up to 10 qrcodes.
    override val maxDataSize = 4000 * 10

    override fun getMainView(): View {
        return mainView
    }

    override fun createMainView () {
        val context = requireContext()
        mainView = ImageView(context)

        mainView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        mainView.adjustViewBounds = true

        popupView = ImageView(context)
        CameraSendChannelFragment.initQrCodePopup(requireContext(), mainView, popupView)
    }

    override fun runAdditionalAction() {
        mainView.isDrawingCacheEnabled = true
        val bitmap = mainView.drawingCache

        IOHelper.initShareImage(bitmap, requireActivity())
    }

    override fun additionalActionDescription() {
        Snackbar.make(requireView() , "Scan the qr code by camera or share it.", Snackbar.LENGTH_LONG)
                .show()
    }

    override fun isStatusSupported(): Boolean {
        return false
    }

    override fun isConfirmationSupported(): Boolean {
        return true
    }

    override fun startTransferDataSet(newData: String) {
        val speed = when (requireActivity().Storage.readSettings().videoSpeed) {
            QrVideoSpeed.NORMAL -> 0
            QrVideoSpeed.SLOW -> -1
            QrVideoSpeed.FAST -> 1
            else -> { throw RuntimeException("Impossible..") }
        }
        val file = VideoHandlerApi().createGif(newData, speed)
        Glide.with(this).load(file).into(DrawableImageViewTarget(mainView))
        Glide.with(this).load(file).into(DrawableImageViewTarget(popupView))
    }
}