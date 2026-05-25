package de.hartz.software.parannoying.offline.interfaces

import dagger.Subcomponent
import de.hartz.software.parannoying.offline.activities.offline.ChatActivity
import de.hartz.software.parannoying.offline.activities.offline.EventOverviewActivity
import de.hartz.software.parannoying.offline.activities.offline.ForwardOverviewActivity
import de.hartz.software.parannoying.offline.activities.offline.OfflineMainActivity
import de.hartz.software.parannoying.offline.activities.offline.settings.ExportActivity
import de.hartz.software.parannoying.offline.activities.offline.settings.OfflineSettingsActivity
import de.hartz.software.parannoying.offline.activities.offline.userinfo.UserInfoActivity
import de.hartz.software.parannoying.offline.fragments.ChatOverviewFragment
import de.hartz.software.parannoying.offline.fragments.SendDataOverviewFragment
import de.hartz.software.parannoying.offline.fragments.welcome.WelcomeImportBackupFragment

@Subcomponent()
interface OfflineComponents {

    // Factory that is used to create instances of this subcomponent
    @Subcomponent.Factory
    interface Factory {
        fun create(): OfflineComponents
    }

    fun inject(fragment: OfflineSettingsActivity)

    fun inject(fragment: ChatActivity)
    fun inject(fragment: UserInfoActivity)
    fun inject(fragment: ChatOverviewFragment)
    fun inject(fragment: EventOverviewActivity)
    fun inject(fragment: OfflineMainActivity)
    fun inject(fragment: ForwardOverviewActivity)
    fun inject(fragment: SendDataOverviewFragment)

    fun inject(fragment: WelcomeImportBackupFragment)
    fun inject(fragment: ExportActivity)




}