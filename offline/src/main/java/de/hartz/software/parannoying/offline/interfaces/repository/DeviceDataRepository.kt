package de.hartz.software.parannoying.offline.interfaces.repository

import de.hartz.software.parannoying.core.model.domain.DeviceRole
import de.hartz.software.parannoying.core.model.domain.OnboardingSteps
import de.hartz.software.parannoying.offline.model.domain.settings.OfflineSettings
import de.hartz.software.parannoying.offline.model.persistence.DeviceDataOfflinePersistence


interface DeviceDataRepository {
    val userId: String
    // For both

    /**
     * The username to login with.
     * When stored offline it has no pre- and post fix
     */
    var onlineUserEmail: String?
    /**
     * The password to login with.
     */
    var onlineUserIdPassword: String?
    /**
     * The onlineUserId which holds all messages within backend. Mainly based on the email address.
     */
     var onlineUserId: String?

     var deviceRole: DeviceRole

    // Cannot change much so can be accessed always as cached instance.
     var onboardingSteps: MutableList<OnboardingSteps>

    var deviceDataOfflinePersistence: DeviceDataOfflinePersistence

    fun readDeviceData(): DeviceDataOfflinePersistence

    fun setUserId(userId: String?)

    fun persistOnlineUserId(onlineUserId: String?)

    fun persistOnlineUserEmail(onlineUserEmail: String)

    fun persistOnlineUserIdPassword(onlineUserIdPassword: String)

    fun persistDeviceRole(deviceRole: DeviceRole)


    // Settings

     fun readSettings(): OfflineSettings

     fun persistSettings(settingsOnline: OfflineSettings)

    // Onboardingsteps

    fun readOnboardingSteps(): MutableList<OnboardingSteps>

    fun persistOnboardingSteps(inboxEncryptedMessage: OnboardingSteps)

    fun deleteAllOnboardingSteps()

}