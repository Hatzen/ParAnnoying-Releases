package de.hartz.software.parannoying.offline.model.persistence

import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject
import de.hartz.software.parannoying.offline.model.persistence.message.MessagePersistence
import de.hartz.software.parannoying.offline.model.persistence.dialogs.UserPersistence
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class EventPersistence: UniqueRealmObject, RealmObject() {
    @PrimaryKey
    override var persistenceId = UniqueRealmObject.ID_META_NEWEST_ID

    var eventType: String = ""
    var eventText: String = ""
    var createdAtTimestamp: Long = -1

    var corruptedString: String = ""

    var numberOfSyncedMessages: Long = -1

    var relatedUser: UserPersistence? = null

    var relatedMessage: MessagePersistence? = null
}