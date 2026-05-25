package de.hartz.software.parannoying.online.adapters.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.mikepenz.iconics.Iconics
import com.mikepenz.iconics.view.IconicsTextView
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.helper.security.DataSecurityHelper
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.online.R
import de.hartz.software.parannoying.online.model.OnlineStorage
import de.hartz.software.parannoying.online.model.domain.LoggedEncryptedMessage


class MessageOverviewLogAdapter(var values :  List<LoggedEncryptedMessage>, val onlineStorage: OnlineStorage, val securityInterfaceHolder: SecurityInterfaceHolder, context:Context)
    :ArrayAdapter<LoggedEncryptedMessage>(context, de.hartz.software.parannoying.online.R.layout.row_received_encryptedmessage) {

    override fun getCount(): Int {
        return values.size
    }

    override fun getView(position:Int, convertView:View?, parent:ViewGroup):View {
        val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


        val encryptedMessag = values[position]

        // TODO: use viewholder pattern?
        val rowView:View
        if (encryptedMessag.isReceivedMessage()) {
            rowView = inflater.inflate(de.hartz.software.parannoying.online.R.layout.row_received_encryptedmessage, parent, false)

            // TODO: Unify with Inbox adapter
            rowView.findViewById<TextView>(de.hartz.software.parannoying.online.R.id.label).text = "Message " + securityInterfaceHolder.hashHelper.getStringHashForUi(encryptedMessag.message)

            val sendAtView = rowView.findViewById<TextView>(de.hartz.software.parannoying.online.R.id.outbox_time)
            if (encryptedMessag.sendAt == -1L) {
                sendAtView.visibility = View.INVISIBLE
            } else {
                sendAtView.visibility = View.VISIBLE
                sendAtView.text = "{cmd-inbox-arrow-up}" + UiHelper.getDateWithTime(encryptedMessag.sendAt)
            }

            val createdAtView = rowView.findViewById<TextView>(de.hartz.software.parannoying.online.R.id.inbox_time)
            createdAtView.text = "{cmd-inbox-arrow-down}" + UiHelper.getDateWithTime(encryptedMessag.receivedAt)

        } else {
            rowView = inflater.inflate(de.hartz.software.parannoying.online.R.layout.row_send_encryptedmessage, parent, false)

            val deliveredText = rowView.findViewById<TextView>(de.hartz.software.parannoying.online.R.id.delivered_time)
            deliveredText.text = "{cmd-inbox-arrow-up}" + UiHelper.getDateWithTime(encryptedMessag.sendAt)

            // TODO: Unify with Outbox adapter
            rowView.findViewById<TextView>(de.hartz.software.parannoying.online.R.id.label).text = "Message " + securityInterfaceHolder.hashHelper.getStringHashForUi(encryptedMessag.message)

            val sendAtView = rowView.findViewById<TextView>(de.hartz.software.parannoying.online.R.id.outbox_time)
            sendAtView.text = "{cmd-inbox}" + UiHelper.getDateWithTime(encryptedMessag.createdAt)

            val hash = encryptedMessag.targetUserHash!!
            rowView.findViewById<ImageView>(R.id.dialogAvatar).setImageBitmap(IOHelper.getProfilePictureForUserWithHashOnly(hash, context))

            val dialogType: IconicsTextView = rowView.findViewById(R.id.dialogType)
            val dataSecurityHelper = DataSecurityHelper(securityInterfaceHolder)
            // Setup type of group
            dialogType.let {
                if (dataSecurityHelper.isOnlineIdValid(hash)) {
                    it.setTextColor(ResourcesCompat.getColor(context.getResources(), de.hartz.software.parannoying.core.R.color.colorPrimaryDark, null))
                } else {
                    it.setTextColor(ResourcesCompat.getColor(context.getResources(), de.hartz.software.parannoying.core.R.color.gray, null))
                }

                // TODO: Unify with ChatOverviewAdapter
                Iconics.Builder().on(it).build()
                if (dataSecurityHelper.isOnlineIdForOnlineGroup(hash)) {
                    it.text = "{faw-users} " +
                            hash
                                    .replace(DataSecurityHelper.ONLINE_GROUP_ONLINE_ID_TARGET_PREFIX_MARKER, DataSecurityHelper.ONLINE_GROUP_ONLINE_ID_SEPARATOR)
                                    .split(DataSecurityHelper.ONLINE_GROUP_ONLINE_ID_TARGET_PREFIX_MARKER).size
                } else if (hash.startsWith(DataSecurityHelper.NOTIFICATION_ID_PREFIX_GROUP_OFFLINE)) {
                    it.text = "{faw-users}"
                } else {
                    if (hash == onlineStorage.onlineUserId) {
                        it.text = "{cmd-account-circle}"
                    } else {
                        it.text = "{cmd-account}"
                    }
                }
            }
        }

        return rowView
    }
}