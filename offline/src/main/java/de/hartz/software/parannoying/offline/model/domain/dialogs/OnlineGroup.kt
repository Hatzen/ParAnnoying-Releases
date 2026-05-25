package de.hartz.software.parannoying.offline.model.domain.dialogs

import android.content.Context
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.offline.model.OfflineStorage
import de.hartz.software.parannoying.offline.model.domain.UserSecurity

class OnlineGroup(
        nickname: String,
        hash: String
    ): BaseDialog(nickname, hash){

    override val className: String
        get() = javaClass.simpleName

    lateinit var groupId: String
    var firebaseEmailIds: List<String> = ArrayList()

    override fun getUsers(): List<User> {
        return firebaseEmailIds.map {
            OfflineStorage.INSTANCE.users.find { user -> user.hash == it }
        }.filterNotNull() // Null instances possible when the user got deleted.
    }

    fun updateSecurityIssues() {
        userSecurityIssues = users.flatMap { it.userSecurityIssues }.toCollection(HashSet())
        userSecurityIssues.add(UserSecurity.ONLINE_GROUP)
    }

    fun getAllOnlineIdsWithoutRelatedUser (): List<String> {
        return firebaseEmailIds.filter {
            OfflineStorage.INSTANCE.users.find { user -> user.hash == it } == null
        }
    }

    fun showWarningForUnknownMembersIfNeeded(context: Context) {
        val unknownMembersCount = getAllOnlineIdsWithoutRelatedUser().size
        if (unknownMembersCount > 0) {
            DialogHelper.showDialog(context, "Unknown Members", "There are $unknownMembersCount unknown members in this online group. These members will not receive any message.")
        }
    }

}