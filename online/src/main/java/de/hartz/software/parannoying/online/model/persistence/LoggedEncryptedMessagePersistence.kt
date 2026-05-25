package de.hartz.software.parannoying.online.model.persistence

import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class LoggedEncryptedMessagePersistence: UniqueRealmObject, RealmObject() {
    @PrimaryKey
    override var persistenceId: Long = UniqueRealmObject.ID_META_NEWEST_ID

    var message: String = ""
    var createdAt: Long = 0
    var sendAt: Long = -1
    var receivedAt: Long = -1L
    var targetUserHash: String = ""
    var isFileMessage: Boolean = false

}
