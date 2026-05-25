package de.hartz.software.parannoying.offline.model.persistence.message

import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject
import de.hartz.software.parannoying.offline.model.persistence.dialogs.OnlineGroupPersistence
import de.hartz.software.parannoying.offline.model.persistence.dialogs.UserPersistence
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class MessagePersistence : UniqueRealmObject, RealmObject() {
    @PrimaryKey
    override var persistenceId = UniqueRealmObject.ID_META_NEWEST_ID

    // usermessage
    var metaData: MetaDataPersistence? = null
    var sender: UserPersistence? = null

    // @Index
    var targetDialogUser: UserPersistence? = null
    // @Index
    var targetDialogGroup: OnlineGroupPersistence? = null

    var message: String? = null
    var createdAtTimestamp: Long = -1
    var messageRead: Boolean = false
    var messageConfirmed: Boolean = false
    var messageTokenSkipped: Boolean = false

    var partialMessage = false

    // filemessage
    var fileMetaData: FileMetaDataPersistence? = null
    var filePath: String? = null
}
