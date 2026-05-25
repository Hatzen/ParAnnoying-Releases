package de.hartz.software.parannoying.air.gap.model

import de.hartz.software.parannoying.core.interfaces.di.air.gap.ActivityPurpose
import de.hartz.software.parannoying.core.interfaces.di.air.gap.DeviceTarget
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ExchangeDataWrapper
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ISendLaunchOptions

data class SendLaunchOptions(
        override val requestCode: Int = -1,
        override var singleData: ExchangeDataWrapper? = null,
        override var multipleData: List<ExchangeDataWrapper>? = null,
        override val text: String = "",
        override val purpose: ActivityPurpose = ActivityPurpose.ANY_DATA,
        override val target: DeviceTarget = DeviceTarget.ANY,
        override val additionalEncryption: Boolean = false,
        override val confirmAndCancle: Boolean = false,
        override val token: String? = null,
): ISendLaunchOptions {

    override val data: List<ExchangeDataWrapper> get() {
        val list = multipleData
        if (list != null) {
            return list
        }
        return listOf(singleData!!)
    }

    override fun useText(data: String): SendLaunchOptions {
        assertNotInitialized()
        singleData = ExchangeDataWrapperImpl(data)
        return this
    }

    override fun useFile(data: String): SendLaunchOptions {
        assertNotInitialized()
        singleData = ExchangeDataWrapperImpl(data, true)
        return this
    }

    override fun useDataWrappers(data: List<ExchangeDataWrapper>): SendLaunchOptions {
        assertNotInitialized()
        multipleData = data
        return this
    }

    override fun useData(data: List<String>): SendLaunchOptions {
        assertNotInitialized()
        multipleData = data.map { ExchangeDataWrapperImpl(it) }
        return this
    }

    private fun assertNotInitialized() {
        // TODO: Remove. With static references of UseCases we will override it always..
        // assert(singleData == null && multipleData == null)
    }
}
