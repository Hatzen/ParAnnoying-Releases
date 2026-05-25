package de.hartz.software.parannoying.offline.model.persistence

import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class OfflineKeyPairPersistence: UniqueRealmObject, RealmObject() {
    @PrimaryKey
    override var persistenceId = UniqueRealmObject.ID_META_NEWEST_ID

    var privateKey: String? = null
    var publicKey: String? = null

}