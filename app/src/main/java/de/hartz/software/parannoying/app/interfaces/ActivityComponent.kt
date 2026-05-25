package de.hartz.software.parannoying.app.interfaces

import dagger.Component
import de.hartz.software.parannoying.air.gap.interfaces.di.ActivitySubComponents
import de.hartz.software.parannoying.app.helper.provider.ActivityModules
import javax.inject.Singleton

@Singleton
@Component(modules = [ActivityModules::class])
interface ActivityComponent {

    // TODO: remove this wont work..
    // fun inject(activity: Activity)
    // fun inject(activity: AppCompatActivity)

    fun activityComponents(): ActivitySubComponents.Factory

}