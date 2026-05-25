package de.hartz.software.parannoying.core.interfaces.di

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import de.hartz.software.parannoying.core.activities.insecured.ExitActivity
import de.hartz.software.parannoying.core.model.VolatileStorage.settings
import de.hartz.software.parannoying.core.model.domain.CrashLog
import de.hartz.software.parannoying.core.model.domain.DeviceRole
import de.hartz.software.parannoying.core.model.domain.OnboardingSteps
import de.hartz.software.parannoying.core.model.domain.settings.HiddenSettings
import de.hartz.software.parannoying.core.model.domain.settings.Settings
import de.hartz.software.parannoying.core.model.persistence.realm.RealmHelper

interface StorageInterface<H: HiddenSettings, S: Settings<H>> {

    val realmHelper: RealmHelper

    companion object {
        // TODO: REMOVE! Make use of
        val DEVELOPER_MODE = true
        val SECURE = true

        fun deleteAll(context: Context) {
            // TODO: Delete all images.
            // TODO: Delete all files.
            try {
                // clearing app data
                // https://stackoverflow.com/questions/6134103/clear-applications-data-programmatically
                if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                    (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData() // note: it has a return value!
                } else {
                    val packageName = context.getPackageName()
                    val runtime = Runtime.getRuntime()
                    runtime.exec("pm clear $packageName")
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
            ExitActivity.exitApplication(context)
        }
    }

    /**
     * Flag indicating the user is a developer and internet connection is allowed and some other
     * critical features are disabled. Also dummy users are created to be able to test message view etc.
     */
    val DEVELOPER_MODE: Boolean get() = settings.hiddenSettings.developerMode
    /**
     * Flag indicating if all encryption is disabled except of the magic hardcoded encryption.
     */
    val SECURE: Boolean get() = true

    /**
     * Flag indicating if user actived developer mode and can open hidden settings.
     */
    val DEVELOPER_OPTIONS_ACTIVATED: Boolean get() = settings.hiddenSettings.developerOptionsActivated


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
    val onboardingSteps: List<OnboardingSteps> // TODO: Make resettable via settings
    val crashLog: List<CrashLog>

    fun readSettings(): S

    fun persistSettings(settings: S)

    fun updateSettings(updateFunc: (S) -> Unit) {
        val settings = readSettings()
        updateFunc(settings)
        persistSettings(settings)
    }

    fun isOnlineDevice() : Boolean {
        if (deviceRole.roleId == DeviceRole.ONLINE)
            return true
        return false
    }

    fun isOfflineDevice() : Boolean {
        if (deviceRole.roleId == DeviceRole.OFFLINE)
            return true
        return false
    }

    fun isUndefinedDevice() : Boolean {
        if (deviceRole.roleId == DeviceRole.UNDEFINED)
            return true
        return false
    }

    fun isMigrationNeeded(): Boolean

    fun runMigration()

    fun addCrashlog(forwardDataset: CrashLog)

    fun deleteAllCrashlogs()
}