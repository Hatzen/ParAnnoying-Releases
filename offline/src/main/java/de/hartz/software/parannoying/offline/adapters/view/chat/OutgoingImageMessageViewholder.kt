package de.hartz.software.parannoying.offline.adapters.view.chat

import android.view.View
import com.stfalcon.chatkit.messages.MessageHolders
import de.hartz.software.parannoying.offline.model.domain.messages.FileMessage

class OutgoingImageMessageViewholder(itemView: View?, var payload: Any) :
        MessageHolders.OutcomingTextMessageViewHolder<FileMessage>(itemView, payload) {

    override fun onBind(message: FileMessage) {
        super.onBind(message)
        val payload = (payload as ChatViewHolderPayload)

        ChatAdapterHelper.initMediaMessage(itemView, payload, message, imageLoader)
        ChatAdapterHelper.setOverlayForFiles(itemView, message.numberOfFiles)

        ChatAdapterHelper.setTime(itemView, message)
    }
}