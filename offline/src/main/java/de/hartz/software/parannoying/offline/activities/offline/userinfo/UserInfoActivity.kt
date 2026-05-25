package de.hartz.software.parannoying.offline.activities.offline.userinfo

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.hartz.software.parannoying.air.gap.model.UseCases
import de.hartz.software.parannoying.core.extensions.launchActivity
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.helper.security.DataSecurityHelper
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.core.interfaces.di.air.gap.AirGapAdapter
import de.hartz.software.parannoying.offline.R
import de.hartz.software.parannoying.offline.activities.offline.BaseOfflineActivity
import de.hartz.software.parannoying.offline.activities.offline.ChatActivity
import de.hartz.software.parannoying.offline.adapters.view.UserProfileAdapter
import de.hartz.software.parannoying.offline.model.domain.dialogs.BaseDialog
import de.hartz.software.parannoying.offline.model.domain.dialogs.OfflineGroup
import de.hartz.software.parannoying.offline.model.domain.dialogs.OnlineGroup
import de.hartz.software.parannoying.offline.model.domain.dialogs.SimpleDialog
import de.hartz.software.parannoying.offline.model.domain.dialogs.UniqueDialogIdWrapper
import de.hartz.software.parannoying.offline.model.domain.dialogs.User
import de.hartz.software.parannoying.offline.model.domain.events.BaseEvent
import de.hartz.software.parannoying.offline.model.domain.events.SimpleEvent
import de.hartz.software.parannoying.offline.model.domain.events.UserEvent
import javax.inject.Inject

/**
 * Additionally following fields could be interesting:
 * Last received message (include deleted, App Version, ?)
 */
class UserInfoActivity : BaseOfflineActivity() {
    companion object {
        const val EXTRA_USER: String = "EXTRA_USER"
    }

    @Inject
    lateinit var securityInterfaceHolder: SecurityInterfaceHolder
    @Inject
    lateinit var airGapAdapter: AirGapAdapter

