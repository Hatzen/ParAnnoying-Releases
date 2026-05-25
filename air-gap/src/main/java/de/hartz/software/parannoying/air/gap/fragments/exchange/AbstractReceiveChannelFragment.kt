package de.hartz.software.parannoying.air.gap.fragments.exchange

import de.hartz.software.parannoying.air.gap.R
import de.hartz.software.parannoying.air.gap.helpers.ConfirmationHelper


abstract class AbstractReceiveChannelFragment: AbstractExchangeChannelFragment() {

    val fragmentResultListener: DataReceiveFragment by lazy {
        requireActivity().supportFragmentManager
                .findFragmentById(R.id.fragment_container) as DataReceiveFragment
    }

    fun fragmentReceivedSomeData (data: String) {
        fragmentResultListener.passResult(data, this)
        if (useConfirmation()) {
            ConfirmationHelper(securityInterfaceHolder).sendConfirmation(requireContext(), data)
        }
    }

}