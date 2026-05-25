package de.hartz.software.parannoying.app.interfaces

import dagger.Component
import de.hartz.software.parannoying.app.App
import de.hartz.software.parannoying.app.helper.provider.AppModules
import de.hartz.software.parannoying.app.helper.provider.SecurityModule
import de.hartz.software.parannoying.core.interfaces.di.CoreComponents
import de.hartz.software.parannoying.offline.interfaces.OfflineComponents
import de.hartz.software.parannoying.online.interfaces.OnlineComponents
import javax.inject.Singleton

// SingletonScope cannot be combined with activity scope.
// https://medium.com/@harivigneshjayapalan/dagger-2-for-android-beginners-advanced-part-ii-eb6f8d8a8926
// https://proandroiddev.com/dagger-2-part-ii-custom-scopes-component-dependencies-subcomponents-697c1fa1cfc
// https://stackoverflow.com/questions/40910932/dagger-2-component-depends-on-more-than-one-scoped-component
// https://stackoverflow.com/questions/32211930/dagger-accessing-singleton-object-from-other-class
// https://stackoverflow.com/questions/24668957/module-depending-on-another-module-in-dagger
// https://stackoverflow.com/a/52335146/8524651
@Singleton
@Component(modules = [AppModules::class, SecurityModule::class])
interface ApplicationComponent {

    fun inject(app: App)

    fun coreComponents(): CoreComponents.Factory

    fun offlineComponent(): OfflineComponents.Factory

    fun onlineComponents(): OnlineComponents.Factory
}
