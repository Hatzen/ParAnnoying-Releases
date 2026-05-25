package de.hartz.software.parannoying.offline.adapters.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.core.helper.ui.UiHelper.setRoundedBackground
import de.hartz.software.parannoying.offline.R
import de.hartz.software.parannoying.offline.model.domain.SendMessage
import de.hartz.software.parannoying.offline.model.domain.events.BaseEvent


class SendDataOverviewAdapter(var values : List<List<SendMessage>>, context:Context)
    :ArrayAdapter<BaseEvent>(context, R.layout.row_send_data) {

    override fun getCount(): Int {
        return values.size
    }

    override fun getView(position:Int, convertView:View?, parent:ViewGroup): View {
        val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = inflater.inflate(R.layout.row_send_data, parent, false)

        val sendMessages = values[position][0]
        val messageCount = values[position].size

        // TODO: sender is not the wanted user we need to store receiver addiionally..
        //    as well as number of messages etc
        rowView.findViewById<TextView>(R.id.receiver_name).text = sendMessages.message.relatedDialog!!.nickname
        val messageText = rowView.findViewById<TextView>(R.id.message_text)
        messageText.text = sendMessages.message.text
        // TODO: Add file icon and use message counter to show mbs.

        val messageCountView = rowView.findViewById<TextView>(R.id.messageCount)
        messageCountView.visibility = if (messageCount > 1) View.VISIBLE else View.GONE
        messageCountView.text = "$messageCount Messages"
        setRoundedBackground(context, messageCountView)


        val createdAtView = rowView.findViewById<TextView>(R.id.event_time)
        createdAtView.text = UiHelper.getDateWithTime(sendMessages.message.createdAtTimestamp * 1000)

        return rowView
    }

}