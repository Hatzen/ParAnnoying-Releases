package de.hartz.software.parannoying.offline.fragments

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ListView
import com.mikepenz.iconics.view.IconicsImageButton
import de.hartz.software.parannoying.air.gap.model.UseCases
import de.hartz.software.parannoying.core.adapter.GenericActionViewItemAdapter
import de.hartz.software.parannoying.core.extensions.launchActivity
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.core.helper.ui.getGroupIcon
import de.hartz.software.parannoying.core.helper.ui.getUserIcon
import de.hartz.software.parannoying.core.helper.ui.getUserIdIcon
import de.hartz.software.parannoying.core.model.view.ActionViewItem
import de.hartz.software.parannoying.offline.R
import de.hartz.software.parannoying.offline.activities.offline.CreateOnlineGroupActivity
import de.hartz.software.parannoying.offline.adapters.view.UserOverviewAdapter
import de.hartz.software.parannoying.offline.helper.security.DialogCreationHelper
import de.hartz.software.parannoying.offline.model.domain.DecryptionKeyCloakForUser

class UserOverviewFragment : AbstractMainFragment() {

    companion object {
        // TODO dont use static..
        // Avoid getting garbage collected when showing userId.
        private var unconfirmedDecryptionKeyCloakForUser: DecryptionKeyCloakForUser? = null
    }

    private lateinit var listView: ListView
    lateinit var adapter : GenericActionViewItemAdapter
    lateinit var viewAdapter : UserOverviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_users_overview, container, false)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        listView = view.findViewById(R.id.actions)
        val context = requireContext()
        val items = listOf(
            ActionViewItem("Add User",
                { airGapAdapter.startReceive(UseCases.Offline.USERID_RECEIVE) },
                context.getUserIcon(IconHelper.SMALL_ICON_PRIMARY),
                "Adding a user"
            ),
            ActionViewItem("Create offline group",
                {
                    // TODO: application context needed?
                    DialogHelper.showInputDialog(context,
                        "Enter name for insecure group",
                        false, object : DialogHelper.InputDialogCallback() {
                            override fun onFinish(input: String) {
                                DialogCreationHelper(
                                    context,
                                    securityInterfaceHolder
                                ).createAndStoreOfflineGroup(input, context)
                                // this@UserOverviewFragment.initAdapter()
                            }
                        })
                },
                context.getGroupIcon(IconHelper.SMALL_ICON_ACCENT),
                "Creating an offline group, which is ABSOLUTLEY insecure but can be used with people who dont know each others userids."
            ),
            ActionViewItem("Create online group",
                {
                    if (offlineStorage.users.size == 1) {
                        UiHelper.showToastFromBackgroundTask(requireContext(), "You need more contacts to create a group.")
                        return@ActionViewItem
                    }
                    val passParams : (intent: Intent) -> Unit = {
                        it.putExtra(SelectUsersFragment.EXTRA_USER_IDS, ArrayList<Long>())
                    }
                    requireActivity().launchActivity<CreateOnlineGroupActivity>(init = passParams)
                },
                context.getGroupIcon(IconHelper.SMALL_ICON_PRIMARY),
                "Creating an online group, you select users you already know to chat to."
            ),
            ActionViewItem("Show UserId",
                {
                    showUserId()
                },
                // TODO: switch color to accent when no google api?
                context.getUserIdIcon(IconHelper.SMALL_ICON_PRIMARY),
                "Shows your userId, creates unique keys for other users to scan. Keys only get persisted when you confirm the scanning process"
            )

        )

        requireActivity().runOnUiThread {
            adapter = GenericActionViewItemAdapter(
                items, requireContext())
            listView.setAdapter(adapter)
            adapter.notifyDataSetChanged()
        }

        listView = view.findViewById<ListView>(R.id.user_id_list)
        listView.emptyView = view.findViewById(R.id.emptyElement)

        updateInbox()

        requireView().findViewById<IconicsImageButton>(R.id.trash).setOnClickListener {
            deleteAllMenuItemClicked()
        }

    // TODO: Maybe show number of users, online groups and offline groups

    }

    override fun onResume() {
        super.onResume()
        updateInbox()
        val progressBar = requireView().findViewById<FrameLayout>(R.id.progress_overlay)
        progressBar.visibility = View.GONE
    }

    fun storeUserId() {
        offlineStorage.persistUnconfirmedKey(unconfirmedDecryptionKeyCloakForUser!!)
        unconfirmedDecryptionKeyCloakForUser = null
    }

    fun showUserId() {
        val progressBar = requireView().findViewById<FrameLayout>(R.id.progress_overlay)
        progressBar.visibility = View.VISIBLE
        Handler().post {
            // TODO: get real application context
            val applicationContext = requireContext()
            val keyTuple = DialogCreationHelper(
                applicationContext,
                securityInterfaceHolder
            ).getRandomUserId(applicationContext)
            val userId = keyTuple.first
            // TODO: Pass data userId
            val decryptionKeyCloakForUser = keyTuple.second
            unconfirmedDecryptionKeyCloakForUser = decryptionKeyCloakForUser
            airGapAdapter.startSend(UseCases.Offline.USERID_SEND.useText(userId))
        }
    }

    fun updateInbox() {
        val keys = offlineStorage.readUnconfirmedKeySet().reversed()
        requireActivity().runOnUiThread {
            viewAdapter = UserOverviewAdapter(keys, securityInterfaceHolder.hashHelper, requireContext())
            listView.setAdapter(viewAdapter)
            viewAdapter.notifyDataSetChanged()
        }
    }

    fun deleteAllMenuItemClicked() {
        DialogHelper.showYesNoAlert(requireContext(), "Are you sure to delete all unconfirmed keys? You wont be able to read messages from users who did not sent you a message so far. Only use this option if decrypting takes to long.", object: DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    offlineStorage.deleteAllUnconfirmedKeys()
                    updateInbox()
                }
            }

        })
    }
}
