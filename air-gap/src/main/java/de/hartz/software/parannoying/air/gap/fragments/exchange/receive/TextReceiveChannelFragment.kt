package de.hartz.software.parannoying.air.gap.fragments.exchange.receive

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
import de.hartz.software.parannoying.air.gap.fragments.exchange.AbstractReceiveChannelFragment
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.getTextIcon
import de.hartz.software.parannoying.core.model.domain.settings.Channels

class TextReceiveChannelFragment: AbstractReceiveChannelFragment() {

    companion object {
        fun getClipboardContent(context: Context): String {
            // TODO: autotimer to clear clipboard.
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val text = clipboard.primaryClip!!.getItemAt(0).coerceToText(context).toString()
            Log.d("getClipboardContent", text)
            return text
        }
    }
    override val channel: Channels
        get() = Channels.TEXT

    override var buttonResource = { context: Context -> context.getTextIcon(IconHelper.SMALL_ICON_WHITE) }
    private lateinit var mainView: View

    override fun getMainView(): View {
        return mainView
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainView = inflater.inflate(R.layout.fragment_receive_text, container, false)
        return mainView
    }

    override fun createMainView() { }


    override fun init () {
        mainView.findViewById<EditText>(R.id.textContainer)?.setText("")
    }

    override fun runAdditionalAction() {
        val currentData = getClipboardContent(requireContext())
        fragmentReceivedSomeData(currentData)
        mainView.findViewById<EditText>(R.id.textContainer)?.setText(currentData)
    }

    override fun additionalActionDescription() {
        Snackbar.make(requireView(), "Use clipboard content to read as message.", Snackbar.LENGTH_LONG)
                .show()
    }

    override fun isStatusSupported(): Boolean {
        return false
    }

}