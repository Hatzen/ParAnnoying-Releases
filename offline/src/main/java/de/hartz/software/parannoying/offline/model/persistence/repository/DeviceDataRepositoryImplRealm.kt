package de.hartz.software.parannoying.offline.model.persistence.repository

import de.hartz.software.parannoying.core.model.domain.DeviceRole
import de.hartz.software.parannoying.core.model.domain.OnboardingSteps
import de.hartz.software.parannoying.core.model.persistence.realm.RealmHelper
import de.hartz.software.parannoying.offline.interfaces.repository.DeviceDataRepository
import de.hartz.software.parannoying.offline.model.domain.settings.OfflineSettings
import de.hartz.software.parannoying.offline.model.mapper.DeviceDataMapper
import de.hartz.software.parannoying.offline.model.persistence.DeviceDataOfflinePersistence
import de.hartz.software.parannoying.offline.model.persistence.OnboardingStepsPersistence
import de.hartz.software.parannoying.offline.model.persistence.settings.DeviceRolePersistence
import de.hartz.software.parannoying.offline.model.persistence.settings.SettingsPersistence


class DeviceDataRepositoryImplRealm(val realmHelper: RealmHelper): DeviceDataRepository {
    // This should not be generated more than once.
    override val userId: String
        get() = deviceDataOfflinePersistence.userId!!


    // For both

    /**
     * The username to login with.
     * When stored offline it has no pre- and post fix
     */
    override var onlineUserEmail: String?
        get() = deviceDataOfflinePersistence.onlineUserEmail
        set(t) = persistOnlineUserEmail(t!!)
    /**
     * The password to login with.
     */
    override var onlineUserIdPassword: String?
        get() = deviceDataOfflinePersistence.onlineUserIdPassword
        set(t) = persistOnlineUserIdPassword(t!!)
    /**
     * The onlineUserId which holds all messages within backend. Mainly based on the email address.
     */
    override var onlineUserId: String?
        get() = deviceDataOfflinePersistence.onlineUserId
        set(t) = persistOnlineUserId(t)

    override var deviceRole: DeviceRole
        get() = DeviceDataMapper(realmHelper).toDomain(deviceDataOfflinePersistence.deviceRole ?: DeviceRolePersistence(DeviceRole.UNDEFINED))
        set(t) = persistDeviceRole(t)

    // Cannot change much so can be accessed always as cached instance.
    override lateinit var onboardingSteps: MutableList<OnboardingSteps>

    override lateinit var deviceDataOfflinePersistence: DeviceDataOfflinePersistence

    init {
        deviceDataOfflinePersistence = readDeviceData()
        onboardingSteps = readOnboardingSteps()
    }

    // DeviceDataOfflinePersistence
    // TODO: but with keeping device data reference we might delete input written in other threads/ processes..
    //    at least we should read within an transaction before setting any value.

    override fun readDeviceData(): DeviceDataOfflinePersistence {
        val store = realmHelper.getThreadInstance()
        val storeData = store.where(DeviceDataOfflinePersistence::class.java)
            .findFirst()
        var result = DeviceDataOfflinePersistence()
        if (storeData != null) {
            result = store.copyFromRealm(storeData)
        }
        store.close()
        deviceDataOfflinePersistence = result
        return result
    }

    override fun setUserId(userId: String?) {
        updateDeviceData {
            it.userId = userId
        }
    }

    override fun persistOnlineUserId(onlineUserId: String?) {
        updateDeviceData {
            it.onlineUserId = onlineUserId
        }
    }

    override fun persistOnlineUserEmail(onlineUserEmail: String) {
        updateDeviceData {
            it.onlineUserEmail = onlineUserEmail
        }
    }

    override fun persistOnlineUserIdPassword(onlineUserIdPassword: String) {
        updateDeviceData {
            it.onlineUserIdPassword = onlineUserIdPassword
        }
    }

    private fun updateDeviceData(updateFunction: (DeviceDataOfflinePersistence) -> Unit) {
        val store = realmHelper.getThreadInstance()
        store.executeTransaction{
            updateFunction(deviceDataOfflinePersistence)
            deviceDataOfflinePersistence = store.copyFromRealm(store.copyToRealmOrUpdate(deviceDataOfflinePersistence))
        }
        store.close()
    }


    override fun persistDeviceRole(deviceRole: DeviceRole) {
        updateDeviceData {
            it.deviceRole = DeviceDataMapper(realmHelper).toPersistence(deviceRole)
        }
    }


    // Settings

    override fun readSettings(): OfflineSettings {
        val store = realmHelper.getThreadInstance()
        val storeData = store.where(SettingsPersistence::class.java)
            .findAll()
        val result = store.copyFromRealm(storeData).map { DeviceDataMapper(realmHelper).toDomain(it) }.firstOrNull()
        store.close()
        return result ?: OfflineSettings()
    }

    override fun persistSettings(settingsOnline: OfflineSettings) {
        val storeData = DeviceDataMapper(realmHelper).toPersistence(settingsOnline)
        val store = realmHelper.getThreadInstance()
        store.executeTransaction{
            it.copyToRealmOrUpdate(storeData)
        }
        store.close()
    }

    // Onboardingsteps

    override fun readOnboardingSteps(): MutableList<OnboardingSteps> {
        val store = realmHelper.getThreadInstance()
        val storeData = store.where(OnboardingStepsPersistence::class.java)
            .findAll()
        val result = store.copyFromRealm(storeData).map { DeviceDataMapper(realmHelper).toDomain(it) }.toMutableList()
        store.close()
        return result
    }

    override fun persistOnboardingSteps(inboxEncryptedMessage: OnboardingSteps) {

        val storeData = DeviceDataMapper(realmHelper).toPersistence(inboxEncryptedMessage)
        val store = realmHelper.getThreadInstance()
        store.executeTransaction{
            it.insert(storeData)
        }
        store.close()
        onboardingSteps.add(inboxEncryptedMessage)
    }

    override fun deleteAllOnboardingSteps() {
        val store = realmHelper.getThreadInstance()
        store.executeTransaction {
            it.delete(OnboardingStepsPersistence::class.java)
            onboardingSteps.clear()
        }
        store.close()
    }

}