package de.hartz.software.parannoying.air.gap.model

import de.hartz.software.parannoying.core.interfaces.di.air.gap.ExchangeDataWrapper
import java.io.Serializable

data class ExchangeDataWrapperImpl(
        override val exchangeData: String,
        override val isFile: Boolean = false) : ExchangeDataWrapper, Serializable