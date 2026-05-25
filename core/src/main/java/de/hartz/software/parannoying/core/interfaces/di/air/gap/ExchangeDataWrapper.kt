package de.hartz.software.parannoying.core.interfaces.di.air.gap

interface ExchangeDataWrapper {

    val exchangeData: String
    val isFile: Boolean
    val filePath: String
        get() = run {
            if (isFile) {
                return exchangeData
            }
            throw RuntimeException()
        }
}
