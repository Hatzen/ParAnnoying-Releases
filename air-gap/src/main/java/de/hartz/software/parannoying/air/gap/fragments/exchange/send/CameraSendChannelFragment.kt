package de.hartz.software.parannoying.air.gap.fragments.exchange.send

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.google.android.material.snackbar.Snackbar
import de.hartz.software.parannoying.air.gap.fragments.exchange.AbstractSendChannelFragment
import de.hartz.software.parannoying.air.gap.helpers.channels.QrCodeHelper
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.getCameraIcon
import de.hartz.software.parannoying.core.model.domain.settings.Channels


class CameraSendChannelFragment: AbstractSendChannelFragment() {

    companion object {
        fun initQrCodePopup(context: Context, mainView: ImageView, imageView: ImageView) {
            val builder = Dialog(context, android.R.style.Theme_Light) // ,
            builder.requestWindowFeature(Window.FEATURE_NO_TITLE)
            builder.window!!.setBackgroundDrawable(
                    ColorDrawable(Color.BLACK))
            builder.addContentView(imageView, RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT))

            mainView.setOnClickListener {
                builder.show()
            }
        }
    }
    override val channel: Channels
        get() = Channels.CAMERA

    private lateinit var mainView: ImageView
    private lateinit var popupView: ImageView
    override var buttonResource = { context: Context -> context.getCameraIcon(IconHelper.SMALL_ICON_WHITE) }

    // qr code max length of 4000+. But with 2000 it is already to big to get scanned
    override val maxDataSize = 1000
    override fun getMainView(): View {
        return mainView
    }

    override fun createMainView () {
        mainView = ImageView(requireContext())
        mainView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        mainView.adjustViewBounds = true
        mainView.tag = "qrCodeImage"

        popupView = ImageView(context)
        initQrCodePopup(requireContext(), mainView, popupView)
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
        val qrcode = QrCodeHelper.dataToQrCode(newData, requireContext())
        if (qrcode != null) {
            mainView.setImageBitmap(qrcode)
            popupView.setImageBitmap(qrcode)
        } else {
            throw RuntimeException("Data is too large for creating qrcode. Length of data: " + newData.length)
        }
    }
}