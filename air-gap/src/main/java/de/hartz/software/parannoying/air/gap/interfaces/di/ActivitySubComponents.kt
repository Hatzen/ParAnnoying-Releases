package de.hartz.software.parannoying.air.gap.interfaces.di

import android.app.Activity
import dagger.BindsInstance
import dagger.Subcomponent
import de.hartz.software.parannoying.air.gap.activities.ReceiveActivity
import de.hartz.software.parannoying.air.gap.activities.SendActivity
import de.hartz.software.parannoying.air.gap.activities.SharedDataRedirectActivity
import de.hartz.software.parannoying.air.gap.activities.dummy.DummyAirGapActivity
import de.hartz.software.parannoying.air.gap.fragments.exchange.DataReceiveFragment
import de.hartz.software.parannoying.air.gap.fragments.exchange.DataSendFragment

@Subcomponent()
interface ActivitySubComponents {

    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance activity: Activity): ActivitySubComponents
    }

    fun inject(fragment: DataReceiveFragment)
    fun inject(fragment: DataSendFragment)

    fun inject(sendActivity: SendActivity)

    fun inject(receiveActivity: ReceiveActivity)

    fun inject(receiveActivity: DummyAirGapActivity)

    fun inject(receiveActivity: SharedDataRedirectActivity)


}