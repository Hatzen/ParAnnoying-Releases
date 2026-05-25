package de.hartz.software.parannoying.offline.adapters.view.chat

import android.view.View
import com.stfalcon.chatkit.messages.MessageHolders
import de.hartz.software.parannoying.offline.adapters.view.chat.ChatAdapterHelper.initOpenSimpleFile
import de.hartz.software.parannoying.offline.model.domain.messages.FileMessage

class IncomingImageMessageViewholder(itemView: View?, var payload: Any)
    : MessageHolders.IncomingTextMessageViewHolder<FileMessage>(itemView, payload) {

    override fun onBind(message: FileMessage) {
        super.onBind(message)
        val payload = (payload as ChatViewHolderPayload)

        ChatAdapterHelper.initMediaMessage(itemView, payload, message, imageLoader)
        ChatAdapterHelper.setOverlayForFiles(itemView, message.numberOfFiles)

        ChatAdapterHelper.setTime(itemView, message)
        ChatAdapterHelper.setUserImage(itemView, message, payload, imageLoader)
    }
}