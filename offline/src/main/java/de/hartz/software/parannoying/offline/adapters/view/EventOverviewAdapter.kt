package de.hartz.software.parannoying.offline.adapters.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import de.hartz.software.parannoying.air.gap.model.UseCases.Offline.EVENT_SEND
import de.hartz.software.parannoying.core.extensions.launchActivity
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.core.interfaces.di.air.gap.AirGapAdapter
import de.hartz.software.parannoying.offline.activities.offline.ChatActivity
import de.hartz.software.parannoying.offline.model.domain.events.BaseEvent
import de.hartz.software.parannoying.offline.model.domain.events.CorruptedDataEvent
import de.hartz.software.parannoying.offline.model.domain.events.MessageEvent
import de.hartz.software.parannoying.offline.model.domain.events.UserEvent


class EventOverviewAdapter(var values : List<BaseEvent>, val context:Activity, val airGapAdapter: AirGapAdapter): BaseExpandableListAdapter() {

    private var expandableListDetail: List<List<BaseEvent>>
    private val expandableListTitle: Collection<String> get() = expandableListDetail.map { it[0].eventType }
    init {
        expandableListDetail = values.fold(ArrayList<ArrayList<BaseEvent>>()) {
            list, item -> list.apply {
                if (isEmpty() || last().last().eventType != item.eventType)
                    add(arrayListOf(item))
                else
                    last().add(item)
            }
        }
    }

    override fun getChild(listPosition: Int, expandedListPosition: Int): Any? {
        return expandableListDetail[listPosition][expandedListPosition]
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    override fun getChildView(listPosition: Int, expandedListPosition: Int,
                              isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        if (convertView == null) {
            val layoutInflater = this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(de.hartz.software.parannoying.offline.R.layout.row_event, parent, false)
        }

        val event = expandableListDetail[listPosition][expandedListPosition]
        convertView!!.findViewById<TextView>(de.hartz.software.parannoying.offline.R.id.label).text = event.eventText

        val createdAtView = convertView.findViewById<TextView>(de.hartz.software.parannoying.offline.R.id.event_time)
        createdAtView.text = UiHelper.getDateWithTime(event.createdAtTimestamp * 1000)

        convertView.setOnClickListener { view ->
            if (event is UserEvent) {
                val passParams : (userIntent: Intent) -> Unit = {
                    it.putExtra(ChatActivity.EXTRA_USER, event.relatedUser.getUniqueDialogId())
                }
                context.launchActivity<ChatActivity>(init = passParams)
                return@setOnClickListener
            } else if (event is MessageEvent) {
                // TODO: Open ChatActivity and show related message. (But what with deleted messages?)
                airGapAdapter.startSend(EVENT_SEND.useText(event.relatedMessage.text))
            } else if (event is CorruptedDataEvent) {
                airGapAdapter.startSend(EVENT_SEND.useText(event.corruptedString))
                return@setOnClickListener
            }
        }
        return convertView
    }

    override fun getChildrenCount(listPosition: Int): Int {
        /*
        TODO: we need to show one item as well to have the click listener and less layout irritation
        return if(expandableListDetail[listPosition].size == 1)
            0
        else
            expandableListDetail[listPosition].size
         */
        return expandableListDetail[listPosition].size
    }

    override fun getGroup(listPosition: Int): List<BaseEvent> {
        return expandableListDetail[listPosition]
    }

    override fun getGroupCount(): Int {
        return expandableListTitle.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getGroupView(listPosition: Int, isExpanded: Boolean,
                              convertView: View?, parent: ViewGroup?): View? {
        var convertView = convertView
        if (convertView == null) {
            val layoutInflater = this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(de.hartz.software.parannoying.offline.R.layout.row_event_header, parent, false)
        }

        val list = expandableListDetail[listPosition]
        val event1 = list.first()
        val eventn = list.last()
        val size = list.size
        val prefix = if (size > 1) "$size x " else ""
        convertView!!.findViewById<TextView>(de.hartz.software.parannoying.offline.R.id.label).text = prefix + event1.eventText

        val createdAtView = convertView.findViewById<TextView>(de.hartz.software.parannoying.offline.R.id.event_time)
        val firstDate = UiHelper.getDateWithTime(event1.createdAtTimestamp * 1000)
        if (size > 1) {
            val endDate = UiHelper.getDateWithTime(eventn.createdAtTimestamp * 1000)
            createdAtView.text = firstDate + " - " + endDate
        } else {
            createdAtView.text = firstDate
        }
        return convertView
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }

}