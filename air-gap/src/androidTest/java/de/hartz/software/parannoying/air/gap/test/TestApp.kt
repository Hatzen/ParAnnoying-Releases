package de.hartz.software.parannoying.air.gap.test

import android.app.Activity
import android.app.Application
import de.hartz.software.parannoying.air.gap.interfaces.di.ActivitySubComponents
import de.hartz.software.parannoying.air.gap.interfaces.di.ExchangeApp
import de.hartz.software.parannoying.core.interfaces.di.CoreComponents
import de.hartz.software.parannoying.core.interfaces.di.StorageInterface
import de.hartz.software.parannoying.core.interfaces.di.air.gap.AirGapAdapter
import io.mockk.mockk

class TestApp: Application(), ExchangeApp {

    override var currentActivity: Activity?
        get() = TODO("Not yet implemented")
        set(value) {}

    override var Storage: StorageInterface<*, *>
        get() = TODO("Not yet implemented")
        set(value) {}
    override var airGapAdapter: AirGapAdapter
        get() = TODO("Not yet implemented")
        set(value) {}
    override var coreComponents: CoreComponents
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun initApp() {
        TODO("Not yet implemented")
    }

    override fun invalidateComponents() {
    }
    override fun getActivityComponents(activity: Activity): ActivitySubComponents {
        return mockk()
    }

    override fun isDebugMode(): Boolean {
        return true
    }
}