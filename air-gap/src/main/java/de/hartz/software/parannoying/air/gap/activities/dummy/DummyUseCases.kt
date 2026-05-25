package de.hartz.software.parannoying.air.gap.activities.dummy

import de.hartz.software.parannoying.air.gap.activities.dummy.DummyAirGapActivity.Companion.REQUEST_CODE_MULTIPLE
import de.hartz.software.parannoying.air.gap.activities.dummy.DummyAirGapActivity.Companion.REQUEST_CODE_SINGLE
import de.hartz.software.parannoying.air.gap.model.SendLaunchOptions
import de.hartz.software.parannoying.air.gap.model.UseCases
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ActivityPurpose
import de.hartz.software.parannoying.core.interfaces.di.air.gap.DeviceTarget
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ReceiveLaunchOptions

object DummyUseCases {
    val FAKE_ONLINE_ID_TOKEN = "FAKE_ONLINE_ID_TOKEN"

    val SINGLE_SEND = SendLaunchOptions(
            requestCode = DummyAirGapActivity.REQUEST_CODE_SINGLE,
            purpose = ActivityPurpose.MESSAGE,
            target = DeviceTarget.OFFLINE,
    )

    val MULTIPLE_SEND = SendLaunchOptions(
            requestCode = DummyAirGapActivity.REQUEST_CODE_MULTIPLE,
            purpose = ActivityPurpose.CRASH,
            target = DeviceTarget.ONLINE
    )

    val FILE_SEND = SendLaunchOptions(
            requestCode = DummyAirGapActivity.REQUEST_CODE_FILE,
            purpose = ActivityPurpose.USERID,
            target = DeviceTarget.OFFLINE
    )

    val ENCRYPTED_SEND = SendLaunchOptions(
            requestCode = DummyAirGapActivity.ENCRYPTED_CODE_SINGLE,
            purpose = ActivityPurpose.ONLINEID,
            target = DeviceTarget.ONLINE,
            confirmAndCancle = true,
            additionalEncryption = true,
            token = FAKE_ONLINE_ID_TOKEN
    )

    val SEND_SYNC = UseCases.Offline.MESSAGES_SYNC

    val SEND_NOTIFICATION = UseCases.Offline.CRASH_REPORT_SEND

    // RECEIVE

    val RECEIVE = ReceiveLaunchOptions(
        purpose = ActivityPurpose.MESSAGE,
        source = DeviceTarget.ONLINE,
        requestCode = REQUEST_CODE_SINGLE,
    )

    val RECEIVE_ENCRYPTED = ReceiveLaunchOptions(
        requestCode = REQUEST_CODE_MULTIPLE,
        purpose = ActivityPurpose.USERID,
        source = DeviceTarget.OFFLINE,
        additionalDecryption = true,
        token = FAKE_ONLINE_ID_TOKEN
    )

    val RECEIVE_SYNC = UseCases.Online.MESSAGES_SYNC
}