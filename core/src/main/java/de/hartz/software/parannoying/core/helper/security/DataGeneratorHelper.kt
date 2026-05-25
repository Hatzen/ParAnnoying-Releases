package de.hartz.software.parannoying.core.helper.security

import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.interfaces.di.StorageInterface
import de.hartz.software.parannoying.core.model.exceptions.InvalidOnlineIdException
import javax.inject.Inject


class DataGeneratorHelper @Inject constructor(val serviceHolder: SecurityInterfaceHolder) {


    // Every caller have to call activity.store() after this.
    fun storeFullOnlineUserDataForOfflineDevice(fullOnlineUserData: String, Storage: StorageInterface<*, *>) {
        val isOnlineId = fullOnlineUserData.contains(DataSecurityHelper.ONLINE_EMAIL_POSTFIX)
        val isOfflineId = fullOnlineUserData.contains(DataSecurityHelper.NOTIFICATION_ID_PREFIX_INVALID)
        if (!isOfflineId && !isOnlineId) {
            throw InvalidOnlineIdException()
        }
        if (isOfflineId) {
            Storage.onlineUserId = fullOnlineUserData
            return
        }
        Storage.onlineUserId = fullOnlineUserData.substringBefore(DataSecurityHelper.ONLINE_EMAIL_POSTFIX)
        Storage.onlineUserEmail = fullOnlineUserData.substring(0, fullOnlineUserData.indexOf(DataSecurityHelper.ONLINE_EMAIL_POSTFIX) + DataSecurityHelper.ONLINE_EMAIL_POSTFIX.length)
        Storage.onlineUserIdPassword = fullOnlineUserData.substringAfter(DataSecurityHelper.ONLINE_EMAIL_POSTFIX)
    }

    // TODO: Only needed in online device..
    fun createFakeOnlineId(): String {
        return DataSecurityHelper.NOTIFICATION_ID_PREFIX_INVALID + serviceHolder.randomHelper.getRandomUUIDv4()
    }

    fun cleanOnlineUserId(onlineUserEmail: String): String {
        return onlineUserEmail.substringBeforeLast(DataSecurityHelper.ONLINE_EMAIL_POSTFIX)
    }


}