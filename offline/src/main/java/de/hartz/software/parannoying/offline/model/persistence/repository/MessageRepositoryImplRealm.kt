package de.hartz.software.parannoying.offline.model.persistence.repository

import de.hartz.software.parannoying.core.model.persistence.realm.RealmHelper
import de.hartz.software.parannoying.core.model.persistence.realm.RealmHelper.Companion.resolveAllRealmReferences
import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject
import de.hartz.software.parannoying.offline.interfaces.repository.MessageRepository
import de.hartz.software.parannoying.offline.model.domain.SendMessage
import de.hartz.software.parannoying.offline.model.domain.dialogs.BaseDialog
import de.hartz.software.parannoying.offline.model.domain.dialogs.OnlineGroup
import de.hartz.software.parannoying.offline.model.domain.dialogs.UniqueDialogId
import de.hartz.software.parannoying.offline.model.domain.dialogs.User
import de.hartz.software.parannoying.offline.model.domain.messages.AbstractMessage
import de.hartz.software.parannoying.offline.model.domain.messages.UserMessage
import de.hartz.software.parannoying.offline.model.mapper.DeviceDataMapper
import de.hartz.software.parannoying.offline.model.persistence.SendMessagePersistence
import de.hartz.software.parannoying.offline.model.persistence.dialogs.OnlineGroupPersistence
import de.hartz.software.parannoying.offline.model.persistence.dialogs.UserPersistence
import de.hartz.software.parannoying.offline.model.persistence.message.MessagePersistence
import io.realm.RealmQuery
import io.realm.Sort
import kotlin.math.min


class MessageRepositoryImplRealm(val realmHelper: RealmHelper): MessageRepository {

    companion object {
        val FIELD_PARTIAL_MESSAGE = RealmHelper.toFieldSelector(MessagePersistence::partialMessage)
        val FIELD_PERSISTENCE_ID = RealmHelper.toFieldSelector(MessagePersistence::persistenceId)
        val FIELD_CREATED_AT_TIMESTAMP = RealmHelper.toFieldSelector(MessagePersistence::createdAtTimestamp)
        val FIELD_MESSAGE_READ = RealmHelper.toFieldSelector(MessagePersistence::messageRead)
        val FIELD_TARGET_DIALOG_USER_PERSISTENCE_ID = RealmHelper.toFieldSelector(MessagePersistence::targetDialogUser, UserPersistence::persistenceId)
        val FIELD_TARGET_DIALOG_GROUP_PERSISTENCE_ID = RealmHelper.toFieldSelector(MessagePersistence::targetDialogGroup, OnlineGroupPersistence::persistenceId)
    }

    // EncryptedMessages

    override fun readSendMessage(): List<SendMessage> {
        val store = realmHelper.getThreadInstance()
        val storeData = store.where(SendMessagePersistence::class.java)
            .findAll()
        val result = store.copyFromRealm(storeData).map { DeviceDataMapper(realmHelper).toDomain(it) }.toList()
        store.close()
        return result
    }


    override fun addEncryptedMessage(encryptedMessage: SendMessage) {
        val store = realmHelper.getThreadInstance()

        store.executeTransaction{
            it.copyToRealmOrUpdate(DeviceDataMapper(realmHelper).toPersistence(encryptedMessage))
        }
        store.close()
    }

    override fun removeEncryptedMessage(encryptedMessage: SendMessage) {
        val store = realmHelper.getThreadInstance()

        store.executeTransaction{
            val rows = store.where(SendMessagePersistence::class.java)
                .equalTo(FIELD_PERSISTENCE_ID, encryptedMessage.persistenceId)
                .findAll()
            rows.deleteAllFromRealm()
        }
        store.close()
    }

    override fun deleteAllEncryptedMessages() {
        val store = realmHelper.getThreadInstance()

        store.executeTransaction{
            val rows = store.where(SendMessagePersistence::class.java).findAll()
            rows.deleteAllFromRealm()
        }
        store.close()
    }



    // Messages

    override fun <T: AbstractMessage> addMessage(abstractMessage: T): T {
        // TODO: May lead to errors as ids are not unique?
        if (abstractMessage.persistenceId == UniqueRealmObject.ID_META_DO_NOT_PERSIST) {
            // TODO: changing this might save it later by accident abstractMessage.persistenceId++
            return abstractMessage
        }

        val store = realmHelper.getThreadInstance()

        lateinit var result: T
        store.executeTransaction {
            val messagePersistence = DeviceDataMapper(realmHelper).toPersistence(abstractMessage)

            resolveAllRealmReferences(messagePersistence, store)

            messagePersistence.targetDialogUser?.lastMessageToDisplay = messagePersistence
            messagePersistence.targetDialogGroup?.lastMessageToDisplay = messagePersistence

            val persisted = store.copyToRealmOrUpdate(messagePersistence)

            result = DeviceDataMapper(realmHelper).toDomain(persisted) as T
        }
        store.close()
        return result
    }

