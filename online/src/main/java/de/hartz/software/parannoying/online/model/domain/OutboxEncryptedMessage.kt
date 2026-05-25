package de.hartz.software.parannoying.online.model.domain

import de.hartz.software.parannoying.core.interfaces.di.air.gap.ExchangeDataWrapper
import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject

class OutboxEncryptedMessage(): ExchangeDataWrapper {

    var persistenceId: Long = UniqueRealmObject.ID_META_NEWEST_ID

    lateinit var message: String
    var createdAt: Long = 0
    lateinit var targetUserHash: String
   override var isFile: Boolean = false
    override val exchangeData: String get() = message
    override val filePath: String
        get() = run {
            if (isFile) {
                message
            }
            throw RuntimeException()
        }
    
    constructor(message: String, createdAt: Long, targetUserHash: String): this() {
        this.message = message
        this.createdAt = createdAt
        this.targetUserHash = targetUserHash
    }

}
