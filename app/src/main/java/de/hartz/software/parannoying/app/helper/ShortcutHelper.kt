package de.hartz.software.parannoying.app.helper

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutManager
import android.os.Build
import android.util.Log
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import de.hartz.software.parannoying.core.R
import de.hartz.software.parannoying.core.activities.insecured.StartupRedirectActivity
import de.hartz.software.parannoying.core.model.domain.DeviceRole
import de.hartz.software.parannoying.offline.activities.offline.settings.OfflineSettingsActivity
import de.hartz.software.parannoying.online.activities.online.OnlineSettingsActivity

object ShortcutHelper {
    const val SHORTCUT_ID_SYNC = "shortcut_sync"
    const val SHORTCUT_ID_OFFLINE_USER_ID = "shortcut_offline_user_id"
    const val SHORTCUT_ID_OFFLINE_SETTINGS = "shortcut_offline_settings"
    const val SHORTCUT_ID_ONLINE_SETTINGS = "shortcut_online_settings"
    const val EXTRA_SHORTCUT_ID = "shortcut"

    const val EXTRA_VALUE_SYNC = "sync"
    const val EXTRA_VALUE_USERID = "userid"

    fun init(deviceRole: DeviceRole?, context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)

            // TODO remove?
            // try {
            //     shortcutManager.disableShortcuts(listOf(SHORTCUT_ID_SYNC, SHORTCUT_ID_OFFLINE_USER_ID))
            // } catch (e: Exception) {
            //     Log.e(javaClass.simpleName, "" + e.message)
            // }
            if (DeviceRole.ONLINE == deviceRole?.roleId) {
                addSyncShortcut(context)
                addOnlineSettings(context)
            }
            if (DeviceRole.OFFLINE == deviceRole?.roleId) {
                addSyncShortcut(context)
                addUserIdShortcut(context)
                addOfflineSettings(context)
            }
            val dynamicShortcuts = shortcutManager.dynamicShortcuts
            val pinnedShortcuts = shortcutManager.pinnedShortcuts
            val manifestShortcuts = shortcutManager.manifestShortcuts

            Log.d("Shortcuts", "Dynamic: $dynamicShortcuts")
            Log.d("Shortcuts", "Pinned: $pinnedShortcuts")
            Log.d("Shortcuts", "Static: $manifestShortcuts")

            val id = context.resources.getIdentifier("shortcuts", "xml", context.packageName)
            Log.d("Shortcuts", "Shortcut XML resource ID: $id")
        }
    }

    private fun addSyncShortcut(context: Context) {
        val shortcut = ShortcutInfoCompat.Builder(context, SHORTCUT_ID_SYNC)
            .setShortLabel(context.resources.getString(R.string.short_shortcut_sync))
            .setLongLabel(context.resources.getString(R.string.long_shortcut_sync))
            .setIcon(IconCompat.createWithResource(context, R.mipmap.sync))
            .setIntent(
                Intent(context, StartupRedirectActivity::class.java)
                    .setAction(StartupRedirectActivity.ACTION_SYNC)
            )
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
    }

    private fun addUserIdShortcut(context: Context) {
        val shortcut = ShortcutInfoCompat.Builder(context, SHORTCUT_ID_OFFLINE_USER_ID)
            .setShortLabel(context.resources.getString(R.string.short_shortcut_userid))
            .setLongLabel(context.resources.getString(R.string.long_shortcut_userid))
            .setIcon(IconCompat.createWithResource(context, R.mipmap.userid))
            .setIntent(
                Intent(context, StartupRedirectActivity::class.java)
                    .setAction(StartupRedirectActivity.ACTION_USERID)
            )
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
    }

    private fun addOnlineSettings(context: Context) {
        val shortcut = ShortcutInfoCompat.Builder(context, SHORTCUT_ID_ONLINE_SETTINGS)
            .setShortLabel("Online Settings")
            .setLongLabel("Online settings for channels etc")
            .setIcon(IconCompat.createWithResource(context, R.mipmap.gear))
            .setIntent(
                Intent(context, OnlineSettingsActivity::class.java)
                    .setAction(Intent.ACTION_VIEW)
            )
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
    }

    private fun addOfflineSettings(context: Context) {
        val shortcut = ShortcutInfoCompat.Builder(context, SHORTCUT_ID_OFFLINE_SETTINGS)
            .setShortLabel("Offline Settings")
            .setLongLabel("Offline settings for channels etc")
            .setIcon(IconCompat.createWithResource(context, R.mipmap.gear))
            .setIntent(
                Intent(context, OfflineSettingsActivity::class.java)
                    .setAction(Intent.ACTION_VIEW)
            )
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
    }
}