    override fun getMessageChunkForUser(baseDialog: BaseDialog, page: Int): List<AbstractMessage> {
        val store = realmHelper.getThreadInstance()

        val query: RealmQuery<MessagePersistence> = store.where(MessagePersistence::class.java)
            .getMessageUserFilter(baseDialog)
            .equalTo(FIELD_PARTIAL_MESSAGE, false)
            .sort(FIELD_CREATED_AT_TIMESTAMP, Sort.DESCENDING)
        val result: List<MessagePersistence>
        if (page == -1) {
            result = query.findAll()
        } else {
            // pagination
            val size = 50
            val from = page * size

            val section = from + size
            val messages = query
                .limit(section.toLong())
                .findAll()

            val to = min(section.toDouble(), messages.size.toDouble()).toInt()
            // efficient as realm uses proxies, doesnt fetch all.
            result = messages.subList(from, to)
        }

        val domainResult = result.map { DeviceDataMapper(realmHelper).toDomain(it) }
        store.close()
        return domainResult
    }

    override fun getIncompleteMessagesForUser(baseDialog: BaseDialog): List<UserMessage> {
        val store = realmHelper.getThreadInstance()
        val result = store.where(MessagePersistence::class.java)
            .getMessageUserFilter(baseDialog)
            .equalTo(FIELD_PARTIAL_MESSAGE, true)
            .sort(FIELD_CREATED_AT_TIMESTAMP, Sort.DESCENDING)
            .findAll()

        val domainResult = result.map { DeviceDataMapper(realmHelper).toDomain(it) } as List<UserMessage>
        store.close()
        return domainResult
    }

    override fun getCountUnreadMessagesForUser(baseDialog: BaseDialog): Int {
        val store = realmHelper.getThreadInstance()
        val domainResult = store.where(MessagePersistence::class.java)
            .getMessageUserFilter(baseDialog)
            .equalTo(FIELD_MESSAGE_READ, false)
            .sort(FIELD_CREATED_AT_TIMESTAMP, Sort.DESCENDING)
            .count().toInt()

        store.close()
        return domainResult
    }

    override fun removeMessages(messages: List<AbstractMessage>) {
        val store = realmHelper.getThreadInstance()

        store.executeTransaction {
            // Query all objects with primary key in keys
            val messagesToDelete = it.where(MessagePersistence::class.java)
                .`in`(FIELD_PERSISTENCE_ID, messages.map { it.persistenceId }.toTypedArray())
                .findAll()

            messagesToDelete.deleteAllFromRealm()
        }
        store.close()
    }

    override fun getAllMessagesForUser(baseDialog: BaseDialog): List<AbstractMessage> {
        return getMessageChunkForUser(baseDialog, -1)
    }

    override fun getAllLatestNecessaryForUser(baseDialog: BaseDialog): List<AbstractMessage> {
        // TODO: implement. Loading all is inefficient on long term,
        //  so pass a "() -> continue" that is called chunkwise and may finish on the first 3 loaded messages
        return getMessageChunkForUser(baseDialog, -1)
    }

    override fun getFirstMessagesByUserId(): Map<UniqueDialogId, AbstractMessage> {
        val store = realmHelper.getThreadInstance()
        val userIds = store.where(MessagePersistence::class.java)
            .distinct(FIELD_TARGET_DIALOG_USER_PERSISTENCE_ID)
            .findAll()
            // TODO: Do we need to properly match to over basedialogs?
            .map { UniqueDialogId( it.targetDialogUser!!.persistenceId, User::class.java.simpleName  ) }

        val mapper = DeviceDataMapper(realmHelper)

        val result = mutableMapOf<UniqueDialogId, AbstractMessage>()
        userIds.forEach { userId ->
            val latestMessage = store.where(MessagePersistence::class.java)
                .equalTo(FIELD_TARGET_DIALOG_USER_PERSISTENCE_ID, userId.persistenceId)
                .sort(FIELD_CREATED_AT_TIMESTAMP, Sort.DESCENDING)
                .findFirst()

            if (latestMessage != null) {
                result[userId] = mapper.toDomain(latestMessage)
            }
        }

        val groupIds = store.where(MessagePersistence::class.java)
            .distinct(FIELD_TARGET_DIALOG_GROUP_PERSISTENCE_ID)
            .findAll()
            // TODO: Do we need to properly match to over basedialogs?
            .map { UniqueDialogId( it.targetDialogGroup!!.persistenceId, OnlineGroup::class.java.simpleName  ) }

        groupIds.forEach { userId ->
            val latestMessage = store.where(MessagePersistence::class.java)
                .equalTo(FIELD_TARGET_DIALOG_GROUP_PERSISTENCE_ID, userId.persistenceId)
                .sort(FIELD_CREATED_AT_TIMESTAMP, Sort.DESCENDING)
                .findFirst()

            if (latestMessage != null) {
                result[userId] = mapper.toDomain(latestMessage)
            }
        }

        store.close()
        return result
    }

    private fun RealmQuery<MessagePersistence>.getMessageUserFilter(baseDialog: BaseDialog): RealmQuery<MessagePersistence> {
        var propertyAccess = RealmHelper.toFieldSelector(MessagePersistence::targetDialogUser, UserPersistence::persistenceId)
        if (baseDialog is OnlineGroup) {
            propertyAccess = RealmHelper.toFieldSelector(MessagePersistence::targetDialogGroup, OnlineGroupPersistence::persistenceId)
        }
        return equalTo(propertyAccess, baseDialog.persistenceId)
    }

}