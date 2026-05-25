package de.hartz.software.parannoying.online.adapters.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.online.model.domain.InboxEncryptedMessage


class MessageOverviewInboxAdapter(val securityInterfaceHolder: SecurityInterfaceHolder, values :  List<InboxEncryptedMessage>, context:Context):ArrayAdapter<InboxEncryptedMessage>(context, de.hartz.software.parannoying.online.R.layout.row_received_encryptedmessage) {
    var values: List<InboxEncryptedMessage> = values

    override fun getCount(): Int {
        return values.size
    }

    override fun getView(position:Int, convertView:View?, parent:ViewGroup):View {
        val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        // TODO: use viewholder pattern? Not really needed as there shouldnt be many messages..
        val rowView = inflater.inflate(de.hartz.software.parannoying.online.R.layout.row_received_encryptedmessage, parent, false)

        val encryptedMessag = values[position]
        rowView.findViewById<TextView>(de.hartz.software.parannoying.online.R.id.label).text = "Message " +
                securityInterfaceHolder.hashHelper.getStringHashForUi(encryptedMessag.message)

        val sendAtView = rowView.findViewById<TextView>(de.hartz.software.parannoying.online.R.id.outbox_time)
        if (encryptedMessag.sendAt == -1L) {
            sendAtView.visibility = View.INVISIBLE
        } else {
            sendAtView.visibility = View.VISIBLE
            sendAtView.text = "{cmd-inbox-arrow-up}" + UiHelper.getDateWithTime(encryptedMessag.sendAt)
        }

        val createdAtView = rowView.findViewById<TextView>(de.hartz.software.parannoying.online.R.id.inbox_time)
        createdAtView.text = "{cmd-inbox-arrow-down}" + UiHelper.getDateWithTime(encryptedMessag.receivedAt)

        return rowView
    }

}