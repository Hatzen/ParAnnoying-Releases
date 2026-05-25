package de.hartz.software.parannoying.offline.interfaces.repository

import de.hartz.software.parannoying.offline.model.domain.SendMessage
import de.hartz.software.parannoying.offline.model.domain.dialogs.BaseDialog
import de.hartz.software.parannoying.offline.model.domain.dialogs.UniqueDialogId
import de.hartz.software.parannoying.offline.model.domain.messages.AbstractMessage
import de.hartz.software.parannoying.offline.model.domain.messages.UserMessage


interface MessageRepository {

    // EncryptedMessages

    fun readSendMessage(): List<SendMessage>

    fun addEncryptedMessage(encryptedMessage: SendMessage)

    fun removeEncryptedMessage(encryptedMessage: SendMessage)

    fun deleteAllEncryptedMessages()


    // Messages

    fun <T: AbstractMessage> addMessage(abstractMessage: T): T

    fun getMessageChunkForUser(baseDialog: BaseDialog, page: Int): List<AbstractMessage>

    fun getIncompleteMessagesForUser(baseDialog: BaseDialog): List<UserMessage>

    fun getCountUnreadMessagesForUser(baseDialog: BaseDialog): Int

    fun removeMessages(messages: List<AbstractMessage>)

    fun getAllMessagesForUser(baseDialog: BaseDialog): List<AbstractMessage>

    fun getAllLatestNecessaryForUser(baseDialog: BaseDialog): List<AbstractMessage>

    @Deprecated("Persist last message..")
    fun getFirstMessagesByUserId(): Map<UniqueDialogId, AbstractMessage>
}