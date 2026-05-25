package de.hartz.software.parannoying.offline.interfaces

import de.hartz.software.parannoying.air.gap.interfaces.di.ExchangeApp

interface OfflineApplication: ExchangeApp {
    var offlineComponents: OfflineComponents
}