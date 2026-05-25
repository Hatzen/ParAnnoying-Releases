package de.hartz.software.parannoying.offline.model.domain.settings

import de.hartz.software.parannoying.core.model.domain.settings.HiddenSettings

class OfflineHiddenSettings: HiddenSettings() {

    // TODO: More granular disable specific features? USB, Cellular, GPS etc
    var allowInternet: Boolean
    var allowScreenshots: Boolean

    init {
        allowInternet = true
        allowScreenshots = false
    }
}