package de.hartz.software.parannoying.online.model.domain

import de.hartz.software.parannoying.core.interfaces.di.air.gap.ExchangeDataWrapper
import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject

class InboxEncryptedMessage(): ExchangeDataWrapper {

    var persistenceId: Long = UniqueRealmObject.ID_META_NEWEST_ID

    lateinit var message: String
    var isFileMessage: Boolean = false
    override val exchangeData: String get() = message
    override val isFile: Boolean get() = isFileMessage
    override val filePath: String
        get() = run {
            if (isFileMessage) {
                return message
            }
            throw RuntimeException()
        }

    /**
     * Both in Milliseconds.
     */
    var receivedAt: Long = 0
    var sendAt: Long = 0

    constructor(message: String, receivedAt: Long, sendAt: Long, isFileMessage: Boolean = false): this() {
        this.message = message
        this.receivedAt = receivedAt
        this.sendAt = sendAt
        this.isFileMessage = isFileMessage
    }

}
