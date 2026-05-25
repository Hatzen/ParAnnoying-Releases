package de.hartz.software.parannoying.air.gap.interfaces.exchange

import de.hartz.software.parannoying.air.gap.fragments.exchange.AbstractExchangeChannelFragment

/**
 * Listener to receive data from a receive channel fragment.
 */
interface ReceiveFragmentResultListener {
    fun passResult(result: String, caller: AbstractExchangeChannelFragment)

}