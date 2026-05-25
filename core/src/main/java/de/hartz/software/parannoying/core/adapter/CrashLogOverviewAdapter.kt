package de.hartz.software.parannoying.core.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import de.hartz.software.parannoying.core.R
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ActivityPurpose
import de.hartz.software.parannoying.core.interfaces.di.air.gap.AirGapAdapter
import de.hartz.software.parannoying.core.interfaces.di.air.gap.DeviceTarget
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ExchangeDataWrapper
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ISendLaunchOptions
import de.hartz.software.parannoying.core.model.domain.CrashLog
import kotlin.math.min

class CrashLogOverviewAdapter(values : List<CrashLog>, val myContext:Context, val serviceHolder: SecurityInterfaceHolder, val airGapAdapter: AirGapAdapter)
        : ArrayAdapter<CrashLog>(myContext, R.layout.row_crashlog) {
    var values: List<CrashLog> = values

    override fun getCount(): Int {
        return values.size
    }

    override fun getView(position:Int, convertView:View?, parent:ViewGroup): View {
        val inflater = myContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = inflater.inflate(R.layout.row_crashlog, parent, false)

        val crashlog = values[position]
        rowView.findViewById<TextView>(R.id.label).text = "DataHash " + serviceHolder.hashHelper.getStringHashForUi(crashlog.log)

        val noteView = rowView.findViewById<TextView>(R.id.note)

        noteView.text = extractLastStacktraceCausedByFromErrorJSON(crashlog.log)

        val createdAtView = rowView.findViewById<TextView>(R.id.event_time)
        createdAtView.text = UiHelper.getDateWithTime(crashlog.createdAtTimestamp * 1000)

        rowView.setOnClickListener { view ->
            airGapAdapter.startSend(getUseCase(crashlog.log))
        }

        return rowView
    }


    fun extractLastStacktraceCausedByFromErrorJSON(acraErrorJson: String): String {
        val cause = "Caused by:"
        val trace = "STACK_TRACE\":\""
        var relevantLine = acraErrorJson
        if (relevantLine.contains(trace)) {
            val index = acraErrorJson.lastIndexOf(trace)
            // TODO: Order may change probably best to parse json and take item properly
            val nextItem = "\"USER_APP_START_DATE\""
            val index2 = acraErrorJson.indexOf(nextItem, index)
            relevantLine = acraErrorJson.substring(index, index2)
        }
        if (relevantLine.contains(cause)) {
            val index = relevantLine.lastIndexOf(cause)
            val index2 = relevantLine.indexOf("\\n", index)
            relevantLine = relevantLine.substring(index, index2)
        }
        return relevantLine.substring(0, min(relevantLine.length, 200))
    }

    private fun getUseCase(data: String): ISendLaunchOptions {
        // TODO: Move usecases to core or split up.
        return object: ISendLaunchOptions {
            override val requestCode = 1234455
            override var singleData: ExchangeDataWrapper? = object : ExchangeDataWrapper {
                override val exchangeData = data
                override val isFile = false
            }
            override var multipleData: List<ExchangeDataWrapper>? = listOf(singleData!!)
            override val text = ""
            override val purpose = ActivityPurpose.CRASH
            override val target = DeviceTarget.ANY
            override val additionalEncryption = false
            override val confirmAndCancle = false
            override val data: List<ExchangeDataWrapper> = multipleData!!

            override fun useText(data: String): ISendLaunchOptions {
                TODO("Not yet implemented")
            }

            override fun useFile(data: String): ISendLaunchOptions {
                TODO("Not yet implemented")
            }

            override fun useDataWrappers(data: List<ExchangeDataWrapper>): ISendLaunchOptions {
                TODO("Not yet implemented")
            }

            override fun useData(data: List<String>): ISendLaunchOptions {
                TODO("Not yet implemented")
            }

            override val token: String?
                get() = TODO("Not yet implemented")

        }
    }
}