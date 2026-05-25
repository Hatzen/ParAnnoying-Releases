package de.hartz.software.parannoying.offline.model.persistence.dialogs

import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject
import de.hartz.software.parannoying.offline.model.persistence.UserSecurityPersistence
import de.hartz.software.parannoying.offline.model.persistence.message.MessagePersistence
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class OnlineGroupPersistence: UniqueRealmObject, RealmObject()  {

    @PrimaryKey
    override var persistenceId: Long = UniqueRealmObject.ID_META_NEWEST_ID

    lateinit var hash: String // OnlineId or fake onlineId starting with prefix
    lateinit var nickname: String

    lateinit var originalName: String
    var createdAtTimestamp: Long = -1

    lateinit var userSecurityIssues: RealmList<UserSecurityPersistence>
    var lastMessageToDisplay: MessagePersistence? = null

    // Only for online groups.
    var groupId: String? = null
    lateinit var firebaseEmailIds: RealmList<String>

}