    lateinit var dialog: BaseDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offline_user_info)

        app.offlineComponents.inject(this)

        val currentUsersId = intent.getSerializableExtra(EXTRA_USER) as UniqueDialogIdWrapper
        dialog = currentUsersId.getDialog(Storage)
        val dialog = dialog

        val recyclerView = findViewById<RecyclerView>(R.id.profileRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val userItems = ArrayList(listOfNotNull(
            UserProfileItem.ProfileImage(dialog.dialogPhoto),
            UserProfileItem.SectionHeader("User Info"),
            UserProfileItem.StaticInfo("Original Name", dialog.originalName),
            UserProfileItem.StaticInfo("Display Name", dialog.nickname),
            UserProfileItem.StaticInfo("Online Id", dialog.hash),
            UserProfileItem.StaticInfo("Using google API", if(DataSecurityHelper(securityInterfaceHolder).isOnlineIdValid(dialog.hash)) "Yes" else "NO"),
            if (dialog is User) UserProfileItem.StaticInfo("Number of Unconfirmed messages", dialog.unconfirmedGeneratedSendTokensForDecryption.size.toString()) else null,

            UserProfileItem.SectionHeader("Actions"),
            UserProfileItem.Action("Open Chat", { showChat() }), // Message icon
            UserProfileItem.Action("Change Name", { renameUser() }),
            UserProfileItem.Action("Delete User", { deleteUser() }),
            if (dialog is OfflineGroup || dialog is OnlineGroup) UserProfileItem.Action("Show GroupId", { showGroupId() }) else null,
            if (dialog is OfflineGroup || dialog is OnlineGroup) UserProfileItem.Action("Add Member", { addMember() }) else null,
            if (dialog is User) UserProfileItem.Action("Reset Keys", {}) else null, // <i class="fa-solid fa-clock-rotate-left"></i>
        ))

        if (dialog is SimpleDialog) {
            userItems.add(UserProfileItem.SectionHeader("Security Issues"))

            val items = dialog.userSecurityIssues.map { UserProfileItem.StaticInfo(null, it.description) }

            if (items.isNotEmpty()) {
                userItems.addAll(items)
            } else {
                userItems.add(UserProfileItem.StaticInfo("", "No security issues detected so far"))
            }

        }
        if (dialog is User) {
            userItems.add(UserProfileItem.SectionHeader("Member of Groups"))

            val items = Storage.onlineGroups
                .filter { it.users.contains(dialog) }
                .map {
                    val bitmap = IOHelper.getProfilePictureForUser(it.dialogPhoto, this)
                        .toDrawable(resources)
                    UserProfileItem.Action(it.nickname, { showUserInfo(it) }, bitmap)
                }

            if (items.isNotEmpty()) {
                userItems.addAll(items)
            } else {
                userItems.add(UserProfileItem.StaticInfo("", "Not part of any group"))
            }
        }
        if (dialog is OnlineGroup) {
            val dialogs = dialog.getUsers()

            userItems.add(UserProfileItem.SectionHeader("Group Members"))

            userItems.addAll(
                dialogs.map {
                    val bitmap = IOHelper.getProfilePictureForUser(it.dialogPhoto, this).toDrawable(resources)
                    UserProfileItem.Action(it.nickname, { showUserInfo(it) }, bitmap)
                }
            )
        }
        if (dialog is OfflineGroup) {
            val messages = Storage.getAllMessagesForUser(dialog)
            val onlineIds = messages.map { it.sender }.filterNotNull()

            userItems.add(UserProfileItem.SectionHeader("Potential Group members (cannot be verified)"))

            val items = onlineIds.map {
                    val bitmap = IOHelper.getProfilePictureForUser(it.dialogPhoto, this).toDrawable(resources)
                    UserProfileItem.Action(it.nickname, { showUserInfo(it) }, bitmap)
                }

            if (items.isNotEmpty()) {
                userItems.addAll(items)
            } else {
                userItems.add(UserProfileItem.StaticInfo("", "Cannot determine any members"))
            }

        }

        recyclerView.adapter = UserProfileAdapter(userItems, this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp() : Boolean {
        // Enable Back button.
        finish()
        return true
    }

    private fun addMember () {
        DialogHelper.showAlert(this, "Groups cannot be changed afterwards as there is no way to refresh other offline devices.")
    }

    private fun renameUser () {
        val callback = object: DialogHelper.InputDialogCallback() {
            override fun onFinish(input: String) {
                val user = dialog.getDialog(Storage)
                user.nickname = input
                Storage.updateDialog(user)
                Storage.persistEvent(UserEvent(BaseEvent.EVENT_RENAMED_USER, user))
            }
        }
        DialogHelper.showInputDialog(this, "Enter the new name for the user.", true, callback)
    }

    private fun showUserInfo (dialog: BaseDialog) {
        val passParams : (userIntent: Intent) -> Unit = {
            it.putExtra(ChatActivity.EXTRA_USER, dialog.getUniqueDialogId())
        }
        launchActivity<UserInfoActivity>(init = passParams)
    }

    private fun deleteUser () {
        DialogHelper.showYesNoAlert(this, "Are you sure to delete this user?", object: DialogInterface.OnClickListener {
            override fun onClick(d: DialogInterface?, which: Int) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    val dialog = dialog.getDialog(Storage)
                    Storage.deleteDialog(dialog)
                    Storage.persistEvent(SimpleEvent(BaseEvent.EVENT_DELETED_USER))
                    // TODO: Simple finish is not enough when coming from chatactivity
                    finish()
                }
            }
        })
    }

    private fun showGroupId() {
        val groupId: String
        if (dialog is OfflineGroup) {
            groupId = (dialog as OfflineGroup).groupId
        } else if (dialog is OnlineGroup) {
            groupId = (dialog as OnlineGroup).groupId
        } else {
            throw RuntimeException("User has not a groupId.")
        }
        airGapAdapter.startSend(UseCases.Offline.OFFLINE_GROUP_SEND.useText(groupId))
    }

    private fun showChat() {
        val passParams : (userIntent: Intent) -> Unit = {
            it.putExtra(ChatActivity.EXTRA_USER, dialog.getUniqueDialogId())
        }
        launchActivity<ChatActivity>(init = passParams)
    }
}
