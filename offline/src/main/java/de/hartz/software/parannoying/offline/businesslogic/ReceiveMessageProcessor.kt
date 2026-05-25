package de.hartz.software.parannoying.offline.businesslogic

import android.content.Context
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.helper.security.DataSecurityHelper
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.offline.helper.security.serializer.DecryptionHandler
import de.hartz.software.parannoying.offline.model.OfflineStorage
import de.hartz.software.parannoying.offline.model.domain.UserSecurity
import de.hartz.software.parannoying.offline.model.domain.dialogs.BaseDialog
import de.hartz.software.parannoying.offline.model.domain.dialogs.CurrentUser
import de.hartz.software.parannoying.offline.model.domain.dialogs.SimpleDialog
import de.hartz.software.parannoying.offline.model.domain.events.BaseEvent
import de.hartz.software.parannoying.offline.model.domain.events.CorruptedDataEvent
import de.hartz.software.parannoying.offline.model.domain.events.MessageEvent
import de.hartz.software.parannoying.offline.model.domain.messages.AbstractMessage
import de.hartz.software.parannoying.offline.model.domain.messages.UserMessage
import de.hartz.software.parannoying.offline.model.exceptions.UserNotFoundException

class ReceiveMessageProcessor(val applicationContext: Context, val securityInterfaceHolder: SecurityInterfaceHolder) {

    private val storage: OfflineStorage
    private val currentUser: CurrentUser

    init {
        storage = applicationContext.app.Storage as OfflineStorage
        currentUser = storage.currentUser
    }


    fun addReceivedMessage(rawMessage: String) {
        val senderUserAndUserMessage: Pair<UserMessage, SimpleDialog>
        try {
            senderUserAndUserMessage = DecryptionHandler(applicationContext, securityInterfaceHolder, storage)
                .getUserMessageAndUser(rawMessage)
        } catch (exception: UserNotFoundException) {
            storage.persistEvent(CorruptedDataEvent(BaseEvent.EVENT_FAILED_RECEIVED_MESSAGE, rawMessage))
            throw exception
        }
        // Needed for online groups.
        val targetOnlineId = DataSecurityHelper(securityInterfaceHolder).getOnlineIdFromMessage(rawMessage)
        handleMessage(senderUserAndUserMessage, targetOnlineId)
    }

    fun addReceivedFileMessage(filePath: String) {
        val senderUserAndUserMessage: Pair<AbstractMessage, SimpleDialog>
        try {
            senderUserAndUserMessage = DecryptionHandler(applicationContext, securityInterfaceHolder, storage)
                .getFileMessageAndUser(filePath)
        }catch (exception: UserNotFoundException) {
            storage.persistEvent(CorruptedDataEvent(BaseEvent.EVENT_FAILED_RECEIVED_MESSAGE, filePath))
            throw exception
        }
        // Needed for online groups.
        val targetOnlineId = senderUserAndUserMessage.first.fileMetaData.targetHash
        handleMessage(senderUserAndUserMessage, targetOnlineId)
    }

    private fun handleMessage(senderUserAndUserMessage: Pair<AbstractMessage, SimpleDialog>, targetOnlineId: String) {
        if (targetOnlineId.startsWith(DataSecurityHelper.NOTIFICATION_ID_PREFIX_GROUP_OFFLINE)) {
            val group = senderUserAndUserMessage.second
            val message = senderUserAndUserMessage.first
            addMessage(group, message)
        } else {
            handleMessageForUserOrOnlineGroup(senderUserAndUserMessage, targetOnlineId)
        }
        // Store for both as we should only persist once, otherwise changes afterwards might overwrite?
        storage.updateDialog(senderUserAndUserMessage.second)
    }

