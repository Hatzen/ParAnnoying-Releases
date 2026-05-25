package de.hartz.software.parannoying.online.fragments.message.overview

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import com.mikepenz.iconics.view.IconicsImageButton
import de.hartz.software.parannoying.air.gap.model.UseCases
import de.hartz.software.parannoying.air.gap.model.UseCases.Online.MESSAGES_SYNC
import de.hartz.software.parannoying.core.adapter.GenericActionViewItemAdapter
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.helper.security.DataSecurityHelper
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.core.helper.ui.getSyncMessagesIcon
import de.hartz.software.parannoying.core.model.view.ActionViewItem
import de.hartz.software.parannoying.online.R
import de.hartz.software.parannoying.online.adapters.view.MessageOverviewInboxAdapter
import de.hartz.software.parannoying.online.interfaces.OnlineApplication
import de.hartz.software.parannoying.online.model.OnlineStorage
import de.hartz.software.parannoying.online.model.domain.LoggedEncryptedMessage


class InboxFragment : AbstractOnlineMessageFragment() {

    lateinit var inboxAdapter : MessageOverviewInboxAdapter

    private lateinit var listView: ListView
    lateinit var adapter : GenericActionViewItemAdapter
    val storage get() = Storage as OnlineStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (app as OnlineApplication).onlineComponents.inject(this)
    }

    override fun onResume() {
        super.onResume()
        updateInbox()
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_message_overview_inbox, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val actionsListView = view.findViewById<ListView>(R.id.actions)
        val context = requireContext()

        val inboxEncryptedMessages = onlineStorage.readInboxEncryptedMessages()
        val count = inboxEncryptedMessages.size

        val items = listOf(
            ActionViewItem("Sync messages ($count)",
                { startSync() },
                context.getSyncMessagesIcon(IconHelper.SMALL_ICON_PRIMARY),
                "Send all inbox to offline device and put all received into outbox or log."
            )
        )

        requireActivity().runOnUiThread {
            adapter = GenericActionViewItemAdapter(items, requireContext())
            actionsListView.setAdapter(adapter)
            adapter.notifyDataSetChanged()
        }

        // setup adapter.
        listView = requireView().findViewById<ListView>(R.id.message_list)
        listView.emptyView = requireView().findViewById(R.id.emptyElement)
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val message = inboxEncryptedMessages[position]
            clearAndSetSelectedMessageIds(message.persistenceId)
            airGapAdapter.startSend(UseCases.Online.MESSAGE_SEND.useText(message.message))
        }
        updateInbox()

        requireView().findViewById<IconicsImageButton>(R.id.trash).setOnClickListener {
            deleteAllMenuItemClicked()
        }

    }

    fun startSync() {
        val inboxEncryptedMessages = onlineStorage.readInboxEncryptedMessages()
        val messagesToSend = inboxEncryptedMessages.map { it.message }.toMutableList()
        if (messagesToSend.size > 0) {
            selectedMessageIds = inboxEncryptedMessages.map { it.persistenceId }.toMutableList()
        } else {
            UiHelper.showToastFromBackgroundTask(requireContext(), "No messages to transfer to offline device.")
        }
        airGapAdapter.startSync(MESSAGES_SYNC.useData(messagesToSend))
    }

    fun deleteAllMenuItemClicked() {
        DialogHelper.showYesNoAlert(requireContext(), "Are you sure to delete all messages received from the internet so far? You might not be able to receive some again when already deleted at the origin. Use only if you already synced all messages.",
            object: DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    onlineStorage.deleteAllInboxEncryptedMessages()
                    updateInbox()
                }
            }

        })
    }

    fun updateInbox() {
        // Needed when view is not visible so fragment is not initialized like when returning from sendActivity.
        if (context != null) {
            val inboxEncryptedMessages = storage.readInboxEncryptedMessages()
            activity?.runOnUiThread(Runnable {
                inboxAdapter = MessageOverviewInboxAdapter(securityInterfaceHolder, inboxEncryptedMessages, requireContext())
                listView.adapter = inboxAdapter
                inboxAdapter.notifyDataSetChanged()
            })
        }
    }


    fun handleSyncSendMessages() {
        val inboxEncryptedMessages = onlineStorage.readInboxEncryptedMessages()
        val logEncryptedMessages = onlineStorage.readSettings().logEncryptedMessages
        val syncedMessages = inboxEncryptedMessages
            .filter{ selectedMessageIds.contains(it.persistenceId) }
            .toSet()

        val dataSecurityHelper = DataSecurityHelper(securityInterfaceHolder)
        syncedMessages.forEach {
            val targetUserHash = if (!it.isFileMessage) {
                dataSecurityHelper.getOnlineIdFromMessage(it.message)
            } else {
                dataSecurityHelper.getOnlineIdFromFile(it.message)
            }
            val loggedMessage = LoggedEncryptedMessage(it.message, it.sendAt, it.receivedAt, it.isFileMessage, targetUserHash)
            if (logEncryptedMessages) {
                onlineStorage.persistLoggedEncryptedMessages(loggedMessage)
            }
        }
        // TODO: why is this leading to Realm Problems with onPause
        //  onlineInterface?.removeAllListeners() seems to be related as onPause Issue of BaseOfflineActivity...
        // onlineStorage.deleteInboxEncryptedMessagesByIds(syncedMessages.map { it.persistenceId })
        onlineStorage.deleteAllInboxEncryptedMessages()
        updateInbox()
    }
}