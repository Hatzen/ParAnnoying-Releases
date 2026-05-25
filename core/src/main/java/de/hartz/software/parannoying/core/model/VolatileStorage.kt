package de.hartz.software.parannoying.core.model

import de.hartz.software.parannoying.core.interfaces.di.StorageInterface
import de.hartz.software.parannoying.core.model.domain.CrashLog
import de.hartz.software.parannoying.core.model.domain.DeviceRole
import de.hartz.software.parannoying.core.model.domain.OnboardingSteps
import de.hartz.software.parannoying.core.model.domain.settings.HiddenSettings
import de.hartz.software.parannoying.core.model.domain.settings.Settings
import de.hartz.software.parannoying.core.model.domain.settings.VolatileSettings
import de.hartz.software.parannoying.core.model.persistence.realm.RealmHelper


object VolatileStorage: StorageInterface<HiddenSettings, Settings<HiddenSettings>> {
    override var onlineUserEmail: String? = null
    override var onlineUserIdPassword: String? = null
    override var onlineUserId: String? = null
    override var deviceRole: DeviceRole = DeviceRole(DeviceRole.UNDEFINED)
    override val realmHelper: RealmHelper
        get() = throw UnsupportedOperationException("Not Supported.")

    var settings: Settings<HiddenSettings>
    override lateinit var onboardingSteps: ArrayList<OnboardingSteps>
    override val crashLog: List<CrashLog>
        get() = throw RuntimeException("crashlogs not supported.")

    init {
        settings = VolatileSettings()
        onboardingSteps = ArrayList()
    }

    override fun readSettings(): Settings<HiddenSettings> {
        return settings
    }

    override fun isMigrationNeeded(): Boolean {
        return false
    }

    override fun runMigration() {
        throw UnsupportedOperationException("Migration not necessary.")
    }

    override fun addCrashlog(forwardDataset: CrashLog) {
        throw UnsupportedOperationException("crashlogs not supported.")
    }

    override fun deleteAllCrashlogs() {
        throw UnsupportedOperationException("crashlogs not supported.")
    }

    override fun persistSettings(settings: Settings<HiddenSettings>) {
        this.settings = settings
    }
}