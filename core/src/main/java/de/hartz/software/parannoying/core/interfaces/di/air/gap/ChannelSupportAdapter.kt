package de.hartz.software.parannoying.core.interfaces.di.air.gap

interface ChannelSupportAdapter {

    fun isNFCSupported(): Boolean
    fun isBluetoothSupported(): Boolean

    fun isCameraSupported(): Boolean

    fun isSoundSupported(): Boolean

    fun isSDSupported(): Boolean

    fun getSDDrives(): List<String>


}