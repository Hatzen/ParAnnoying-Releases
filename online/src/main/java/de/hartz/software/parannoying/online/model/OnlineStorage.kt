package de.hartz.software.parannoying.online.model

import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.interfaces.di.StorageInterface
import de.hartz.software.parannoying.core.model.domain.CrashLog
import de.hartz.software.parannoying.core.model.domain.DeviceRole
import de.hartz.software.parannoying.core.model.domain.OnboardingSteps
import de.hartz.software.parannoying.core.model.domain.settings.HiddenSettings
import de.hartz.software.parannoying.core.model.persistence.realm.RealmHelper
import de.hartz.software.parannoying.online.model.domain.InboxEncryptedMessage
import de.hartz.software.parannoying.online.model.domain.LoggedEncryptedMessage
import de.hartz.software.parannoying.online.model.domain.OutboxEncryptedMessage
import de.hartz.software.parannoying.online.model.domain.SettingsOnline
import de.hartz.software.parannoying.online.model.mapper.DeviceDataMapper
import de.hartz.software.parannoying.online.model.persistence.CrashLogPersistence
import de.hartz.software.parannoying.online.model.persistence.DeviceDataOnlinePersistence
import de.hartz.software.parannoying.online.model.persistence.DeviceRolePersistence
import de.hartz.software.parannoying.online.model.persistence.InboxEncryptedMessagePersistence
import de.hartz.software.parannoying.online.model.persistence.LoggedEncryptedMessagePersistence
import de.hartz.software.parannoying.online.model.persistence.OnboardingStepsPersistence
import de.hartz.software.parannoying.online.model.persistence.OutboxEncryptedMessagePersistence
import de.hartz.software.parannoying.online.model.persistence.SettingsPersistence


class OnlineStorage(override val realmHelper: RealmHelper): StorageInterface<HiddenSettings, SettingsOnline> {

    /**
     * Holds the notificationId got by firebase. Only needed when user is not yet connected to firebase with an account.
     */
    var tmpNotificationId : String? = null


    val useGoogleApi: Boolean
        get() = deviceDataOnlinePersistence.useGoogleApi

    /**
     * The username to login with.
     * When stored offline it has no pre- and post fix
     */
    override var onlineUserEmail: String?
        get() = deviceDataOnlinePersistence.onlineUserEmail
        set(t) = persistOnlineUserEmail(t!!)
    /**
     * The password to login with.
     */
    override var onlineUserIdPassword: String?
        get() = deviceDataOnlinePersistence.onlineUserIdPassword
        set(t) = persistOnlineUserIdPassword(t!!)
    /**
     * The onlineUserId which holds all messages within backend. Mainly based on the email address.
     */
    override var onlineUserId: String?
        get() = deviceDataOnlinePersistence.onlineUserId
        set(t) = persistOnlineUserId(t)

    override var deviceRole: DeviceRole
        get() = DeviceDataMapper(realmHelper).toDomain(deviceDataOnlinePersistence.deviceRole ?: DeviceRolePersistence(DeviceRole.UNDEFINED))
        set(t) = persistDeviceRole(t)

    override lateinit var onboardingSteps: List<OnboardingSteps>
    override val crashLog: List<CrashLog>
        get() = readCrashlogs()

    lateinit var deviceDataOnlinePersistence: DeviceDataOnlinePersistence

    init {
        deviceDataOnlinePersistence = readDeviceData()
        onboardingSteps = readOnboardingSteps()
    }

    @Synchronized // Added as e2e tests may be able to receive twice with realm.
    fun addInboxEncryptedMessage(rawMessage: String, sendAt: Long = -1, isFileMessage: Boolean = false) {
        val currentTime = IOHelper.getCurrentDateAsUnixTimestamp() * 1000
        // If message already exists update timestamp and avoid multiple instances.
        // TODO: cache list or something..
        /*
        for (message in inboxEncryptedMessages) {
            if (message.message == rawMessage) {
                message.receivedAt = currentTime
                return
            }
        }*/
        val message = InboxEncryptedMessage(rawMessage, currentTime, sendAt, isFileMessage)
        persistInboxEncryptedMessages(message)
    }

    // device data

