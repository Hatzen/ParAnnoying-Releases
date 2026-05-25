package de.hartz.software.parannoying.offline.adapters.view.chat

import android.view.View
import com.stfalcon.chatkit.messages.MessageHolders
import de.hartz.software.parannoying.offline.adapters.view.chat.ChatAdapterHelper.initOpenSimpleFile
import de.hartz.software.parannoying.offline.model.domain.messages.FileMessage


class OutgoingSimpleFileMessageViewholder(itemView: View?, payload: Any?) :
        MessageHolders.OutcomingTextMessageViewHolder<FileMessage>(itemView, payload) {

    override fun onBind(message: FileMessage) {
        super.onBind(message)
        val payload = (payload as ChatViewHolderPayload)

        initOpenSimpleFile(itemView, message, payload)

        ChatAdapterHelper.setTime(itemView, message)
    }

}