package de.hartz.software.parannoying.air.gap.model

import de.hartz.software.parannoying.core.interfaces.di.air.gap.ActivityPurpose
import de.hartz.software.parannoying.core.interfaces.di.air.gap.DeviceTarget
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ReceiveLaunchOptions
import de.hartz.software.parannoying.core.interfaces.di.air.gap.SyncLaunchOptions

object UseCases {

    // Cleartextfiles => primarly used to send online to offline but as well export offline to any device
    val CLEARTEXT_FILE_SEND = SendLaunchOptions(
        purpose = ActivityPurpose.ANY_DATA,
        target = DeviceTarget.ANY,
        // TODO: encryption would be nice but additional impact on performance..
        // additionalEncryption = true,
        requestCode = 12_385,
        text = "Send cleartext file."
    )
    val CLEARTEXT_FILE_RECEIVE = ReceiveLaunchOptions(
        purpose = ActivityPurpose.ANY_DATA,
        source = DeviceTarget.ANY,
        // TODO: encryption would be nice but additional impact on performance..
        // additionalEncryption = true,
        requestCode = 12_386,
        text = "Receive cleartext file."
    )

    // ForwardData => Can be userId, Message, anything in cleartext
    // Token might not work
    // Yes or No for deletion
    // Multiple?
    // TODO: Currently only offline but online would be useful as well..
    val FORWARD_SEND_DATA = SendLaunchOptions(
        requestCode = 12_595,
        text = "Send data so another offline device can redirect it.",
        confirmAndCancle = true
    )
    val FORWARD_RECEIVE_DATA = ReceiveLaunchOptions(
            requestCode = 12_596,
            text = "Scan foreign data now."
    )


    object Offline {
        // Crashreport offline => wont be started from another actvity
        // YES NO for Ending activity
        // Token might work, but not when we cannot open storage..
        // Singledata
        val CRASH_REPORT_SEND = SendLaunchOptions(
                purpose = ActivityPurpose.CRASH,
                target = DeviceTarget.ONLINE,
                confirmAndCancle = true,
                text = "Scan this id with your online device to send the crash report",
        )

        // UserId => for foreign device, uses PinCode
            // Token does not work
            // Additional encryption
            // YES NO for confirming
            // Singledata
        val USERID_RECEIVE = ReceiveLaunchOptions(
                requestCode = 72,
                purpose = ActivityPurpose.USERID,
                source = DeviceTarget.OFFLINE,
        )
        val USERID_SEND = SendLaunchOptions(
                purpose = ActivityPurpose.USERID,
                target = DeviceTarget.OFFLINE,
                confirmAndCancle = true,
                additionalEncryption = true,
                requestCode = 1212
        )


        val OFFLINE_GROUP_SEND = SendLaunchOptions(
                purpose = ActivityPurpose.USERID,
                target = DeviceTarget.OFFLINE,
                additionalEncryption = true
        )

        // Send/Restore Online Id
        // Token wont work
        // Single
        // TODO: additionalEncryption might be good?
        val ONLINEID_SEND = SendLaunchOptions(
            purpose = ActivityPurpose.ONLINEID,
            target = DeviceTarget.ONLINE,
            text = "Scan this id with a new online device to replace the old one."
        )


        // SyncMessages Online to Offline and vice versa
            // YES NO in Send Activity to go further/ confirmation
            // Token works
            // Multiple

        val MESSAGES_SYNC = SyncLaunchOptions(
            SendLaunchOptions(
                purpose = ActivityPurpose.MESSAGE,
                target = DeviceTarget.ONLINE,
                requestCode = 1241
            ),
            ReceiveLaunchOptions(
                purpose = ActivityPurpose.MESSAGE,
                source = DeviceTarget.ONLINE,
                requestCode = 1242
            )
        )


        val MESSAGE_RECEIVE = ReceiveLaunchOptions(
                purpose = ActivityPurpose.MESSAGE,
                source = DeviceTarget.ONLINE,
                requestCode = 152
            )


