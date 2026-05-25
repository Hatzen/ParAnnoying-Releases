package de.hartz.software.parannoying.offline.adapters.view.chat

import android.view.View
import com.mikepenz.iconics.Iconics
import com.stfalcon.chatkit.messages.MessageHolders
import de.hartz.software.parannoying.offline.adapters.view.chat.ChatAdapter.Companion.setStatus
import de.hartz.software.parannoying.offline.adapters.view.chat.ChatAdapterHelper.setUserImage
import de.hartz.software.parannoying.offline.model.domain.messages.UserMessage

class IncomingMessageViewholder(itemView: View?, payload: Any?) : MessageHolders.IncomingTextMessageViewHolder<UserMessage>(itemView, payload) {

    override fun onBind(message: UserMessage?) {
        super.onBind(message)
        text?.let{ textview ->
            Iconics.Builder().on(textview).build()
        }

        val chatViewType = (payload as ChatViewHolderPayload).chatViewType
        val payload = (payload as ChatViewHolderPayload)

        setUserImage(itemView, message!!, payload, imageLoader)

        setStatus(message, itemView, chatViewType)
    }
}