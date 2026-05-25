package de.hartz.software.parannoying.offline.helper

import de.hartz.software.parannoying.core.interfaces.di.ActivityProvider
import de.hartz.software.parannoying.offline.activities.offline.OfflineMainActivity
import de.hartz.software.parannoying.offline.activities.offline.WelcomeOfflineActivity
import de.hartz.software.parannoying.offline.activities.offline.settings.OfflineSettingsHiddenActivity

class OfflineActivityProvider(fakeEntryActivity: Class<*>): ActivityProvider(fakeEntryActivity) {

    override fun getStartActivityClass(): Class<*> {
        return OfflineMainActivity::class.java
    }

    override fun getWelcomeActivityClass(): Class<*> {
        return WelcomeOfflineActivity::class.java
    }

    override fun getHiddenSettingsActivityClass(): Class<*> {
        return OfflineSettingsHiddenActivity::class.java
    }
}