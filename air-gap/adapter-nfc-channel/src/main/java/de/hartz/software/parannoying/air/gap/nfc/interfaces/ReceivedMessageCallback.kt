package de.hartz.software.parannoying.air.gap.nfc.interfaces

interface ReceivedMessageCallback {

    fun receiveMessage(message: String)

    /**
     * Called when no further data is send.
     */
    fun disconnect()

}