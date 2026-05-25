package de.hartz.software.parannoying.offline.adapters.view.chat

import android.content.Context
import android.view.View
import com.mikepenz.iconics.Iconics
import com.mikepenz.iconics.view.IconicsTextView
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.messages.MessageHolders
import com.stfalcon.chatkit.messages.MessagesListAdapter
import de.hartz.software.parannoying.offline.R
import de.hartz.software.parannoying.offline.model.domain.messages.AbstractMessage
import de.hartz.software.parannoying.offline.model.domain.messages.MessageContentChecker
import de.hartz.software.parannoying.offline.model.domain.messages.MessageContentType
import de.hartz.software.parannoying.offline.model.domain.messages.UserMessage


class ChatAdapter(
        senderId: String?,
        chatViewType: ChatViewType,
        context: Context,
        imageLoader: ImageLoader,
        holders: MessageHolders = MessageHolders()
): MessagesListAdapter<AbstractMessage>(senderId, holders, imageLoader) {

    fun getItems(): List<AbstractMessage> {
        return items.toList().map { it.item }
                // Filter Date Wrapper
                .filterIsInstance<AbstractMessage>()
    }

    companion object {
        // Inner class cant be set as init fails caused by framework.
        fun setStatus (message: UserMessage?, itemView: View, chatViewType: ChatViewType) {
            val status = itemView.findViewById<IconicsTextView>(R.id.status)

            Iconics.Builder().on(status).build()
            status.text = ""

            if (message?.messageLoading?.value == true) {
                ChatAdapterHelper.startInfiniteRotation(status, message.messageLoading)
                status.text = "{faw-circle-notch}"
            } else if (chatViewType != ChatViewType.OFFLINE_GROUP) {
                if (message?.messageConfirmed == true) {
                    status.text = "{faw-check}"
                } else if (message?.messageTokenSkipped == true) {
                    status.text = "{faw-exclamation-triangle}"
                } else {
                    status.text = "{faw-question-circle}"
                }
            }

            if (message?.messageRead != true) {
                status.text = "new " + status.text
            }
        }
    }


    enum class ChatViewType {
        SINGLE_USER,
        OFFLINE_GROUP,
        ONLINE_GROUP
    }

    init {
        val payload = ChatViewHolderPayload(chatViewType, context)
        val contentChecker = MessageContentChecker()

        holders.registerContentType(
                MessageContentType.CONTENT_TYPE_TEXT.id,
                IncomingMessageViewholder::class.java,
                payload,
                R.layout.item_custom_incoming_text_message,
                OutgoingMessageViewholder::class.java,
                payload,
                R.layout.item_custom_outcoming_text_message,
                contentChecker
            )
        holders.registerContentType(
                MessageContentType.CONTENT_TYPE_SIMPLE_FILE.id,
                IncomingSimpleFileMessageViewholder::class.java,
                payload,
                R.layout.item_custom_incoming_simple_file_message,
                OutgoingSimpleFileMessageViewholder::class.java,
                payload,
                R.layout.item_custom_outcoming_simple_file_message,
                contentChecker
        )
        holders.registerContentType(
                MessageContentType.CONTENT_TYPE_IMAGE.id,
                IncomingImageMessageViewholder::class.java,
                payload,
                R.layout.item_custom_incoming_media_file_message,
                OutgoingImageMessageViewholder::class.java,
                payload,
                R.layout.item_custom_outcoming_media_file_message,
                contentChecker
        )
        holders.registerContentType(
                MessageContentType.CONTENT_TYPE_VIDEO.id,
                IncomingVideoMessageViewholder::class.java,
                payload,
                R.layout.item_custom_incoming_media_file_message,
                OutgoingVideoMessageViewholder::class.java,
                payload,
                R.layout.item_custom_outcoming_media_file_message,
                contentChecker
        )
    }

}
