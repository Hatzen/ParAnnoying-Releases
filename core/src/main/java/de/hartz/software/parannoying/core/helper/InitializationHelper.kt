package de.hartz.software.parannoying.core.helper

import android.content.Context
import android.content.SharedPreferences
import de.hartz.software.parannoying.core.model.persistence.realm.RealmHelper

/**
 * Usually the StorageInterface.deviceRole is the way to identify the device.
 * But on initalization and onboarding we need a idiomatic way to determine it.
 */
object InitializationHelper {

    const val UNDEFINED_REALM_FILE_NAME = "DEFAULT_FILE_NAME"
    private const val PREF = "INIT"
    private const val KEY_INITIALIZED = "INITIALIZED"
    private const val KEY_DEVICE_ROLE = "DEVICE_ROLE"


    fun setInitialized(context: Context, initialized: Boolean = true) {
        val sharedPref = getPref(context)
        with (sharedPref.edit()) {
            putBoolean(KEY_INITIALIZED, initialized)
            apply()
        }
    }

    fun isInitialized(context: Context): Boolean {
        val sharedPref = getPref(context)
        return sharedPref.getBoolean(KEY_INITIALIZED, false)
    }

    fun resetDeviceRole(context: Context) {
        setDeviceRole(context, "")
    }

    fun setDeviceRole(context: Context, realmFileName: String) {
        val sharedPref = getPref(context)
        with (sharedPref.edit()) {
            putString(KEY_DEVICE_ROLE, realmFileName)
            apply()
        }
    }

    fun getRealmFileName(context: Context): String {
        val sharedPref = getPref(context)
        val realmFileName = sharedPref.getString(KEY_DEVICE_ROLE, UNDEFINED_REALM_FILE_NAME)!!
        if (UNDEFINED_REALM_FILE_NAME == realmFileName || realmFileName == "") {
            // throw RuntimeException("There should not be a file without device role")
            return UNDEFINED_REALM_FILE_NAME
        }
        return realmFileName
    }

    fun isOfflineDevice(context: Context): Boolean {
        val sharedPref = getPref(context)
        val realmFileName = sharedPref.getString(KEY_DEVICE_ROLE, "")!!
        return realmFileName.contains(RealmHelper.OFFLINE_FILE_NAME)
    }

    fun isOnlineDevice(context: Context): Boolean {
        val sharedPref = getPref(context)
        val realmFileName = sharedPref.getString(KEY_DEVICE_ROLE, "")!!
        return realmFileName.contains(RealmHelper.ONLINE_FILE_NAME)
    }

    private fun getPref(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE) ?: throw RuntimeException("Cannot get Preferences..")
    }

}