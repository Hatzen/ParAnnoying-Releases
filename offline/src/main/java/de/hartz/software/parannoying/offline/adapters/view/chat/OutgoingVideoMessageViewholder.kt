package de.hartz.software.parannoying.offline.adapters.view.chat

import android.view.View
import com.stfalcon.chatkit.messages.MessageHolders
import com.stfalcon.chatkit.messages.MessageHolders.BaseOutcomingMessageViewHolder
import de.hartz.software.parannoying.offline.adapters.view.chat.ChatAdapterHelper.addPlayOverlay
import de.hartz.software.parannoying.offline.adapters.view.chat.ChatAdapterHelper.initMediaMessage
import de.hartz.software.parannoying.offline.adapters.view.chat.ChatAdapterHelper.setOverlayForFiles
import de.hartz.software.parannoying.offline.model.domain.messages.FileMessage

class OutgoingVideoMessageViewholder(itemView: View?, var payload: Any) :
        MessageHolders.OutcomingTextMessageViewHolder<FileMessage>(itemView, payload) {

    override fun onBind(message: FileMessage) {
        super.onBind(message)
        val payload = payload as ChatViewHolderPayload

        initMediaMessage(itemView, payload, message, imageLoader)
        setOverlayForFiles(itemView, message.numberOfFiles)
        addPlayOverlay(itemView, payload)

        ChatAdapterHelper.setTime(itemView, message)
    }

}