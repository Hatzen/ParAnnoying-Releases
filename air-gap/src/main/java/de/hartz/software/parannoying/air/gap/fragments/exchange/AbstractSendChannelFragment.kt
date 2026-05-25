package de.hartz.software.parannoying.air.gap.fragments.exchange

import de.hartz.software.parannoying.air.gap.R

abstract class AbstractSendChannelFragment: AbstractExchangeChannelFragment() {

    val fragmentResultListener: DataSendFragment by lazy {
        requireActivity().supportFragmentManager
                .findFragmentById(R.id.fragment_container) as DataSendFragment
    }

    val currentData get() = fragmentResultListener.currentElement!!

    open val maxDataSize = DataSendFragment.ONE_CHUNK_AS_MAX

    open fun startTransferDataSet(newData: String) {

    }

}