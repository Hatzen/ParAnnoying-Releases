package de.hartz.software.parannoying.offline.businesslogic

import android.content.Context
import android.content.DialogInterface
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.offline.helper.security.DialogCreationHelper
import de.hartz.software.parannoying.offline.model.OfflineStorage
import de.hartz.software.parannoying.offline.model.domain.dialogs.CurrentUser
import de.hartz.software.parannoying.offline.model.domain.events.BaseEvent
import de.hartz.software.parannoying.offline.model.domain.events.UserEvent

class CreateUserProcessor(val applicationContext: Context, val securityInterfaceHolder: SecurityInterfaceHolder) {

    private val storage: OfflineStorage
    private val currentUser: CurrentUser

    init {
        storage = applicationContext.app.Storage as OfflineStorage
        currentUser = storage.currentUser
    }

    fun addUser(decryptedText: String, callback: () -> Unit) {
        val scannedUser = DialogCreationHelper(applicationContext, securityInterfaceHolder).createABaseDialogFromUserId(decryptedText, applicationContext)
        // Save only if the user doesnt exists yet.
        try {
            storage.addDialog(scannedUser)
            storage.persistEvent(UserEvent(BaseEvent.EVENT_ADDED_USER, scannedUser))
            callback()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            // TODO: This happens when 2 users have same onlineID, we should check if they are the same or at least show the user the differences..
            DialogHelper.showYesNoAlert(applicationContext, "User ${scannedUser.originalName} already exists. Do you want to replace it?",
                DialogInterface.OnClickListener { _, choice ->
                    if (choice == DialogInterface.BUTTON_POSITIVE) {
                        storage.forceAddDialog(scannedUser)
                        callback()
                    }
                })
        }
    }
}