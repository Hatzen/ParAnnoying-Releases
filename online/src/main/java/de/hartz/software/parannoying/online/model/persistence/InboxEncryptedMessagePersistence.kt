package de.hartz.software.parannoying.online.model.persistence

import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class InboxEncryptedMessagePersistence: UniqueRealmObject, RealmObject() {
    @PrimaryKey
    override var persistenceId: Long = UniqueRealmObject.ID_META_NEWEST_ID
    lateinit var message: String
    var receivedAt: Long = 0
    var sendAt: Long = 0
    var isFileMessage: Boolean = false

}
