package de.hartz.software.parannoying.offline.model.persistence

import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class CrashLogPersistence: UniqueRealmObject, RealmObject() {
    @PrimaryKey
    override var persistenceId: Long = UniqueRealmObject.ID_META_NEWEST_ID
    var log: String = ""
    var createdAtTimestamp: Long = -1
}