     fun readDeviceData(): DeviceDataOnlinePersistence {
        val store = realmHelper.getThreadInstance()
        val storeData = store.where(DeviceDataOnlinePersistence::class.java)
            .findFirst()

         var result = DeviceDataOnlinePersistence()
         if (storeData != null) {
             result = store.copyFromRealm(storeData)
         }
        store.close()
        deviceDataOnlinePersistence = result
        return result
    }


     fun persistDeviceData(deviceDataOnlinePersistence: DeviceDataOnlinePersistence) {
        val store = realmHelper.getThreadInstance()
        store.executeTransaction{
            it.insertOrUpdate(deviceDataOnlinePersistence) // Insert without returning a managed copy.
        }
        store.close()
    }

     fun setUseGoogleApi(useGoogleApi: Boolean) {
         updateDeviceData {
             it.useGoogleApi = useGoogleApi
         }
    }

     fun persistOnlineUserId(onlineUserId: String?) {
         updateDeviceData {
             it.onlineUserId = onlineUserId
         }
    }

     fun persistOnlineUserEmail(onlineUserEmail: String) {
         updateDeviceData {
            it.onlineUserEmail = onlineUserEmail
        }
    }

     fun persistOnlineUserIdPassword(onlineUserIdPassword: String) {
         updateDeviceData {
             it.onlineUserIdPassword = onlineUserIdPassword
         }
    }

    private fun updateDeviceData(updateFunction: (DeviceDataOnlinePersistence) -> Unit) {
        val store = realmHelper.getThreadInstance()
        store.executeTransaction{
            updateFunction(deviceDataOnlinePersistence)
            deviceDataOnlinePersistence = store.copyFromRealm(store.copyToRealmOrUpdate(deviceDataOnlinePersistence))
        }
        store.close()
    }

    fun persistDeviceRole(deviceRole: DeviceRole) {
        updateDeviceData {
            it.deviceRole = DeviceDataMapper(realmHelper).toPersistence(deviceRole)
        }
    }

    // InboxEncryptedMessage

     fun readInboxEncryptedMessages(): List<InboxEncryptedMessage> {
        val store = realmHelper.getThreadInstance()
        val storeData = store.where(InboxEncryptedMessagePersistence::class.java)
            .findAll()
        val result = store.copyFromRealm(storeData).map { DeviceDataMapper(realmHelper).toDomain(it) }.toList()
        store.close()
        return result
    }

     fun persistInboxEncryptedMessages(inboxEncryptedMessage: InboxEncryptedMessage) {
        val storeData = DeviceDataMapper(realmHelper).toPersistence(inboxEncryptedMessage)
        val store = realmHelper.getThreadInstance()
        store.executeTransaction{
            it.insertOrUpdate(storeData) // Insert without returning a managed copy.
        }
        store.close()
    }

     fun deleteInboxEncryptedMessages(inboxEncryptedMessage: InboxEncryptedMessage) {
        val storeData = DeviceDataMapper(realmHelper).toPersistence(inboxEncryptedMessage)
        val store = realmHelper.getThreadInstance()
        store.executeTransaction{
            val storeData = store.where(InboxEncryptedMessagePersistence::class.java).equalTo("persistenceId", inboxEncryptedMessage.persistenceId).findAll()
            storeData.deleteAllFromRealm()
        }
        store.close()
    }

     fun deleteAllInboxEncryptedMessages() {
        val store = realmHelper.getThreadInstance()
         store.executeTransaction {
             store.delete(InboxEncryptedMessagePersistence::class.java)
         }
        store.close()
    }

    fun deleteInboxEncryptedMessagesByIds(ids: List<Long>) {
        val store = realmHelper.getThreadInstance()
        store.executeTransaction {
            store.where(InboxEncryptedMessagePersistence::class.java).`in`("id", ids.toTypedArray()).findAll().deleteAllFromRealm()
        }
        store.close()
    }

    // OutboxEncryptedMessage

     fun readOutboxEncryptedMessages(): List<OutboxEncryptedMessage> {
        val store = realmHelper.getThreadInstance()
        val storeData = store.where(OutboxEncryptedMessagePersistence::class.java)
            .findAll()
        val result = store.copyFromRealm(storeData).map { DeviceDataMapper(realmHelper).toDomain(it) }.toMutableList()
        store.close()
        return result
    }

