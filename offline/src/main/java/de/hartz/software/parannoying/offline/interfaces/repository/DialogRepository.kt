package de.hartz.software.parannoying.offline.interfaces.repository

import de.hartz.software.parannoying.offline.model.domain.DecryptionKeyCloakForUser
import de.hartz.software.parannoying.offline.model.domain.dialogs.BaseDialog
import de.hartz.software.parannoying.offline.model.domain.dialogs.CurrentUser
import de.hartz.software.parannoying.offline.model.domain.dialogs.OfflineGroup
import de.hartz.software.parannoying.offline.model.domain.dialogs.OnlineGroup
import de.hartz.software.parannoying.offline.model.domain.dialogs.User
import de.hartz.software.parannoying.offline.model.persistence.*


interface DialogRepository {

    val currentUser: CurrentUser

    val users: List<User>
    val onlineGroups: List<OnlineGroup>
    val offlineGroups: List<OfflineGroup>

    // DecryptionKeyCloakForUser

    fun readUnconfirmedKeySet(): Set<DecryptionKeyCloakForUser>

    fun persistUnconfirmedKey(inboxEncryptedMessage: DecryptionKeyCloakForUser)

    fun deleteUnconfirmedKey(inboxEncryptedMessage: DecryptionKeyCloakForUser)

    fun deleteAllUnconfirmedKeys()

    fun deleteDialog(user: BaseDialog)

    fun updateCurrentUser()

    @Throws(IllegalArgumentException::class)
    fun updateDialog(user: BaseDialog)

    @Throws(IllegalArgumentException::class)
    fun addDialog(user: BaseDialog)

    fun forceAddDialog(user: BaseDialog)

    fun getDialogs(): List<BaseDialog>

}