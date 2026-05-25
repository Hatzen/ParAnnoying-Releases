package de.hartz.software.parannoying.offline.model.persistence.repository

import de.hartz.software.parannoying.core.model.persistence.realm.RealmHelper
import de.hartz.software.parannoying.offline.interfaces.repository.EventRepository
import de.hartz.software.parannoying.offline.model.domain.events.BaseEvent
import de.hartz.software.parannoying.offline.model.domain.events.SimpleEvent
import de.hartz.software.parannoying.offline.model.mapper.DeviceDataMapper
import de.hartz.software.parannoying.offline.model.persistence.EventPersistence


class EventRepositoryImplRealm(val realmHelper: RealmHelper): EventRepository {

    override fun readEvents(): List<BaseEvent> {
        val store = realmHelper.getThreadInstance()
        val storeData = store.where(EventPersistence::class.java)
            .findAll()
        val result = store.copyFromRealm(storeData).map { DeviceDataMapper(realmHelper).toDomain(it) }.toList()
        store.close()
        return result
    }

    private val cache = HashMap<String, Long>()
    private val MINUTE = 1000 * 60 * 3
    override fun persistEvent(inboxEncryptedMessage: BaseEvent) {
        if (inboxEncryptedMessage is SimpleEvent) {
            val timeSinceLast = cache[inboxEncryptedMessage.eventType] ?: 0
            val current = System.currentTimeMillis()
            if (current - timeSinceLast < MINUTE) {
                return
            }
            cache[inboxEncryptedMessage.eventType] = current
        }

        val storeData = DeviceDataMapper(realmHelper).toPersistence(inboxEncryptedMessage)
        val store = realmHelper.getThreadInstance()
        store.executeTransaction{
            it.copyToRealmOrUpdate(storeData)
        }
        store.close()
    }

    override fun deleteEvent(inboxEncryptedMessage: BaseEvent) {
        // TODO: Do we need the mapping for generating the id or something?
        val storeData = DeviceDataMapper(realmHelper).toPersistence(inboxEncryptedMessage)
        val store = realmHelper.getThreadInstance()
        store.executeTransaction{
            val storeData = store.where(EventPersistence::class.java)
                .equalTo("persistenceId", storeData.persistenceId)
                .findAll()
            storeData.deleteAllFromRealm()
        }
        store.close()
    }

    override fun deleteAllEvents() {
        val store = realmHelper.getThreadInstance()
        store.executeTransaction{
            store.delete(EventPersistence::class.java)
        }
        store.close()
    }

}