package de.hartz.software.parannoying.core.interfaces.di

import dagger.Subcomponent
import de.hartz.software.parannoying.core.activities.BaseActivity
import de.hartz.software.parannoying.core.activities.CrashLogOverviewActivity
import de.hartz.software.parannoying.core.activities.insecured.SplashActivity
import de.hartz.software.parannoying.core.activities.insecured.StartupRedirectActivity
import de.hartz.software.parannoying.core.activities.insecured.welcome.WelcomeActivity
import de.hartz.software.parannoying.core.activities.insecured.wiki.SideFactsActivity
import de.hartz.software.parannoying.core.fragments.FileCheckStatusFragment
import de.hartz.software.parannoying.core.helper.crash.CrashReportFactory

@Subcomponent()
interface CoreComponents{

    @Subcomponent.Factory
    interface Factory {
        fun create(): CoreComponents
    }

    @Deprecated("needs specific actvitiy https://stackoverflow.com/a/58646250/8524651")
    fun inject(baseActivity: BaseActivity)

    fun inject(splashActivity: SplashActivity)

    fun inject(redirectActivity: StartupRedirectActivity)

    fun inject(redirectActivity: SideFactsActivity)

    fun inject(crashReportFactory: CrashReportFactory)

    fun inject(welcomeActivity: WelcomeActivity)

    fun inject(welcomeActivity: CrashLogOverviewActivity)

    fun inject(instance: FileCheckStatusFragment)

}