package de.hartz.software.parannoying.offline.adapters.view.chat

import android.view.View
import androidx.lifecycle.Observer
import com.mikepenz.iconics.Iconics
import com.stfalcon.chatkit.messages.MessageHolders
import de.hartz.software.parannoying.offline.adapters.view.chat.ChatAdapter.Companion.setStatus
import de.hartz.software.parannoying.offline.model.domain.messages.UserMessage

class OutgoingMessageViewholder(itemView: View?, payload: Any?) : MessageHolders.OutcomingTextMessageViewHolder<UserMessage>(itemView, payload) {

        override fun onBind(message: UserMessage?) {
            super.onBind(message)
            text?.let{ textview ->
                Iconics.Builder().on(textview).build()
            }
            val chatViewType = (payload as ChatViewHolderPayload).chatViewType

            val observer = object : Observer<Boolean> {
                override fun onChanged(t: Boolean) {
                    if (!t) {
                        message?.messageLoading?.removeObserver(this)
                    }
                    setStatus(message, itemView, chatViewType)
                }
            }
            message?.messageLoading?.observeForever(observer)
            setStatus(message, itemView, chatViewType)
        }
    }