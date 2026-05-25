package de.hartz.software.parannoying.online.model.domain

import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ExchangeDataWrapper
import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject

class LoggedEncryptedMessage(): ExchangeDataWrapper {

    var persistenceId: Long = UniqueRealmObject.ID_META_NEWEST_ID

    lateinit var message: String
    var createdAt: Long = 0 // When this message got moved to the log.
    var receivedAt: Long = -1L
    var sendAt: Long = 0  // Leaving Outbox timestamp from server or device.
    var targetUserHash: String? = null // Null for InboxMessages
    var isFileMessage: Boolean = false
    override val exchangeData: String get() = message
    override val isFile: Boolean get() = isFileMessage
    override val filePath: String
        get() = run {
            if (isFileMessage) {
                message
            }
            throw RuntimeException()
        }

    constructor(
        message: String,
        sendAt: Long,
        receivedAt: Long = -1,
        fileMessage: Boolean = false,
        targetUserHash: String): this() {
        this.message = message
        this.sendAt = sendAt
        // TODO: this was never set, thats a bug isnt it?
        // this.receivedAt = receivedAt
        this.createdAt = IOHelper.getCurrentDateAsUnixTimestamp() * 1000
        isFileMessage = fileMessage
        this.targetUserHash = targetUserHash
    }

    fun isReceivedMessage (): Boolean {
        return receivedAt != -1L
    }
}
