package de.hartz.software.parannoying.air.gap.interfaces.exchange

import de.hartz.software.parannoying.air.gap.fragments.exchange.AirGapFragment

interface ExchangeFragmentListener {
    fun setExchangeStatus(status: AirGapFragment.StatusColor)

    fun readyForTransmitting()
}