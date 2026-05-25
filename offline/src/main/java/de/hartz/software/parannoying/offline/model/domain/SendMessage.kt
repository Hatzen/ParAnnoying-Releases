package de.hartz.software.parannoying.offline.model.domain

import de.hartz.software.parannoying.core.interfaces.di.air.gap.ExchangeDataWrapper
import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject
import de.hartz.software.parannoying.offline.model.domain.messages.AbstractMessage
import de.hartz.software.parannoying.offline.model.domain.messages.FileMessage

class SendMessage : ExchangeDataWrapper {
    var persistenceId: Long = UniqueRealmObject.ID_META_NEWEST_ID

    lateinit var encryptedMessage: String
    lateinit var message: AbstractMessage

    override val exchangeData: String get() = encryptedMessage

    override val isFile get() = message is FileMessage
    var encryptedFilePath
        get() = encryptedMessage
        set(t) { encryptedMessage = t }

}
