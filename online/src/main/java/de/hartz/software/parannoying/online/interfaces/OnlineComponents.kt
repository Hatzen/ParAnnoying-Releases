package de.hartz.software.parannoying.online.interfaces

import dagger.Subcomponent
import de.hartz.software.parannoying.online.activities.online.OnlineMainActivity
import de.hartz.software.parannoying.online.activities.online.OnlineSettingsActivity
import de.hartz.software.parannoying.online.activities.online.ServerConfigActivity
import de.hartz.software.parannoying.online.activities.online.WelcomeOnlineActivity
import de.hartz.software.parannoying.online.fragments.LogFragment
import de.hartz.software.parannoying.online.fragments.message.overview.InboxFragment
import de.hartz.software.parannoying.online.fragments.message.overview.OutboxFragment
import de.hartz.software.parannoying.online.fragments.welcome.GenerateOnlineIdFragment
import de.hartz.software.parannoying.online.fragments.welcome.WelcomeOnlineImportBackupFragment

@Subcomponent()
interface OnlineComponents{

    @Subcomponent.Factory
    interface Factory {
        fun create(): OnlineComponents
    }

    fun inject(fragment: OnlineSettingsActivity)

    fun inject(fragment: OnlineMainActivity)

    fun inject(fragment: WelcomeOnlineImportBackupFragment)

    fun inject(fragment: GenerateOnlineIdFragment)

    fun inject(fragment: InboxFragment)

    fun inject(fragment: LogFragment)

    fun inject(fragment: OutboxFragment)

    fun inject(welcomeOnlineActivity: WelcomeOnlineActivity)

    fun inject(serverConfigActivity: ServerConfigActivity)

    // TODO: Does not work.
    // fun inject(service: NotificationReceiver)



}