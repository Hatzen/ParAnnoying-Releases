package de.hartz.software.parannoying.offline.model.persistence

import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class EncryptionKeyCloakForUserPersistence: UniqueRealmObject, RealmObject() {
    @PrimaryKey
    override var persistenceId = UniqueRealmObject.ID_META_NEWEST_ID

    var encryptionKey: String? = null
    var symmetricKey: String? = null
    var signedKey: String? = null
}