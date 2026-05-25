package de.hartz.software.parannoying.offline.model.domain

import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject
import java.security.PrivateKey
import java.security.PublicKey

class OfflineKeyPair() {

    constructor(publicKey: PublicKey, privateKey: PrivateKey) : this() {
        this.publicKey = publicKey
        this.privateKey = privateKey
    }

    var persistenceId: Long = UniqueRealmObject.ID_META_NEWEST_ID

    lateinit var privateKey: PrivateKey

    lateinit var publicKey: PublicKey

}