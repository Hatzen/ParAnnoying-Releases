package de.hartz.software.parannoying.offline.model.domain

import android.content.Context
import android.os.Build
import android.util.Log
import com.scottyab.rootbeer.RootBeer
import de.hartz.software.parannoying.core.helper.development.DevelopmentUtil
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.model.domain.settings.Channels
import de.hartz.software.parannoying.offline.helper.guard.usb.UsbConnectivityReceiver
import de.hartz.software.parannoying.offline.model.OfflineStorage
import de.hartz.software.parannoying.offline.model.domain.settings.MessageSecurity
import de.hartz.software.parannoying.offline.model.domain.settings.OfflineSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

class UserSecurity(val key: Char, val description: String, val errorLevel: Int) {
    companion object {
        const val FINE = 0
        const val WARNING = 5
        const val ERROR = 10
        const val FATAL = 1000

        val INTERNET = UserSecurity('A', "Internet connection is enabled", FATAL)
        val BLUETOOTH = UserSecurity('B', "Bluetooth connection is enabled", WARNING)
        val DEVELOPER = UserSecurity('C', "Developer mode is enabled", ERROR)
        val SCREENSHOTS = UserSecurity('D', "Screenshots are allowed", WARNING)
        val ROOT = UserSecurity('E', "Device is rooted", ERROR)
        val GOOGLE_API = UserSecurity('F', "Using google api, less anonymous", WARNING)
        val SECURED_DEVICE = UserSecurity('G', "Uses pin code or hidden messages", FINE)
        val AIRPLANE_MODE = UserSecurity('H', "Airplane mode is used for cutting internet", WARNING)
        val USB_CONNECTION = UserSecurity('I', "Usb connection detected", FATAL)
        val OLD_ANDROID = UserSecurity('J', "Old android version detected. Important security updates missing.", WARNING)
        val UNKNOWN = UserSecurity('K', "Unknown Security Issue detected. You probably have an older version.", ERROR)
        val OFFLINE_GROUP = UserSecurity('L', "Offline Groups are insecure.", FATAL)
        val ONLINE_GROUP = UserSecurity('M', "Online Groups are as safe as single chats.", FINE)
        val UNKNOWN_USER = UserSecurity('N', "User is not known.", FATAL)
        // TODO: val OUTDATED_KEYS = UserSecurity('O', "Keys are older than a year.", FATAL) => Store the Month of created keys (scanned or created?) and if it is passed one time set security issue.


        fun getByString(keys: String):  List<UserSecurity> {
            return keys.toCharArray().map { getByKey(it) }
        }

        fun getByKey(key: Char): UserSecurity {
            return listOf(
                    INTERNET,
                    BLUETOOTH,
                    DEVELOPER,
                    SCREENSHOTS,
                    ROOT,
                    GOOGLE_API,
                    SECURED_DEVICE,
                    AIRPLANE_MODE,
                    USB_CONNECTION,
                    OLD_ANDROID,
                    OFFLINE_GROUP,
                    ONLINE_GROUP,
                    UNKNOWN_USER
            ).find {
                it.key == key
            } ?: UNKNOWN
        }

        fun getAllByContextAsString(context: Context, settings: OfflineSettings): String {
            return getAllByContext(context, settings).map { it.key }.joinToString()
        }

        fun getAllByContext(context: Context, settings: OfflineSettings): List<UserSecurity> {
            if (DevelopmentUtil.isDebugMode()) {
                return listOf()
            }

            val insecurities = mutableListOf<UserSecurity>()
            if (settings.hiddenSettings.developerMode) {
                insecurities.add(DEVELOPER)
            } else if (settings.hiddenSettings.allowInternet) {
                insecurities.add(INTERNET)
            }
            if (settings.hiddenSettings.allowScreenshots) {
                insecurities.add(SCREENSHOTS)
            }
            if (RootBeer(context).isRooted) {
                insecurities.add(ROOT)
            }
            if (settings.allowedChannels.contains(Channels.BLUETOOTH)) {
                insecurities.add(BLUETOOTH)
            }
            if (OfflineStorage.INSTANCE.onlineUserEmail != null) {
                insecurities.add(GOOGLE_API)
            }
            if (settings.screenSaver) {
                insecurities.add(SECURED_DEVICE)
            }
            if (settings.messageSecurity == MessageSecurity.HIDE || settings.messageSecurity == MessageSecurity.DELETE) {
                insecurities.add(SECURED_DEVICE)
            }
            if (IOHelper.isAirplaneModeOn(context)) {
                insecurities.add(AIRPLANE_MODE)
            }
            if (UsbConnectivityReceiver.hasConnection(context)) {
                insecurities.add(USB_CONNECTION)
            }
            if (hasPassedCheapSecurityUpdateThreshold()) {
                insecurities.add(OLD_ANDROID)
            }
            return insecurities
        }

        private fun hasPassedCheapSecurityUpdateThreshold (): Boolean {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
                return true
            }
            try {
                // Detect if the distance of the first security update to the current one installed is bigger than from current to the current date.
                // TODO: This determination of security patchs security is very weak.. But we cannot detemine it generically because of no internet connection..
                // https://source.android.com/security/bulletin/2015-08-01
                val dateFormat = SimpleDateFormat("yyyy-MM-dd")
                val firstBulletin = dateFormat.parse("2015-08-01")
                val currentBulletin = dateFormat.parse(Build.VERSION.SECURITY_PATCH)
                val now = Date()

                val diffInMillies: Long = Math.abs(now.getTime() - currentBulletin.getTime())
                val diffCurrentUpdateToNow: Long = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS)

                val diffInMillies2: Long = Math.abs(firstBulletin.getTime() - currentBulletin.getTime())
                val diffCurrentUpdateToFirst: Long = TimeUnit.DAYS.convert(diffInMillies2, TimeUnit.MILLISECONDS)
                if (diffCurrentUpdateToFirst / diffCurrentUpdateToNow > 0.5) {
                    return true
                }
            } catch (e: Exception) {
                // Older android versions dont have Build.VERSION.SECURITY_PATCH or any patches..
                Log.d(javaClass.simpleName, "Error detecting security updates", e)
                return true
            }
            return false
        }
    }


    override fun hashCode(): Int {
        return key.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserSecurity

        if (key != other.key) return false

        return true
    }
}