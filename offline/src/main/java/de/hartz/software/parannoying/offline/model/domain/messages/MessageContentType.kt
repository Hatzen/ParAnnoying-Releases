package de.hartz.software.parannoying.offline.model.domain.messages

import com.stfalcon.chatkit.messages.MessageHolders

enum class MessageContentType(val id: Byte) {
    CONTENT_TYPE_TEXT(1),
    CONTENT_TYPE_IMAGE(2),
    CONTENT_TYPE_VIDEO(3),
    CONTENT_TYPE_VOICE(4),
    CONTENT_TYPE_SIMPLE_FILE(5)
    // CONTENT_TYPE_MAP
    // CONTENT_TYPE_SURVEY
    // CONTENT_TYPE_CONTACT
    // Binary files and updates?
}

private fun isContentType(message: AbstractMessage?, type: MessageContentType): Boolean {
    return message is FileMessage && message.messageContentTypes.size == 1 && message.messageContentTypes.contains(type)
}

class MessageContentChecker: MessageHolders.ContentChecker<AbstractMessage> {
    override fun hasContentFor(message: AbstractMessage?, type: Byte): Boolean {
        when (type) {
            MessageContentType.CONTENT_TYPE_TEXT.id ->
                if (message is UserMessage) {
                    return true
                }
            MessageContentType.CONTENT_TYPE_VIDEO.id ->
                if (isContentType(message, MessageContentType.CONTENT_TYPE_VIDEO)) {
                    return true
                }
            MessageContentType.CONTENT_TYPE_IMAGE.id ->
                if (isContentType(message, MessageContentType.CONTENT_TYPE_IMAGE)) {
                    return true
                }
            MessageContentType.CONTENT_TYPE_SIMPLE_FILE.id ->
                if (isContentType(message, MessageContentType.CONTENT_TYPE_SIMPLE_FILE) || message is FileMessage && message.messageContentTypes.size > 1) {
                    return true
                }
        }
        return false
    }
}