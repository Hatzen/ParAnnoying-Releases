package de.hartz.software.parannoying.offline.model.domain.dialogs

import de.hartz.software.parannoying.offline.model.OfflineStorage
import java.io.Serializable


interface UniqueDialogIdWrapper {
    val persistenceId: Long
    val className: String

    fun getDialog(storage: OfflineStorage): BaseDialog {
        if (className == OnlineGroup::class.java.simpleName) {
            return storage.onlineGroups.find { it.persistenceId == persistenceId }!!
        } else if (className == User::class.java.simpleName || className == CurrentUser::class.java.simpleName) {
            return storage.users.find { it.persistenceId == persistenceId }!!
        } else if (className == OfflineGroup::class.java.simpleName) {
            return storage.offlineGroups.find { it.persistenceId == persistenceId }!!
        } else if (className == UnknownUser::class.java.simpleName) {
            return storage.users.find { it.persistenceId == persistenceId }!!
        }
        throw UnsupportedOperationException("Dialog not defined for className $className and id $persistenceId")
    }

    fun getUniqueDialogId (): UniqueDialogId {
        return UniqueDialogId(persistenceId, className)
    }
}

// To avoid errors as subclasses are not serializable when casted to serializable use wrapper..
data class UniqueDialogId (
        override val persistenceId: Long,
        override val className: String
) : Serializable, UniqueDialogIdWrapper