package de.hartz.software.parannoying.offline.helper.guard

import android.content.Context
import de.hartz.software.parannoying.core.helper.development.DevelopmentUtil
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.core.interfaces.di.StorageInterface
import de.hartz.software.parannoying.offline.helper.guard.usb.UsbConnectivityReceiver
import de.hartz.software.parannoying.offline.model.OfflineStorage
import de.hartz.software.parannoying.offline.model.domain.events.BaseEvent
import de.hartz.software.parannoying.offline.model.domain.events.SimpleEvent

object ConnectionGuard {

    fun decideToKillApp (context: Context) {
        // Avoid realm issues with switching file to often.
        if (DevelopmentUtil.isRunningTest()) {
            return
        }
        shouldKillApp(grantAccess(context), context)
    }

    fun isConnected(context: Context): Boolean {
        if (UsbConnectivityReceiver.hasConnection(context)) {
            OfflineStorage.INSTANCE.persistEvent(SimpleEvent(BaseEvent.EVENT_USB_CONNECTION))
            return true
        }
        if (IOHelper.hasInternetConnection(context)) {
            OfflineStorage.INSTANCE.persistEvent(SimpleEvent(BaseEvent.EVENT_INTERNET_CONNECTION))
            return true
        }
        if (CellularConnectionDetector(context).hasConnection()) {
            OfflineStorage.INSTANCE.persistEvent(SimpleEvent(BaseEvent.EVENT_PHONE_CONNECTION))
            return true
        }
        return false
    }

    private fun shouldKillApp(isConnected: Boolean, context: Context) {
        if (isConnected) {
            StorageInterface.deleteAll(context)
            UiHelper.showToastFromBackgroundTask(context, "Paranoying: Data got Deleted. Caused by Internet connection")
        }
        if (OfflineStorage.INSTANCE.DEVELOPER_MODE) {
            if (isConnected(context)) {
                UiHelper.showToastFromBackgroundTask(context, "Paranoying: Connection detected but nothing done.")
            } else {
                UiHelper.showToastFromBackgroundTask(context, "Paranoying: Connection got lost")
            }
        }
    }

    private fun needsProtection (): Boolean {
        return OfflineStorage.INSTANCE.isOfflineDevice() && !OfflineStorage.INSTANCE.readSettings().hiddenSettings.allowInternet
    }
    private fun grantAccess(context: Context): Boolean {
        return isConnected(context) && needsProtection()
    }

}