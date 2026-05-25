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
import de.hartz.software.parannoying.core.adapter.GenericActionViewItemAdapter
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.helper.security.DataSecurityHelper
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.getSendIcon
import de.hartz.software.parannoying.core.helper.ui.getShareIcon
import de.hartz.software.parannoying.core.model.view.ActionViewItem
import de.hartz.software.parannoying.online.R
import de.hartz.software.parannoying.online.activities.online.OnlineMainActivity
import de.hartz.software.parannoying.online.adapters.view.MessageOverviewOutboxAdapter
import de.hartz.software.parannoying.online.interfaces.OnlineApplication
import de.hartz.software.parannoying.online.model.OnlineStorage
import de.hartz.software.parannoying.online.model.domain.LoggedEncryptedMessage

class OutboxFragment : AbstractOnlineMessageFragment() {

    lateinit var messageOverviewActivity: OnlineMainActivity

    lateinit var outboxAdapter : MessageOverviewOutboxAdapter
    lateinit var adapter : GenericActionViewItemAdapter
    private lateinit var listView: ListView

    val storage get() = Storage as OnlineStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (app as OnlineApplication).onlineComponents.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_message_overview_outbox, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val actionsListView = view.findViewById<ListView>(R.id.actions)
        val context = requireContext()

        val messages = onlineStorage.readOutboxEncryptedMessages()
        val count = messages.size

        val items = listOfNotNull(

            if(onlineStorage.useGoogleApi)
            ActionViewItem("Scan and send messages",
                {
                    // Scan message.
                    airGapAdapter.startReceive(UseCases.Online.MESSAGE_RECEIVE)
                },
                context.getSendIcon(IconHelper.SMALL_ICON_PRIMARY),
                "Send all messages via google api and move to log."
            ) else null,
            ActionViewItem("Scan and Share messages",
                {
                    airGapAdapter.startReceive(UseCases.Online.MESSAGE_SHARE_RECEIVE)
                },
                context.getShareIcon(IconHelper.SMALL_ICON_PRIMARY),
                "Send all received messages via share"
            )/*,
            // TODO: How to share large data? simply share sd file?
            ActionViewItem("Share messages",
                {

                },
                context.getShareIcon(IconHelper.SMALL_ICON_PRIMARY),
                "Send all outbox messages via google api and move to log"
            )*/
        )

        requireActivity().runOnUiThread {
            adapter = GenericActionViewItemAdapter(items, requireContext())
            actionsListView.setAdapter(adapter)
            adapter.notifyDataSetChanged()
        }

        // setup adapter.
        listView = requireView().findViewById<ListView>(R.id.message_list)
        listView.emptyView = requireView().findViewById(R.id.emptyElement)
        listView.onItemClickListener = AdapterView.OnItemClickListener {
            parent, view, position, id ->
            val message = messages[position]
            clearAndSetSelectedMessageIds(message.persistenceId)
            // gets cleared with activity result.
            messageOverviewActivity.sendViaShare(message.message)
        }
        updateOutbox()

        requireView().findViewById<IconicsImageButton>(R.id.trash).setOnClickListener {
            deleteAllMenuItemClicked()
        }
    }

    fun updateOutbox() {
        if (context != null) {
            val messages = storage.readOutboxEncryptedMessages()
            activity?.runOnUiThread(Runnable {
                outboxAdapter = MessageOverviewOutboxAdapter(messages, securityInterfaceHolder, onlineStorage, requireContext())
                listView.adapter = outboxAdapter
                outboxAdapter.notifyDataSetChanged()
            })
        }
    }

    fun hasTriggeredShare (): Boolean {
        return selectedMessageIds.isNotEmpty()
    }

    fun removeSharedMessage () {
        val dataSecurityHelper = DataSecurityHelper(securityInterfaceHolder)
        // TODO: read selected id directly by filter?
        val messages = storage
            .readOutboxEncryptedMessages()
            .filter{ selectedMessageIds.contains(it.persistenceId) }
            .toSet()
        // Add to log.
        if (storage.readSettings().logEncryptedMessages) {
            messages.forEach {
                val targetUserHash = if (!it.isFile) {
                    dataSecurityHelper.getOnlineIdFromMessage(it.message)
                } else {
                    dataSecurityHelper.getOnlineIdFromFile(it.message)
                }

                val message = LoggedEncryptedMessage(it.message, it.createdAt, targetUserHash = targetUserHash)
                storage.persistLoggedEncryptedMessages(message)
            }
        }
        // Remove from outbox.
        messages.forEach { storage.deleteOutboxEncryptedMessages(it) }

        updateOutbox()
        selectedMessageIds.clear()
    }

    fun deleteAllMenuItemClicked() {
        DialogHelper.showYesNoAlert(requireContext(), "Are you sure to delete all messages received from offline device to send to the internet? You might not be able to receive some again when already deleted at the origin. Use only if you already synced all messages.",
            object: DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        onlineStorage.deleteAllInboxEncryptedMessages()
                        updateOutbox()
                    }
                }
            })
    }
}