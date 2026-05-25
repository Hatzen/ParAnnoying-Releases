package de.hartz.software.parannoying.core.model.domain.welcome

import android.content.Context
import de.hartz.software.parannoying.core.helper.ui.getBluetoothIcon
import de.hartz.software.parannoying.core.helper.ui.getCameraIcon
import de.hartz.software.parannoying.core.helper.ui.getNfcIcon
import de.hartz.software.parannoying.core.helper.ui.getSDCardIcon
import de.hartz.software.parannoying.core.helper.ui.getSoundIcon
import de.hartz.software.parannoying.core.helper.ui.getTextIcon
import de.hartz.software.parannoying.core.helper.ui.getVideoIcon

val listOfChannels = listOf<ChannelExplanation>(
        ChannelExplanation(
        "Photo",
            """
            Channel generates and scannes single qrcodes which is most secure as it communicates only in one direction and it is hard to do man in the middle attacks. But the data to transfer is limited
            """.trimMargin().trimIndent(),
            2,
            5,
            2,
            Context::getCameraIcon
        ),
        ChannelExplanation(
                "NFC",
                """
            The nfc channel uses induction and transfers data between to devices only in very close range (up to 5 cm). It can be relatively fast with up to 400kb/s and is not to easy to get attacked by man in the middle.
            """.trimMargin().trimIndent(),
                3,
                3,
                3,
                Context::getNfcIcon
        ),
        ChannelExplanation(
                "Bluetooth",
                """
            The bluetooth channel connects two devices via bluetooth in a range of ca. 5m, both devices need bluetooth support and enabled the receiver needs to be discoverable. It is considered the leaast secure channel as it has a persistent connection to an other device.
            """.trimMargin().trimIndent(),
                3,
                1,
                4,
                Context::getBluetoothIcon
        ),
        ChannelExplanation(
                "SD-Card",
                """
               To transfer data an extra sd card is needed and both devices need to have a sd card slot. It is relativley fast and as long as there is no access of foreign devices copying data which will be autorun there can not be an man in the middle attack.
            """.trimMargin().trimIndent(),
                3,
                4,
                4,
                Context::getSDCardIcon
        ),
        ChannelExplanation(
                "Sound",
                """
            The sound channel works like an old acoustic coupler making strange noises and records them to transfer them. It is mandatory to have a silend environment and spoofing data might be relativley easy.
            """.trimMargin().trimIndent(),
                2,
                2,
                2,
                Context::getSoundIcon
        ),
        ChannelExplanation(
                "Video",
                """
            Video is an improved version of qrcodes as it uses animated qrcode with eraser codes which improve speed and throughput.
            """.trimMargin().trimIndent(),
                3,
                3,
                3,
                Context::getVideoIcon
        ),
        ChannelExplanation(
                "Text",
                """
            The text channel is usually not useful but it can be used to transfer data via an other app or to see the real data which should be transferred.
            """.trimMargin().trimIndent(),
                1,
                2, // Shared clipboard, plaintext etc.
                1, // 2MB max size, hard to process
                Context::getTextIcon
        )
)