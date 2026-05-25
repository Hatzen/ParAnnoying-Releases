package de.hartz.software.parannoying.offline.model.persistence

import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject
import de.hartz.software.parannoying.offline.model.domain.messages.AbstractMessage
import de.hartz.software.parannoying.offline.model.persistence.message.MessagePersistence
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class SendMessagePersistence : UniqueRealmObject, RealmObject()  {
    @PrimaryKey
    override var persistenceId = UniqueRealmObject.ID_META_NEWEST_ID

    lateinit var encryptedMessage: String
    lateinit var message: MessagePersistence

}
