package de.hartz.software.parannoying.core.interfaces.di

abstract class ActivityProvider(private val fakeEntryActivity: Class<*>) {
    fun getFakeEntryActivityClass(): Class<*> {
        return fakeEntryActivity
    }

    abstract fun getStartActivityClass(): Class<*>

    abstract fun getWelcomeActivityClass(): Class<*>

    abstract fun getHiddenSettingsActivityClass(): Class<*>
}