package de.hartz.software.parannoying.air.gap.interfaces.exchange

/**
 * Pull data for SendFragments
 */
interface SendFragmentDataProvider {

    fun getNextChunkOfDataToSend(): String
    fun hasAnotherChunk(): Boolean

}