    private fun handleMessageForUserOrOnlineGroup(
        senderUserAndUserMessage: Pair<AbstractMessage, SimpleDialog>,
        targetOnlineId: String
    )  {
        val message = senderUserAndUserMessage.first
        val user = senderUserAndUserMessage.second

        val receivedCombinedToken = if (message.metaData.tokenCheckSum == "")  null else message.metaData.tokenCheckSum
        // Create hash before adding new message.

        val messages = storage.getAllLatestNecessaryForUser(currentUser)
        TokenDeterminer(securityInterfaceHolder).generateCombinedTokenForUserAndConfirmTokens(
            currentUser,
            messages,
            receivedCombinedToken)

        val userSecurity = UserSecurity.getByString(message.metaData.securityRisks)
        user.userSecurityIssues.addAll(userSecurity)

        val dataSecurityHelper = DataSecurityHelper(securityInterfaceHolder)
        if (dataSecurityHelper.isOnlineIdForOnlineGroup(targetOnlineId)) {
            // Remove target user marker, as set by message creation.
            val cleanOnlineGroupId = targetOnlineId
                .replace(
                    DataSecurityHelper.ONLINE_GROUP_ONLINE_ID_TARGET_PREFIX_MARKER,
                    DataSecurityHelper.ONLINE_GROUP_ONLINE_ID_SEPARATOR)

            // TODO: Display missing/ deleted users hashes within UserInfoActivity
            val onlineGroup = storage.onlineGroups.find { it.hash == cleanOnlineGroupId }!!
            addMessage(onlineGroup, message)

            // It would be also useful to update this for every online group whenever a new message arrives.
            // Especially it is important to notice that the current user himself does not have any impact.
            onlineGroup.updateSecurityIssues()
            storage.updateDialog(onlineGroup)
        } else {
            // Hack for self written messages add message as user is not the same (TODO: Check if this is still true?)
            /*if (user == storage.currentUser) {
                currentUser.messages.add(message)
            }*/
            addMessage(user, message)
        }

        // TODO: Received tokens not handled within SimpleDialog#setTokensForMessageMetaData
        if (message.metaData.previousToken != user.newestReceivedToken) {
            // TODO: Dont mark this message. Mark every message before this til the last confirmed one.
            //  We could confirm every message before this with xorED string of all received tokens til this new one.
            //  update (2025.04) probably ok to mark this message, as others get marked when confirming?
            message.messageTokenSkipped = true
            UiHelper.showToastFromBackgroundTask(applicationContext, "Scanned in the wrong order or missing previous message.")
        }

        // For online groups we will use the same tokens. // TODO: But currently we do it for users and online groups? Seems wrong..
        user.previousReceivedToken = user.newestReceivedToken
        user.newestReceivedToken = message.metaData.newToken
    }

    private fun addMessage(dialog: BaseDialog, message: AbstractMessage) {
        var messageToPersist = message
        if (messageToPersist is UserMessage && messageToPersist.metaData.isPartOfLargeMessage()) {
            messageToPersist.partialMessage = true
            val incompleteMessagesOfUser = storage.getIncompleteMessagesForUser(dialog)

            val uuid = message.metaData.messageUuid
            val incompleteMessages = dialog.getIncompleteMessageMap(incompleteMessagesOfUser)
            var listOfMessagesWithReceivedUUID = incompleteMessages[uuid]
            if (listOfMessagesWithReceivedUUID == null) {
                listOfMessagesWithReceivedUUID = mutableListOf()
            }

            val allNecessaryMessagesReceived = listOfMessagesWithReceivedUUID.size == messageToPersist.metaData.maxSequenceNumber
            if (allNecessaryMessagesReceived) {
                // TODO: Check for duplicates (there is a message for EVERY sequence number inbetween)
                val completeMessage = listOfMessagesWithReceivedUUID.sortedBy { it.metaData.sequenceNumber }
                    .map {
                        it.message
                    }.joinToString("")
                val decryptedMessage = securityInterfaceHolder.hardcodedEncryptionHelper.decrypt(completeMessage)
                val finalMessage = listOfMessagesWithReceivedUUID[0]
                finalMessage.message = decryptedMessage
                finalMessage.partialMessage = false
                finalMessage.relatedDialog = dialog

                messageToPersist = finalMessage
                dialog.numberOfNewMessages++ // Or calculate all new? updateUnreadMessages()
                storage.removeMessages(listOfMessagesWithReceivedUUID) // Removes partialmessage as well! so be sure to add it again.
            }
        } else {
            messageToPersist.relatedDialog = dialog
            dialog.numberOfNewMessages++ // Or calculate all new? updateUnreadMessages()
        }
        storage.addMessage(messageToPersist)
        // TODO: messageToPersist or message as reference?
        storage.persistEvent(MessageEvent(BaseEvent.EVENT_RECEIVED_MESSAGE, message))
    }

}