package de.hartz.software.parannoying.online.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import androidx.fragment.app.Fragment
import de.hartz.software.parannoying.air.gap.model.UseCases
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.interfaces.di.air.gap.AirGapAdapter
import de.hartz.software.parannoying.online.R
import de.hartz.software.parannoying.online.adapters.view.MessageOverviewLogAdapter
import de.hartz.software.parannoying.online.interfaces.OnlineApplication
import de.hartz.software.parannoying.online.model.OnlineStorage
import javax.inject.Inject

class LogFragment : Fragment() {

    lateinit var logAdapter : MessageOverviewLogAdapter
    lateinit var listView: ListView

    @Inject
    lateinit var airGapAdapter: AirGapAdapter
    @Inject
    lateinit var securityInterfaceHolder: SecurityInterfaceHolder
    val onlineStorage get() = Storage as OnlineStorage


    private val KEY_MESSAGE_ID: String = "KEY_MESSAGE_ID"
    var selectedMessageIds = mutableListOf<Long>()

    fun clearAndSetSelectedMessageIds(id: Long) {
        clearAndSetSelectedMessageIds(arrayListOf(id))
    }

    fun clearAndSetSelectedMessageIds(ids: ArrayList<Long>) {
        selectedMessageIds = ids
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // TODO: Do we need a specific list for every fragment? (Can we return to a different fragment for example?)
        outState.putSerializable(KEY_MESSAGE_ID, ArrayList(selectedMessageIds))
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        val saved = savedInstanceState?.getSerializable(KEY_MESSAGE_ID) as java.util.ArrayList<Long>?
        if (saved != null) {
            clearAndSetSelectedMessageIds(saved)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (app as OnlineApplication).onlineComponents.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_message_overview_log, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // TODO: this should be read looged messages, shouldnt it?
        val messages = onlineStorage.readInboxEncryptedMessages()
        // setup adapter.
        listView = requireView().findViewById<ListView>(R.id.chat_list)
        listView.emptyView = requireView().findViewById(R.id.emptyElement)
        listView.onItemClickListener = AdapterView.OnItemClickListener {
            parent, view, position, id ->
            val message = messages[position]
            clearAndSetSelectedMessageIds(message.persistenceId)
            airGapAdapter.startSend(UseCases.Online.MESSAGE_SEND.useText(message.message))
        }
        updateLog()
    }

    fun updateLog() {
        if (context != null) {
            val messages = onlineStorage.readLoggedEncryptedMessages()
            activity?.runOnUiThread(Runnable {
                logAdapter = MessageOverviewLogAdapter(messages, onlineStorage, securityInterfaceHolder, requireContext())
                listView.adapter = logAdapter
                logAdapter.notifyDataSetChanged()
            })
        }
    }
}