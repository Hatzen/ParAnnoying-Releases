package de.hartz.software.parannoying.offline.helper.security.serializer

import android.util.Log
import de.hartz.software.parannoying.core.helper.security.DataSecurityHelper
import de.hartz.software.parannoying.offline.model.exceptions.UserIsUsingUnknownIdException
import de.hartz.software.parannoying.offline.model.OfflineStorage
import de.hartz.software.parannoying.offline.model.domain.dialogs.SimpleDialog
import de.hartz.software.parannoying.offline.model.domain.dialogs.UnknownUser

class SourceUserDeterminer {

    companion object {
        fun determine(user: SimpleDialog?, sourceUserHash: String): SimpleDialog {
            if (user == null) { // user == null => virgin user decryption
                val sender = OfflineStorage.INSTANCE.users.find{ it.hash == sourceUserHash }!!
                return sender
            } else if (user.hash != sourceUserHash) {
                if (user.hash.startsWith(DataSecurityHelper.NOTIFICATION_ID_PREFIX_GROUP_OFFLINE)) {
                    val unknownUser = UnknownUser(sourceUserHash)
                    unknownUser.initDummy()
                    return unknownUser
                } else {
                    Log.e(this.javaClass::class.simpleName, "Could not verify sourceUser! \n" + user.hash + "\n" +  sourceUserHash)
                    throw UserIsUsingUnknownIdException()
                }
            } else { // user.hash == sourceUserHash
                // needed for offline group known users. // TODO: BUT KEEP IN MIND: EVERY MEMBER OF THE OFFLINE_GROUP CAN FAKE MESSAGE BY REPLACING SOURCEHASH!!!
                return user
            }
        }
    }
}