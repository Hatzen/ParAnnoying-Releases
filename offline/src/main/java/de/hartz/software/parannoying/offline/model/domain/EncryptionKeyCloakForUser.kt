package de.hartz.software.parannoying.offline.model.domain

import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject
import java.security.PrivateKey
import java.security.PublicKey

class EncryptionKeyCloakForUser() {

    var persistenceId: Long = UniqueRealmObject.ID_META_NEWEST_ID

    // Keys to encrypt message for target user.
    lateinit var encryptionKey: PublicKey
    lateinit var symmetricKey: String
    // Key to check the message is signed by the sender correctly. // TODO: This is not needed encryption but for decryption..
    lateinit var signedKey: PrivateKey

}
