package de.hartz.software.parannoying.offline.helper.security.serializer

import android.content.Context
import de.hartz.software.parannoying.core.helper.io.FileHelper
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.helper.security.DataSecurityHelper
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.interfaces.di.ApplicationInfoComponent
import de.hartz.software.parannoying.offline.businesslogic.TokenDeterminer
import de.hartz.software.parannoying.offline.helper.security.serializer.file.FileSerializer
import de.hartz.software.parannoying.offline.helper.security.serializer.message.UserMessageSerializer
import de.hartz.software.parannoying.offline.model.OfflineStorage
import de.hartz.software.parannoying.offline.model.domain.SendMessage
import de.hartz.software.parannoying.offline.model.domain.dialogs.BaseDialog
import de.hartz.software.parannoying.offline.model.domain.dialogs.OnlineGroup
import de.hartz.software.parannoying.offline.model.domain.dialogs.SimpleDialog
import de.hartz.software.parannoying.offline.model.domain.messages.AbstractMessage
import de.hartz.software.parannoying.offline.model.domain.messages.FileMessage
import de.hartz.software.parannoying.offline.model.domain.messages.FileMetaData
import de.hartz.software.parannoying.offline.model.domain.messages.MetaData
import java.io.File

class EncryptionHandler(
    val securityInterfaceHolder: SecurityInterfaceHolder,
    val Storage: OfflineStorage,
    val context: Context,
    val applicationInfoComponent: ApplicationInfoComponent) {

    fun getEncryptedMessages(plainText: String, targetUser: BaseDialog): ArrayList<String> {
        if (targetUser is OnlineGroup) {
            targetUser.showWarningForUnknownMembersIfNeeded(context)
            val onlineGroupHash =  targetUser.hash
            return targetUser.users
                    .filter { it != Storage.currentUser }
                    .flatMap {
                        // ONLINE_GROUP_ONLINE_ID_SEPARATOR is only used in between ids.
                        val itUserPart = DataSecurityHelper.ONLINE_GROUP_ONLINE_ID_SEPARATOR + it.hash
                        val itUserPartWithTargetMarker = DataSecurityHelper.ONLINE_GROUP_ONLINE_ID_TARGET_PREFIX_MARKER + it.hash
                        val onlineGroupHashWithTargetUser = onlineGroupHash.replace(itUserPart, itUserPartWithTargetMarker)
                        getEncryptedMessageForSingleUser(plainText, it, onlineGroupHashWithTargetUser)
                    }
                    .toCollection(ArrayList())
        } else {
            val resultList = ArrayList<String>()
            resultList.addAll(
                getEncryptedMessageForSingleUser(plainText,
                    targetUser as SimpleDialog
                )
            )
            return resultList
        }
    }

    fun createAndStoreFileMessages(
        file: File,
        targetUser: BaseDialog,
        callback: (FileMessage) -> Unit
    ) : List<SendMessage> {
        // Create current user copy.
        val currentDate = IOHelper.getCurrentDateAsUnixTimestamp()
        val prefix = FileMetaData(
            targetUser.hash,
            Storage.currentUser.hash,
            "invalid hmac",
            MetaData(), // Will be replaced.
            currentDate,
            file.name
        )

        var result = FileMessage()
        result.fileMetaData = prefix
        result.createdAtTimestamp = currentDate
        result.sender = Storage.currentUser
        result.relatedDialog = targetUser
        result.filePath = file.absolutePath

        result = Storage.addMessage(result)
        result.messageLoading.postValue(true)
        callback(result)

        val resultList = mutableListOf<SendMessage>()
        // Create data to send.
        if (targetUser is OnlineGroup) {
            targetUser.getUsers().forEach {
                resultList.add(createFileMessage(
                    it, result, file
                ))
            }
        } else if (targetUser is SimpleDialog) {
            resultList.add(createFileMessage(
                targetUser, result, file
            ))
        } else {
            throw RuntimeException("Unsupported dialog type.")
        }
        result.messageLoading.postValue(false)
        return resultList
    }

    private fun createFileMessage(targetUser: SimpleDialog, result: FileMessage, file: File): SendMessage {
        val tokenDeterminer = TokenDeterminer(securityInterfaceHolder)

        val messages = Storage.getAllMessagesForUser(targetUser)
        val tokens = tokenDeterminer.getTokensForMessageMetaData(targetUser, messages)
        val metaData = MetaData().init(
            context,
            applicationInfoComponent,
            Storage.readSettings(),
            tokens
        )
        result.fileMetaData.metaData = metaData

        val message = encryptFileMessageAndStoreSendMessage(file, targetUser, result)
        Storage.addEncryptedMessage(message)
        Storage.updateDialog(targetUser)
        return message
    }

    private fun getEncryptedMessageForSingleUser(plainText: String, targetUser: SimpleDialog, targetHash: String = targetUser.hash): List<String> {

        // TODO: Dont we need to perist these uniqueIds and metadata completley?
        //   where to store when sending onlinegroup? Messages are stored on online groups but tokens required for user?
        val messages = Storage.getAllLatestNecessaryForUser(targetUser)
        val tokens = TokenDeterminer(securityInterfaceHolder).getTokensForMessageMetaData(targetUser as SimpleDialog, messages)
        val metaData = MetaData().init(context, applicationInfoComponent, Storage.readSettings(), tokens)

        var text = plainText
        if (plainText.length > DataSecurityHelper.MAX_MESSAGE_SIZE) {
            metaData.messageUuid = securityInterfaceHolder.randomHelper.getRandomUUIDv4()
            metaData.sequenceNumber = 0
            text = securityInterfaceHolder.hardcodedEncryptionHelper.encrypt(plainText)
        }

        val chunks = FileHelper.getChunks(text)
        metaData.maxSequenceNumber = chunks.size

        val encryptedChunks = chunks.mapIndexed { index, chunk ->
            val message = encryptMessage(chunk, targetUser, metaData, targetHash)
            metaData.sequenceNumber = index + 1
            message
        }
        return encryptedChunks
    }

    private fun encryptMessage(data: String, targetUser: SimpleDialog, metaData: MetaData, targetHash: String = targetUser.hash) : String {
        return UserMessageSerializer.serializeAndEncrypt(securityInterfaceHolder, data, targetUser, metaData, targetHash)
    }

    /**
     * File handler code
     */
    private fun encryptFileMessageAndStoreSendMessage(file: File, user: SimpleDialog, message: AbstractMessage): SendMessage {
        val encryptedfile = FileSerializer(securityInterfaceHolder).encryptFile(file.absolutePath, user, message.metaData)

        val sendMessage = SendMessage()
        sendMessage.encryptedFilePath = encryptedfile.absolutePath
        sendMessage.message = message

        Storage.addEncryptedMessage(sendMessage)
        return sendMessage
    }


}