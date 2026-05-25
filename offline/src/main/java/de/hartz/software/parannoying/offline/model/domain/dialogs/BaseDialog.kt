package de.hartz.software.parannoying.offline.model.domain.dialogs

import com.stfalcon.chatkit.commons.models.IDialog
import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject
import de.hartz.software.parannoying.offline.model.domain.UserSecurity
import de.hartz.software.parannoying.offline.model.domain.messages.AbstractMessage
import de.hartz.software.parannoying.offline.model.domain.messages.UserMessage


abstract class BaseDialog(
        var nickname: String,
        var hash: String, // OnlineId or fake onlineId starting with prefix
        var originalName:String = nickname
    ): IDialog<AbstractMessage>, UniqueDialogIdWrapper {

    fun getIncompleteMessageMap(message: List<UserMessage>): HashMap<String, MutableList<UserMessage>> {
        val map = HashMap<String, MutableList<UserMessage>>()
        message.forEach {
            var list = map[it.metaData.messageUuid]
            if (list == null) {
                list = mutableListOf()
                map[it.metaData.messageUuid] = list
            }
            list.add(it)
        }
        return map
    }

    var createdAtTimestamp: Long = -1

    override var persistenceId: Long = UniqueRealmObject.ID_META_NEWEST_ID

    var userSecurityIssues: HashSet<UserSecurity>

    var numberOfNewMessages : Int = 0

    var lastMessageToDisplay: AbstractMessage? = null

    init {
        userSecurityIssues = HashSet()
    }

    fun updateUnreadMessages(number: Int) {
        numberOfNewMessages = number
    }

    /**
     * Calculates a normalized security approximation.
     * returns 1.0 for most insecure device 0.0 for probably secure devices.
     */
    fun getSecurityLevel(): Double {
        val normalizedErrorLevel = userSecurityIssues
            .map {
                it.errorLevel
            }
            .average()
            .coerceAtLeast(0.0)
            .coerceAtMost(10.0)
            .div(10)
        return 1 - normalizedErrorLevel
    }

    override fun equals(other: Any?): Boolean {
        if (other is BaseDialog) {
            // Needed for mapper
            if (other.javaClass != javaClass) {
                return false
            }
            return other.hash == hash
        }
        return super.equals(other)
    }

    override fun getDialogPhoto(): String {
        return hash + '#' + getSecurityLevel()
    }

    override fun getUnreadCount(): Int {
        return numberOfNewMessages
    }

    override fun getId(): String {
        return persistenceId.toString()
    }

    override fun getDialogName(): String {
        return nickname
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    fun getCreatedAtInMilliseconds(): Long {
        return createdAtTimestamp * 1000
    }

    override fun getLastMessage(): AbstractMessage? {
        return lastMessageToDisplay
    }

    override fun setLastMessage(message: AbstractMessage?) {
        lastMessageToDisplay = message
    }
}