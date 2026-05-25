package de.hartz.software.parannoying.air.gap.fragments.exchange.send

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.google.android.material.snackbar.Snackbar
import de.hartz.software.parannoying.air.gap.R
import de.hartz.software.parannoying.air.gap.fragments.exchange.AbstractSendChannelFragment
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.getTextIcon
import de.hartz.software.parannoying.core.model.domain.settings.Channels


class TextSendChannelFragment: AbstractSendChannelFragment() {
    companion object {
        /**
         * Must run on UI Thread otherwise
         * java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
         */
        fun setClipboardContent(activity: Activity, text: String) {
            activity.runOnUiThread {
                val clipboard =
                    activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

                clipboard.setPrimaryClip(ClipData.newPlainText("parannoying send content", text))
                Log.d("setClipboardContent", text)
            }
        }
    }
    override val channel: Channels
        get() = Channels.TEXT

    override var buttonResource = { context: Context -> context.getTextIcon(IconHelper.SMALL_ICON_WHITE) }
    private lateinit var mainView: View


    // Theoretically 2mb are the max string size. but edittext has probably lower limits.
    override val maxDataSize = 2000

    override fun init () {
    }

    override fun startTransferDataSet(newData: String) {
        mainView.findViewById<EditText>(R.id.textContainer)?.setText(currentData)
    }

    override fun getMainView(): View {
        return mainView
    }

    override fun createMainView() {
    }

    override fun runAdditionalAction() {
        val text = mainView.findViewById<EditText>(R.id.textContainer)!!.text.toString()
        setClipboardContent(requireActivity(), text)
        Snackbar.make(requireView(), "The encrypted content got copied to your clipboard.", Snackbar.LENGTH_LONG)
                .show()
    }

    override fun additionalActionDescription() {
        Snackbar.make(requireView(), "The encrypted content will get copied to your clipboard.", Snackbar.LENGTH_LONG)
                .show()
    }

    override fun isStatusSupported(): Boolean {
        return false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainView = inflater.inflate(R.layout.fragment_receive_text, container, false)
        return mainView
    }

}