        val MESSAGE_SEND = SendLaunchOptions(
                purpose = ActivityPurpose.MESSAGE,
                target = DeviceTarget.ONLINE,
                text = "Scan your message now.",
                confirmAndCancle = true
        )

        // Send message or just
        val MESSAGE_SEND_SUBSET = SendLaunchOptions(
                requestCode = 1241,
                purpose = ActivityPurpose.MESSAGE,
                target = DeviceTarget.ONLINE,
        )

        // Send event
            // Token wont work
            // Single

        val EVENT_SEND = SendLaunchOptions(
                target = DeviceTarget.OFFLINE,
                text = "Now scan this with your offline device to decrypt the message.",
                confirmAndCancle = true
        )
    }

    object Online {

        val MESSAGES_SYNC = SyncLaunchOptions(
            SendLaunchOptions(
                purpose = ActivityPurpose.MESSAGE,
                target = DeviceTarget.OFFLINE,
                text = "Scan your messages now.",
                requestCode = 12313
            ),
            ReceiveLaunchOptions(
                purpose = ActivityPurpose.MESSAGE,
                source = DeviceTarget.OFFLINE,
                text = "Scan your messages now.",
                requestCode = 12312
            ),
            receiveThenSend = true
        )


        val MESSAGE_RECEIVE = ReceiveLaunchOptions(
                purpose = ActivityPurpose.MESSAGE,
                source = DeviceTarget.OFFLINE,
                requestCode = 152
        )

        // TODO: This is wrong isnt it? Share is not related to airgap mechanism.. delete..
        val MESSAGE_SHARE_RECEIVE = ReceiveLaunchOptions(
                text = "encrypted message via share",
                purpose = ActivityPurpose.MESSAGE,
                source = DeviceTarget.OFFLINE,
                requestCode = 132
        )

        val MESSAGE_SEND = SendLaunchOptions(
                purpose = ActivityPurpose.MESSAGE,
                target = DeviceTarget.OFFLINE,
                text = "Now scan this with your offline device to decrypt the message.",
                confirmAndCancle = true,
                requestCode = 712
        )


        val SERVER_CONFIG_RECEIVE = ReceiveLaunchOptions(
            purpose = ActivityPurpose.SERVER_CONFIG,
            source = DeviceTarget.ONLINE,
            requestCode = 5995
        )

        val SERVER_CONFIG_SEND = SendLaunchOptions(
            purpose = ActivityPurpose.SERVER_CONFIG,
            target = DeviceTarget.ONLINE,
            requestCode = 5996
        )

        // Scan OnlineId => messageoverview to offlineactity
            // Token does not work
            // YES NO for confirming
            // Singledata

        val ONLINEID_SEND = SendLaunchOptions(
                purpose = ActivityPurpose.ONLINEID,
                target = DeviceTarget.OFFLINE,
                text =  "Scan this id with your offline device to receive its messages here"
        )

        // Scan OnlineId => Send Fragment and ReceiveActvitiy in Onboarding
            // Token does not work
            // YES NO for confirming
            // Singledata
        val ONLINEID_FIRST_SEND = SendLaunchOptions(
                purpose = ActivityPurpose.ONLINEID,
                target = DeviceTarget.OFFLINE,
                text =  "Scan this id with your offline device to receive its messages here",
                confirmAndCancle = true,
                requestCode = 711
        )

        val ONLINEID_BACKUP_RECEIVE = ReceiveLaunchOptions(
                text =  "Receive the full onlineId of your already existing online device",
                purpose = ActivityPurpose.ONLINEID,
                source =  DeviceTarget.OFFLINE,
                requestCode = 1941
        )

        // Scan Crash => messageoverview to offlineactity
            // Token might work
            // YES NO for confirming
            // Singledata


        val CRASH_RECEIVE = ReceiveLaunchOptions(
                purpose = ActivityPurpose.CRASH,
                source = DeviceTarget.OFFLINE,
                requestCode = 73
        )

    }

}