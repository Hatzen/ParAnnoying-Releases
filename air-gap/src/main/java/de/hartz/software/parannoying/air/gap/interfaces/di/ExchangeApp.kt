package de.hartz.software.parannoying.air.gap.interfaces.di

import android.app.Activity
import de.hartz.software.parannoying.core.interfaces.AbstractApp

interface ExchangeApp: AbstractApp {

    var currentActivity: Activity?

    fun getActivityComponents(activity: Activity): ActivitySubComponents

}