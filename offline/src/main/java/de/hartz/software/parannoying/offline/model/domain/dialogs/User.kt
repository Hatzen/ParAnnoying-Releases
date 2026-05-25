package de.hartz.software.parannoying.offline.model.domain.dialogs

import com.stfalcon.chatkit.commons.models.IUser


open class User(
nickname: String,
hash: String
): SimpleDialog(nickname, hash), IUser {

    override val className: String
        get() = javaClass.simpleName

    override fun getUsers(): MutableList<out IUser> {
        return mutableListOf(this)
    }

    override fun getName(): String {
        return nickname
    }

    override fun getAvatar(): String {
        // TODO: This will get called to display the user of the last message
        // dialogStyle.isDialogMessageAvatarEnabled() in com.stfalcon.chatkit.dialogs.DialogsListAdapter
        return getDialogPhoto()
    }

    fun isCurrentUser (): Boolean {
        return this is CurrentUser
        // TODO: Do we need this? // || persistenceId == OfflineStorage.INSTANCE.currentUser.persistenceId
    }

    fun isUnconfirmedUser(): Boolean {
        return decryptionKeyCloakForUser == null
    }

}