package de.hartz.software.parannoying.online.model.domain

import de.hartz.software.parannoying.core.model.domain.settings.HiddenSettings
import de.hartz.software.parannoying.core.model.domain.settings.Settings

class SettingsOnline: Settings<HiddenSettings>() {

    var serverConfigs: MutableList<ServerConfig> = mutableListOf()
    var logEncryptedMessages: Boolean
    var reportErrorOnline: Boolean

    init {
        logEncryptedMessages = true
        reportErrorOnline = false
        hiddenSettings = HiddenSettings()
    }
}