package de.hartz.software.parannoying.offline.model.persistence.dialogs

import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject
import de.hartz.software.parannoying.offline.model.persistence.DecryptionKeyCloakForUserPersistence
import de.hartz.software.parannoying.offline.model.persistence.EncryptionKeyCloakForUserPersistence
import de.hartz.software.parannoying.offline.model.persistence.OfflineKeyPairPersistence
import de.hartz.software.parannoying.offline.model.persistence.UserSecurityPersistence
import de.hartz.software.parannoying.offline.model.persistence.message.MessagePersistence
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class UserPersistence: UniqueRealmObject, RealmObject()  {

    @PrimaryKey
    override var persistenceId: Long = UniqueRealmObject.ID_META_NEWEST_ID

    lateinit var hash: String // OnlineId or fake onlineId starting with prefix
    lateinit var nickname: String

    lateinit var originalName: String
    var createdAtTimestamp: Long = -1

    lateinit var userSecurityIssues: RealmList<UserSecurityPersistence>

    // Keys.
    var decryptionKeyCloakForUser: DecryptionKeyCloakForUserPersistence? = null
    var encryptionKeyCloakForUser: EncryptionKeyCloakForUserPersistence? = null
    // IVs
    lateinit var newestReceivedToken: String
    lateinit var previousReceivedToken: String
    lateinit var unconfirmedGeneratedTokens: RealmList<String>

    var numberOfNewMessages : Int = 0
    var lastMessageToDisplay: MessagePersistence? = null

    // Sign key only for current user
    var signKey: OfflineKeyPairPersistence? = null

    // Only for offline groups.
    var groupId: String? = null

}