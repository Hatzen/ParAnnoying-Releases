package de.hartz.software.parannoying.offline.model.domain.messages

import androidx.lifecycle.MutableLiveData
import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.commons.models.IUser
import com.stfalcon.chatkit.commons.models.MessageContentType
import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject
import de.hartz.software.parannoying.offline.model.domain.dialogs.BaseDialog
import de.hartz.software.parannoying.offline.model.domain.dialogs.OfflineGroup
import de.hartz.software.parannoying.offline.model.domain.dialogs.SimpleDialog
import de.hartz.software.parannoying.offline.model.domain.dialogs.UnknownUser
import de.hartz.software.parannoying.offline.model.domain.dialogs.User
import java.util.Date

abstract class AbstractMessage : IMessage, MessageContentType {

    var persistenceId: Long = UniqueRealmObject.ID_META_NEWEST_ID

    // Creator of this message
    var sender: SimpleDialog? = null

    // The dialog in which this message appears.
    var relatedDialog: BaseDialog? = null

    var createdAtTimestamp: Long = -1
    // Message indicator when user received a message on offline device so it is not read yet.
    var messageRead: Boolean = false
    // Message indicator when user sends a message and gets a response to it, so the send message got confirmed.
    var messageConfirmed: Boolean = false
    // Message indicator when user sends a message and gets a response to it, so the send message got confirmed.
    var messageTokenSkipped: Boolean = false
    // is part of messages and needs further parts to get full message
    var partialMessage = false

    var messageLoading = MutableLiveData(false)

    open lateinit var metaData: MetaData
    override fun getId(): String {
        return persistenceId.toString()
    }

    override fun getCreatedAt(): Date {
        return Date(createdAtTimestamp * 1000)
    }

    override fun getUser(): IUser {
        if (sender is User) {
            return sender as User
        } else if (sender is OfflineGroup) {
            return sender as OfflineGroup
        } else if (sender is UnknownUser) {
            return sender as UnknownUser
        }
        throw NotImplementedError()
    }
}
