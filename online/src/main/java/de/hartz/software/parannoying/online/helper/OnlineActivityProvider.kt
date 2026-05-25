package de.hartz.software.parannoying.online.helper

import de.hartz.software.parannoying.core.interfaces.di.ActivityProvider
import de.hartz.software.parannoying.online.activities.online.OnlineMainActivity
import de.hartz.software.parannoying.online.activities.online.OnlineSettingsHiddenActivity
import de.hartz.software.parannoying.online.activities.online.WelcomeOnlineActivity

class OnlineActivityProvider(fakeEntryActivity: Class<*>): ActivityProvider(fakeEntryActivity) {
    override fun getStartActivityClass(): Class<*> {
        return OnlineMainActivity::class.java
    }

    override fun getWelcomeActivityClass(): Class<*> {
        return WelcomeOnlineActivity::class.java
    }

    override fun getHiddenSettingsActivityClass(): Class<*> {
        return OnlineSettingsHiddenActivity::class.java
    }
}