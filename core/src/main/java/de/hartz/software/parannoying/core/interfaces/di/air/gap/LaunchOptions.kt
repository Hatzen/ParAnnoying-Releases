package de.hartz.software.parannoying.core.interfaces.di.air.gap

import java.io.Serializable


class SyncLaunchOptions(
        val sendLaunchOptions: ISendLaunchOptions,
        val receiveLaunchOptions: ReceiveLaunchOptions,
        val receiveThenSend: Boolean = false
) {

    fun useText(data: String): SyncLaunchOptions {
        sendLaunchOptions.useText(data)
        return this
    }

    fun useFile(data: String): SyncLaunchOptions {
        sendLaunchOptions.useFile(data)
        return this
    }

    fun useDataWrappers(data: List<ExchangeDataWrapper>): SyncLaunchOptions {
        sendLaunchOptions.useDataWrappers(data)
        return this
    }

    fun useData(data: List<String>): SyncLaunchOptions {
        sendLaunchOptions.useData(data)
        return this
    }
}


interface ILaunchOptions: Serializable {
    val requestCode: Int
    val token: String?
}

interface ISendLaunchOptions: ILaunchOptions {
    override val requestCode: Int
    var singleData: ExchangeDataWrapper?
    var multipleData: List<ExchangeDataWrapper>?
    val text: String
    val purpose: ActivityPurpose
    val target: DeviceTarget
    val additionalEncryption: Boolean
    val confirmAndCancle: Boolean

    val data: List<ExchangeDataWrapper>

    fun useText(data: String): ISendLaunchOptions
    fun useFile(data: String): ISendLaunchOptions

    fun useDataWrappers(data: List<ExchangeDataWrapper>): ISendLaunchOptions
    fun useData(data: List<String>): ISendLaunchOptions
}

data class ReceiveLaunchOptions(
        override val requestCode: Int,
        override val token: String? = null,
        val text: String = "",
        val purpose: ActivityPurpose = ActivityPurpose.ANY_DATA,
        val source: DeviceTarget = DeviceTarget.ANY,
        val additionalDecryption: Boolean = false,
): ILaunchOptions

enum class DeviceTarget {
    ONLINE,
    OFFLINE,
    ANY
}

enum class ActivityRole {
    SEND,
    RECEIVE
}

enum class ActivityPurpose {
    CRASH,
    ONLINEID,
    MESSAGE,
    USERID,
    SERVER_CONFIG,
    ANY_DATA // TODO: replace with event and forward??
}