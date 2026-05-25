package de.hartz.software.parannoying.core.model.domain.settings

import de.hartz.software.parannoying.core.helper.development.DevelopmentUtil

open class HiddenSettings {

    /**
     * Flag indicating the user is a developer and internet connection is allowed and some other
     * critical features are disabled. Also dummy users are created to be able to test message view etc.
     */
    var developerMode: Boolean

    // Boolean indicating if the hidden settings are accessible
    var developerOptionsActivated: Boolean

    init {
        developerMode = DevelopmentUtil.isDebugMode()
        developerOptionsActivated = false
    }
}

// TODO: Maybe SECURE should be an option as well, and the docs might be more verbose.
/*

        // TODO: #123121 How to access the storage here? And how to set this BEFORE this init Method is called?
        // Storage.settings.hiddenSettings.developerMode

        /**
         * Flag indicating the user is a developer and internet connection is allowed and some other
         * critical features are disabled. Also dummy users are created to be able to test message view etc.
         */
        val DEVELOPER_MODE: Boolean get() = settings.hiddenSettings.developerMode
        /**
         * Flag indicating if all encryption is disabled except of the magic hardcoded encryption.
         */
        var SECURE = true

        /**
         * Flag indicating if user actived developer mode and can open hidden settings.
         */
        val DEVELOPER_OPTIONS_ACTIVATED: Boolean get() = settings.hiddenSettings.developerOptionsActivated
 */