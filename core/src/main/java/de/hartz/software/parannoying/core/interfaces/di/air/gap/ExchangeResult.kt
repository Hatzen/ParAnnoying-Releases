package de.hartz.software.parannoying.core.interfaces.di.air.gap

interface ExchangeResult {

    val requestCode: Int
    val result: Sequence<ExchangeDataWrapper>?
    val success: Boolean

    fun getSingleResult(): ExchangeDataWrapper
    fun matchesUseCase(options: ILaunchOptions): Boolean
    fun getUseCase(): ILaunchOptions?
}
