package de.hartz.software.parannoying.online.interfaces

import de.hartz.software.parannoying.air.gap.interfaces.di.ExchangeApp

interface OnlineApplication: ExchangeApp {
    var onlineComponents: OnlineComponents
}