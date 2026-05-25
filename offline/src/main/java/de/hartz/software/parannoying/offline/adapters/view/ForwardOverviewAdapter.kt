package de.hartz.software.parannoying.offline.adapters.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.offline.R
import de.hartz.software.parannoying.offline.model.domain.events.BaseEvent
import de.hartz.software.parannoying.offline.model.domain.events.ForwardDataset


class ForwardOverviewAdapter(values : List<ForwardDataset>, val securityInterfaceHolder: SecurityInterfaceHolder, context:Context):ArrayAdapter<BaseEvent>(context, R.layout.row_event) {
    var values: List<ForwardDataset> = values

    override fun getCount(): Int {
        return values.size
    }

    override fun getView(position:Int, convertView:View?, parent:ViewGroup):View {
        val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = inflater.inflate(R.layout.row_forward_dataset, parent, false)

        val forwardDataset = values[position]
        rowView.findViewById<TextView>(R.id.label).text = "DataHash " + securityInterfaceHolder.hashHelper.getStringHashForUi(forwardDataset.data)

        val noteView = rowView.findViewById<TextView>(R.id.note)
        if (forwardDataset.note.isNullOrBlank()) {
            noteView.visibility = View.GONE
        } else {
            noteView.visibility = View.VISIBLE
            noteView.text = forwardDataset.note
        }


        val createdAtView = rowView.findViewById<TextView>(R.id.event_time)
        createdAtView.text = UiHelper.getDateWithTime(forwardDataset.createdAtTimestamp * 1000)

        return rowView
    }

}