package de.hartz.software.parannoying.offline.businesslogic

import android.content.Context
import de.hartz.software.parannoying.air.gap.model.UseCases
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.interfaces.di.ApplicationInfoComponent
import de.hartz.software.parannoying.core.interfaces.di.air.gap.AirGapAdapter
import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject
import de.hartz.software.parannoying.offline.helper.security.serializer.EncryptionHandler
import de.hartz.software.parannoying.offline.model.OfflineStorage
import de.hartz.software.parannoying.offline.model.domain.SendMessage
import de.hartz.software.parannoying.offline.model.domain.dialogs.BaseDialog
import de.hartz.software.parannoying.offline.model.domain.dialogs.CurrentUser
import de.hartz.software.parannoying.offline.model.domain.events.BaseEvent
import de.hartz.software.parannoying.offline.model.domain.events.MessageEvent
import de.hartz.software.parannoying.offline.model.domain.messages.FileMessage
import de.hartz.software.parannoying.offline.model.domain.messages.MetaData
import de.hartz.software.parannoying.offline.model.domain.messages.UserMessage
import de.hartz.software.parannoying.offline.model.domain.settings.MessageSecurity
import java.io.File

class CreateMessageProcessor(val securityInterfaceHolder: SecurityInterfaceHolder, val applicationContext: Context, val applicationInfoComponent: ApplicationInfoComponent) {

    private val storage: OfflineStorage
    private val currentUser: CurrentUser
    private val airGapAdapter: AirGapAdapter

    init {
        storage = applicationContext.app.Storage as OfflineStorage
        currentUser = storage.currentUser
        airGapAdapter = applicationContext.app.airGapAdapter
    }

    fun queue(text: String, user: BaseDialog, callback: (UserMessage) -> Unit) {
        val storage = applicationContext.app.Storage as OfflineStorage

        val userMessage = applySentMessage(text, user, callback)

        // TODO: Determining tokens here seem more proper. On the other hand this metadata dont get persisted anytime..
        //    may we create metadata twice without side effects on tokens?
        val encryptedMessages = EncryptionHandler(securityInterfaceHolder, storage, applicationContext, applicationInfoComponent)
            .getEncryptedMessages(text, user)
        if (encryptedMessages.size > 1 || storage.readSettings().syncAllMessages) {
            encryptedMessages.forEach {
                val sendMessage = SendMessage()
                sendMessage.encryptedMessage = it
                sendMessage.message = userMessage
                storage.addEncryptedMessage(sendMessage)
            }
            userMessage.messageLoading.postValue(false)
        } else {
            val encryptedMessage = encryptedMessages[0]
            // TODO: Probably launching activity from background wont work..
            airGapAdapter.startSend(UseCases.Offline.MESSAGE_SEND.useText(encryptedMessage))
        }
    }


    fun queueFileMessage(filePath: String, user: BaseDialog, callback: (FileMessage) -> Unit) {
        val storage = applicationContext.app.Storage as OfflineStorage

        val encryptedMessages = EncryptionHandler(securityInterfaceHolder, storage, applicationContext, applicationInfoComponent)
                .createAndStoreFileMessages(File(filePath), user, callback)
    }

    fun applySentMessage (plainText: String, targetUser: BaseDialog, callback: (UserMessage) -> Unit): UserMessage {
        var messageSent = createMessageSent(plainText, targetUser)

        messageSent.messageLoading.postValue(true)

        messageSent = storage.addMessage(messageSent) as UserMessage
        storage.persistEvent(MessageEvent(BaseEvent.EVENT_SEND_MESSAGES, messageSent))

        callback(messageSent)
        return messageSent
    }

    private fun createMessageSent(plainText: String, targetUser: BaseDialog): UserMessage {
        val messageSent = UserMessage()
        if (storage.readSettings().messageSecurity == MessageSecurity.DELETE) {
            messageSent.persistenceId = UniqueRealmObject.ID_META_DO_NOT_PERSIST
        }
        messageSent.message = plainText
        messageSent.createdAtTimestamp = IOHelper.getCurrentDateAsUnixTimestamp()
        messageSent.sender = currentUser
        messageSent.relatedDialog = targetUser
        messageSent.messageRead = true
        // TODO: But the send message should have a token which gets confirmed so we get a proper icon.
        // Do NOT create or change tokens here done later when creating messages.
        messageSent.metaData = MetaData().initWithoutTokens(
            applicationContext,
            applicationInfoComponent,
            storage.readSettings()
        )
        return messageSent
    }

}