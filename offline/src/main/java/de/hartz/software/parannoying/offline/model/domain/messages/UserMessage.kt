package de.hartz.software.parannoying.offline.model.domain.messages

import com.mikepenz.iconics.Iconics
import de.hartz.software.parannoying.offline.model.domain.settings.MessageSecurity
import de.hartz.software.parannoying.offline.model.OfflineStorage

class UserMessage : AbstractMessage() {

    lateinit var message: String

    override fun getText(): String {
        if (OfflineStorage.INSTANCE.readSettings().messageSecurity == MessageSecurity.HIDE) {
            val hiddenString =  message.replace(Regex("."), "{faw-wave-square}")
            Iconics.Builder().on(hiddenString).build()
            return hiddenString
        }
        return message
    }
}
