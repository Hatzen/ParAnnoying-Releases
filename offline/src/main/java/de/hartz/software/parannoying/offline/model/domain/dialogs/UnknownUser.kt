package de.hartz.software.parannoying.offline.model.domain.dialogs

import com.stfalcon.chatkit.commons.models.IUser
import de.hartz.software.parannoying.offline.model.OfflineStorage
import de.hartz.software.parannoying.offline.model.domain.UserSecurity

/**
 * User to display when unkown offline group member answers.
 */
open class UnknownUser(hash: String): User(hash, hash), IUser {

    fun initDummy() {
        // Just dummy encryptionkey to not fail mapping..
        encryptionKeyCloakForUser = OfflineStorage.INSTANCE.currentUser.encryptionKeyCloakForUser
        userSecurityIssues.add(UserSecurity.UNKNOWN_USER)
        unconfirmedGeneratedSendTokensForDecryption.add("dummy")
        unconfirmedGeneratedSendTokensForDecryption.add("dummy")
    }

    override val className: String
        get() = javaClass.simpleName

}