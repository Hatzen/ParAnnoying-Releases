package de.hartz.software.parannoying.offline.model.domain.settings

import de.hartz.software.parannoying.core.model.domain.settings.Settings

class OfflineSettings: Settings<OfflineHiddenSettings>() {
    var messageSecurity: MessageSecurity
    var syncAllMessages: Boolean

    init {
        syncAllMessages = false
        messageSecurity = MessageSecurity.NONE
        hiddenSettings =  OfflineHiddenSettings()
    }

}