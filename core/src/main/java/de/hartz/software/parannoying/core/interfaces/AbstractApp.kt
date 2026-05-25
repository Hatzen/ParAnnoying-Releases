package de.hartz.software.parannoying.core.interfaces

import de.hartz.software.parannoying.core.interfaces.di.CoreComponents
import de.hartz.software.parannoying.core.interfaces.di.StorageInterface
import de.hartz.software.parannoying.core.interfaces.di.air.gap.AirGapAdapter


interface AbstractApp {
    var Storage: StorageInterface<*, *>
    var airGapAdapter: AirGapAdapter

    var coreComponents: CoreComponents

    fun initApp()

    fun invalidateComponents()

    fun isDebugMode(): Boolean
}