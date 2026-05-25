package de.hartz.software.parannoying.ggwave.interfaces

interface ReceivedMessageCallback {

    fun receivedMessage(message: String)

    fun process(array: ByteArray)


}