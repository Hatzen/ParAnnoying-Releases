package de.hartz.software.parannoying.offline.model.domain

import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject
import java.security.PrivateKey

class DecryptionKeyCloakForUser() {

    var persistenceId: Long = UniqueRealmObject.ID_META_NEWEST_ID

    // Keys to decrypt message for target user.
    lateinit var decryptionKey: PrivateKey
    lateinit var symmetricKey: String
    var initialToken: String? = null
}
