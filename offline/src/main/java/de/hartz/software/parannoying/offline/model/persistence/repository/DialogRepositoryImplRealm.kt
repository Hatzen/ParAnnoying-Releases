package de.hartz.software.parannoying.offline.model.persistence.repository

import android.util.Log
import de.hartz.software.parannoying.core.model.persistence.realm.RealmHelper
import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject
import de.hartz.software.parannoying.offline.interfaces.repository.DialogRepository
import de.hartz.software.parannoying.offline.model.domain.DecryptionKeyCloakForUser
import de.hartz.software.parannoying.offline.model.domain.dialogs.BaseDialog
import de.hartz.software.parannoying.offline.model.domain.dialogs.CurrentUser
import de.hartz.software.parannoying.offline.model.domain.dialogs.OfflineGroup
import de.hartz.software.parannoying.offline.model.domain.dialogs.OnlineGroup
import de.hartz.software.parannoying.offline.model.domain.dialogs.SimpleDialog
import de.hartz.software.parannoying.offline.model.domain.dialogs.UnknownUser
import de.hartz.software.parannoying.offline.model.domain.dialogs.User
import de.hartz.software.parannoying.offline.model.mapper.DeviceDataMapper
import de.hartz.software.parannoying.offline.model.persistence.*
import de.hartz.software.parannoying.offline.model.persistence.dialogs.OnlineGroupPersistence
import de.hartz.software.parannoying.offline.model.persistence.dialogs.UserPersistence
import io.realm.RealmObject


class DialogRepositoryImplRealm(val realmHelper: RealmHelper): DialogRepository {

    override val currentUser: CurrentUser
        get() = getDialogs().filterIsInstance<CurrentUser>().firstOrNull()!!

    override val users get() = getDialogs().filterIsInstance<User>()
    override val onlineGroups get() = getDialogs().filterIsInstance<OnlineGroup>()
    override val offlineGroups get() = getDialogs().filterIsInstance<OfflineGroup>()

    // DecryptionKeyCloakForUser
    // TODO: Probably a Stack (LIFO) is better for abandomed keys?

    override fun readUnconfirmedKeySet(): Set<DecryptionKeyCloakForUser> {
        val store = realmHelper.getThreadInstance()
        val storeData = store.where(DecryptionKeyCloakForUserPersistence::class.java)
            .findAll()
        val result = store.copyFromRealm(storeData).map { DeviceDataMapper(realmHelper).toDomain(it) }.toSet()
        store.close()
        return result
    }

    override fun persistUnconfirmedKey(inboxEncryptedMessage: DecryptionKeyCloakForUser) {
        val storeData = DeviceDataMapper(realmHelper).toPersistence(inboxEncryptedMessage)
        val store = realmHelper.getThreadInstance()
        store.executeTransaction{
            it.copyToRealmOrUpdate(storeData)
        }
        store.close()
    }

    override fun deleteUnconfirmedKey(inboxEncryptedMessage: DecryptionKeyCloakForUser) {
        val storeData = DeviceDataMapper(realmHelper).toPersistence(inboxEncryptedMessage)
        val store = realmHelper.getThreadInstance()
        store.executeTransaction{
            val storeData = store.where(DecryptionKeyCloakForUserPersistence::class.java).equalTo("persistenceId", inboxEncryptedMessage.persistenceId).findAll()
            storeData.deleteAllFromRealm()
        }
        store.close()
    }

    override fun deleteAllUnconfirmedKeys() {
        val store = realmHelper.getThreadInstance()
        store.executeTransaction {
            it.delete(DecryptionKeyCloakForUserPersistence::class.java)
        }
        store.close()
    }

    override fun deleteDialog(user: BaseDialog) {
        val store = realmHelper.getThreadInstance()
        store.executeTransaction{
            val data: RealmObject
            val classToDelete: Class<out RealmObject>
            if (user is SimpleDialog) {
                data = DeviceDataMapper(realmHelper).toPersistence(user)
                classToDelete = UserPersistence::class.java
            } else if (user is OnlineGroup) {
                data = DeviceDataMapper(realmHelper).toPersistence(user)
                classToDelete = OnlineGroupPersistence::class.java
            } else {
                throw RuntimeException("not supported")
            }
            val storeData = store.where(classToDelete)
                .equalTo("persistenceId", (data as UniqueRealmObject).persistenceId)
                .findAll()
            storeData.deleteAllFromRealm()
        }
        store.close()
    }

    override fun updateCurrentUser(){
        updateDialog(currentUser)
    }

    @Throws(IllegalArgumentException::class)
    override fun updateDialog(user: BaseDialog) {
        if (!getDialogs().contains(user)) {
            throw IllegalArgumentException("User updated does not exist")
        }
        Log.e(javaClass.simpleName, "Storage updateDialog " + realmHelper.FILE_NAME)
        val store = realmHelper.getThreadInstance()
        store.executeTransaction{
            val data: RealmObject
            if (user is SimpleDialog) {
                data = DeviceDataMapper(realmHelper).toPersistence(user)
            } else if (user is OnlineGroup) {
                data = DeviceDataMapper(realmHelper).toPersistence(user)
            } else {
                throw RuntimeException("not supported")
            }
            it.copyToRealmOrUpdate(data)
        }
        store.close()
    }

    @Throws(IllegalArgumentException::class)
    override fun addDialog(user: BaseDialog) {
        if (getDialogs().contains(user)) {
            throw IllegalArgumentException("Duplicate dialog entry not accepted")
        }
        forceAddDialog(user)
    }

    override fun forceAddDialog(user: BaseDialog) {
        Log.e(javaClass.simpleName, "Storage addDialog " + realmHelper.FILE_NAME)
        val store = realmHelper.getThreadInstance()
        store.executeTransaction{
            val data: RealmObject
            if (user is SimpleDialog) {
                data = DeviceDataMapper(realmHelper).toPersistence(user)
            } else if (user is OnlineGroup) {
                data = DeviceDataMapper(realmHelper).toPersistence(user)
            } else {
                throw RuntimeException("not supported")
            }
            it.copyToRealmOrUpdate(data)
        }
        store.close()
    }

    override fun getDialogs(): List<BaseDialog> {
        Log.e(javaClass.simpleName, "Storage getDialogs " + realmHelper.FILE_NAME)
        val store = realmHelper.getThreadInstance()
        val storeData = store.where(UserPersistence::class.java)
            .findAll()
        val result = store.copyFromRealm(storeData)
            .map { DeviceDataMapper(realmHelper).toDomain(it) }
            .toMutableList()

        val storeData2 = store.where(OnlineGroupPersistence::class.java)
            .findAll()
        val result2 = store.copyFromRealm(storeData2)
            .map { DeviceDataMapper(realmHelper).toDomain(it) }
            .toList()

        result.addAll(result2);

        store.close()

        result.sortByDescending {
            it.lastMessageToDisplay?.createdAtTimestamp ?: 0
        }
        return result.filter { it !is UnknownUser }
    }

}