    fun deleteOutboxEncryptedMessages(inboxEncryptedMessage: OutboxEncryptedMessage) {
        val storeData = DeviceDataMapper(realmHelper).toPersistence(inboxEncryptedMessage)
        val store = realmHelper.getThreadInstance()
        store.executeTransaction{
            val storeData = store.where(OutboxEncryptedMessagePersistence::class.java).equalTo("persistenceId", inboxEncryptedMessage.persistenceId).findAll()
            storeData.deleteAllFromRealm()
        }
        store.close()
    }

     fun persistOutboxEncryptedMessages(inboxEncryptedMessage: OutboxEncryptedMessage) {
        val storeData = DeviceDataMapper(realmHelper).toPersistence(inboxEncryptedMessage)
        val store = realmHelper.getThreadInstance()
        store.executeTransaction{
            it.insertOrUpdate(storeData) // Insert without returning a managed copy.
        }
        store.close()
    }

    // OutboxEncryptedMessage

    fun readLoggedEncryptedMessages(): List<LoggedEncryptedMessage> {
        val store = realmHelper.getThreadInstance()
        val storeData = store.where(LoggedEncryptedMessagePersistence::class.java)
            .findAll()
        val result = store.copyFromRealm(storeData).map { DeviceDataMapper(realmHelper).toDomain(it) }.toList()
        store.close()
        return result
    }

    fun persistLoggedEncryptedMessages(inboxEncryptedMessage: LoggedEncryptedMessage) {
        val storeData = DeviceDataMapper(realmHelper).toPersistence(inboxEncryptedMessage)
        val store = realmHelper.getThreadInstance()
        store.executeTransaction{
            it.insertOrUpdate(storeData) // Insert without returning a managed copy.
        }
        store.close()
    }

    fun deleteAllLoggedEncryptedMessages() {
        val store = realmHelper.getThreadInstance()
        store.delete(LoggedEncryptedMessagePersistence::class.java)
        store.close()
    }
    // Onboardingsteps

     fun readOnboardingSteps(): List<OnboardingSteps> {
        val store = realmHelper.getThreadInstance()
        val storeData = store.where(OnboardingStepsPersistence::class.java)
            .findAll()
        val result = store.copyFromRealm(storeData).map { DeviceDataMapper(realmHelper).toDomain(it) }.toList()
        store.close()
        return result
    }

     fun persistOnboardingSteps(inboxEncryptedMessage: OnboardingSteps) {
        val storeData = DeviceDataMapper(realmHelper).toPersistence(inboxEncryptedMessage)
        val store = realmHelper.getThreadInstance()
        store.executeTransaction{
            it.insert(storeData) // Insert without returning a managed copy.
        }
        store.close()
    }

    // Settings

    override fun readSettings(): SettingsOnline {
        val store = realmHelper.getThreadInstance()
        val storeData = store.where(SettingsPersistence::class.java)
            .findAll()
        val result = store.copyFromRealm(storeData).map { DeviceDataMapper(realmHelper).toDomain(it) }.firstOrNull()
        store.close()
        return result ?: SettingsOnline()
    }

    override fun isMigrationNeeded(): Boolean {
        return realmHelper.isMigrationNeeded()
    }

    override fun runMigration() {
        realmHelper.getThreadInstance().use {
            // Migration done
        }
    }

    override fun persistSettings(settingsOnline: SettingsOnline) {
        val storeData = DeviceDataMapper(realmHelper).toPersistence(settingsOnline)
        val store = realmHelper.getThreadInstance()
        store.executeTransaction{
            it.insertOrUpdate(storeData) // Insert without returning a managed copy.
        }
        store.close()
    }
    // Crashlog

    override fun addCrashlog(forwardDataset: CrashLog) {
        val storeData = DeviceDataMapper(realmHelper).toPersistence(forwardDataset)
        val store = realmHelper.getThreadInstance()
        store.executeTransaction{
            it.insertOrUpdate(storeData) // Insert without returning a managed copy.
        }
        store.close()
    }

    fun readCrashlogs(): List<CrashLog> {
        val store = realmHelper.getThreadInstance()
        val storeData = store.where(CrashLogPersistence::class.java)
                .findAll()
        val result = store.copyFromRealm(storeData).map { DeviceDataMapper(realmHelper).toDomain(it) }.toList()
        store.close()
        return result
    }

    override fun deleteAllCrashlogs() {
        val store = realmHelper.getThreadInstance()
        store.executeTransaction {
            it.delete(CrashLogPersistence::class.java)
        }
        store.close()
    }

}