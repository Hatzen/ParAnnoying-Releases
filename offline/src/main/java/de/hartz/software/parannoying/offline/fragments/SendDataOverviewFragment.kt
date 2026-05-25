package de.hartz.software.parannoying.offline.fragments

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import androidx.lifecycle.lifecycleScope
import com.mikepenz.iconics.view.IconicsImageButton
import de.hartz.software.parannoying.air.gap.model.UseCases
import de.hartz.software.parannoying.core.adapter.GenericActionViewItemAdapter
import de.hartz.software.parannoying.core.extensions.launchActivity
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.getAddMessageIcon
import de.hartz.software.parannoying.core.helper.ui.getForwardIcon
import de.hartz.software.parannoying.core.helper.ui.getSyncMessagesIcon
import de.hartz.software.parannoying.core.model.view.ActionViewItem
import de.hartz.software.parannoying.offline.R
import de.hartz.software.parannoying.offline.activities.offline.ChatActivity
import de.hartz.software.parannoying.offline.activities.offline.ForwardOverviewActivity
import de.hartz.software.parannoying.offline.adapters.view.SendDataOverviewAdapter
import de.hartz.software.parannoying.offline.model.domain.SendMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SendDataOverviewFragment : AbstractMainFragment() {

    lateinit var viewAdapter : SendDataOverviewAdapter
    private lateinit var listView: ListView
    private var tmpSelectedId = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_send_overview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val actions = requireView().findViewById<ListView>(de.hartz.software.parannoying.offline.R.id.actions)
        val context = requireContext()
        val items = listOf(
            ActionViewItem("Send and Receive Messages",
                {
                    startSync()
                },
                context.getSyncMessagesIcon(IconHelper.SMALL_ICON_PRIMARY),
                "Send all messages and receive directly afterwards."
            ),
            ActionViewItem("Receive Messages",
                {
                    airGapAdapter.startReceive(UseCases.Offline.MESSAGE_RECEIVE)
                },
                context.getAddMessageIcon(IconHelper.SMALL_ICON_PRIMARY),
                "Receive messages but do not send them."
            ),
            ActionViewItem("Forward Data",
                {
                    requireActivity().launchActivity<ForwardOverviewActivity>()
                },
                context.getForwardIcon(IconHelper.SMALL_ICON_PRIMARY),
                "Scan data so it gets stored and a third user can scan it. Do not forward userids this way, only if you want all three users to know the keys"
            )
        )

        val adapter = GenericActionViewItemAdapter(
            items, requireContext())
        actions.setAdapter(adapter)
        adapter.notifyDataSetChanged()


        listView = view.findViewById<ListView>(R.id.message_list)
        listView.emptyView = view.findViewById(R.id.emptyElement)

        // TODO: Maybe customize menu: https://www.androhub.com/android-popup-menu/
        registerForContextMenu(listView)

        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            showMessage(position)
        }
        listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id ->
            tmpSelectedId = position
            requireActivity().openContextMenu(listView)
            true
        }
        requireView().findViewById<IconicsImageButton>(R.id.trash).setOnClickListener {
            deleteAllMenuItemClicked()
        }
    }

    fun startSync() {
        val data = ArrayList(offlineStorage.readSendMessage().map { it.encryptedMessage })
        airGapAdapter.startSync(UseCases.Offline.MESSAGES_SYNC.useData(data))
    }

    override fun onResume() {
        super.onResume()
        updateMessageOverview()
    }

    fun deleteAllMenuItemClicked() {
        DialogHelper.showYesNoAlert(requireContext(), "Are you sure to delete all unsent messages?", object: DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    offlineStorage.deleteAllEncryptedMessages()
                    updateMessageOverview()
                }
            }

        })
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            de.hartz.software.parannoying.offline.R.id.send -> {
                sendMessages(tmpSelectedId)
                return true
            }
            de.hartz.software.parannoying.offline.R.id.show -> {
                showMessage(tmpSelectedId)
                return true
            }
            de.hartz.software.parannoying.offline.R.id.delete -> {
                deleteMessages(tmpSelectedId)
                return true
            }
            else -> return super.onContextItemSelected(item)
        }
    }

    fun showMessage(position: Int) {
        val event = getItem(position).get(0)

        val message = event.message.persistenceId
        val user = event.message.relatedDialog!!

        val passParams : (userIntent: Intent) -> Unit = {
            it.putExtra(ChatActivity.EXTRA_USER, user.getUniqueDialogId())
            it.putExtra(ChatActivity.EXTRA_MESSAGE, message)
        }
        requireActivity().launchActivity<ChatActivity>(init = passParams)
    }

    fun deleteMessages(position: Int) {
        getItem(position).forEach {
            val message = it.message
            offlineStorage.removeMessages(listOf(message))
            offlineStorage.removeEncryptedMessage(it)
        }
        updateMessageOverview()
    }

    fun sendMessages(position: Int) {
        val data = getItem(position)
        airGapAdapter.startSend(
            UseCases.Offline.MESSAGE_SEND_SUBSET.useDataWrappers(data)
        )
    }

    fun getItem(position: Int): List<SendMessage> {
        // TODO: Move to adapter as storage might change, so we need the item the user really clicked..
        return viewAdapter.values[position]
    }

    fun updateMessageOverview() {


        lifecycleScope.launch {
            val groupedMessages = withContext(Dispatchers.IO) {
                val sendMessagesNewestToLast = offlineStorage.readSendMessage().reversed()

                // TODO: Why is it.message not the exact same object, mappers context should make it use the same for same persistenceId?
                sendMessagesNewestToLast.groupBy { it.message.persistenceId }.values.toList()
            }
            viewAdapter = SendDataOverviewAdapter(groupedMessages, requireContext())
            listView.setAdapter(viewAdapter)
        }
    }

}
