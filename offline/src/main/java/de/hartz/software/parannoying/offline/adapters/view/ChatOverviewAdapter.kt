package de.hartz.software.parannoying.offline.adapters.view

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.mikepenz.iconics.Iconics
import com.mikepenz.iconics.view.IconicsTextView
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.dialogs.DialogsListAdapter
import com.stfalcon.chatkit.utils.DateFormatter
import com.stfalcon.chatkit.utils.DateFormatter.Formatter
import de.hartz.software.parannoying.core.helper.security.DataSecurityHelper
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.offline.R
import de.hartz.software.parannoying.offline.model.domain.dialogs.BaseDialog
import de.hartz.software.parannoying.offline.model.domain.dialogs.CurrentUser
import de.hartz.software.parannoying.offline.model.domain.dialogs.OfflineGroup
import de.hartz.software.parannoying.offline.model.domain.dialogs.OnlineGroup
import de.hartz.software.parannoying.offline.model.domain.dialogs.User


class ChatOverviewAdapter(val securityinterfaceholder: SecurityInterfaceHolder, val context: Context, imageLoader: ImageLoader)
    : DialogsListAdapter<BaseDialog>(R.layout.item_dialog, ChatOverviewViewholder::class.java, imageLoader) {

    init {
        ChatOverviewAdapter.instance = this
    }

    companion object {
        // TODO: remove
        var instance: ChatOverviewAdapter? = null

        val formatter = object : Formatter {
            override fun format(date: java.util.Date?): String {
                if (DateFormatter.isToday(date)) {
                    return DateFormatter.format(date, DateFormatter.Template.TIME);
                } else if (DateFormatter.isYesterday(date)) {
                    return "Yesterday";
                } else {
                    return DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH_YEAR);
                }
            }
        }
    }


    // Setting inner class to pass context leads to strange crash..
    class ChatOverviewViewholder(itemView: View) : DialogsListAdapter.DialogViewHolder<BaseDialog>(itemView) {

        init {
            setDatesFormatter(formatter)
        }

        override fun onBind(dialog: BaseDialog) {
            val provider = ChatOverviewAdapter.instance
            if (provider == null) {
                return
            }

            setDatesFormatter(formatter)

            // Enable text getting encoded as "stars".
            tvLastMessage?.let{ textview ->
                Iconics.Builder().on(textview).build()
            }

            val dialogType: IconicsTextView = itemView.findViewById(R.id.dialogType)
            val dialogLastMessageUserName: TextView = itemView.findViewById(R.id.dialogLastMessageUserName)

            // Setup sender name.
            dialogLastMessageUserName.let {
                if (dialog.lastMessageToDisplay == null) {
                    it.text = ""
                } else if (dialog.lastMessageToDisplay!!.sender is CurrentUser) {
                    it.text = "You:"
                } else {
                    it.text = dialog.lastMessageToDisplay!!.sender!!.nickname + ":"
                }
            }
            // Setup type of group
            dialogType.let {
                if (DataSecurityHelper(provider.securityinterfaceholder).isOnlineIdValid(dialog.hash)) {
                    it.setTextColor(ResourcesCompat.getColor(provider.context.getResources(), R.color.colorPrimaryDark, null))
                } else {
                    it.setTextColor(ResourcesCompat.getColor(provider.context.getResources(), R.color.gray, null))
                }

                Iconics.Builder().on(it).build()
                if (dialog is User) {
                    if (dialog.isCurrentUser()) {
                        it.text = "{cmd-account-circle}"
                    } else {
                        // TODO: For some reason cant get faw / fab-user as it is only regualr not solid
                        it.text = "{cmd-account}"
                    }
                } else if (dialog is OnlineGroup) {
                    it.text = "" + dialog.firebaseEmailIds.size + "{faw-users}"
                } else if (dialog is OfflineGroup) {
                    it.text = "{faw-users}"
                }
            }
            // Last call to not overwrite formatter.
            // TODO: crashes as setting
            //      java.lang.IllegalArgumentException: Parameter specified as non-null is null: method com.mikepenz.iconics.view.IconicsTextView.setText, parameter text
            super.onBind(dialog)
        }
    }
}