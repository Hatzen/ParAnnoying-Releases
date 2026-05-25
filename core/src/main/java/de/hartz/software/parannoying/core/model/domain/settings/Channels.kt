package de.hartz.software.parannoying.core.model.domain.settings

import android.content.Context
import de.hartz.software.parannoying.core.R

class Channels(var channel: Int = UNKNOWN_VAL ) {
    companion object {
        const val UNKNOWN_VAL: Int = -1
        private const val CAMERA_VAL: Int = 0
        private const val NFC_VAL: Int = 1
        private const val BLUETOOTH_VAL: Int = 2
        private const val TEXT_VAL: Int = 3
        private const val SOUND_VAL: Int = 4
        private const val VIDEO_VAL: Int = 5
        private const val SD_CARD_VAL: Int = 6
        // TODO: Do we want to implement these? They are the easiest to hack..
        // private const val WIFI_VAL: Int = 5
        // private const val USB_VAL: Int = 5

        // Must be same order as arrays.xml -> channel_values
        val CAMERA = Channels(CAMERA_VAL)
        val NFC = Channels(NFC_VAL)
        val BLUETOOTH = Channels(BLUETOOTH_VAL)
        val TEXT = Channels(TEXT_VAL)
        val SOUND = Channels(SOUND_VAL)
        val VIDEO = Channels(VIDEO_VAL)
        val SD_CARD = Channels(SD_CARD_VAL)


        val CHANNEL_LIST = listOf(CAMERA, NFC, BLUETOOTH, SOUND, VIDEO, SD_CARD, TEXT)

        fun getByChannel(channel: Int) : Channels {
            val listOfConsts = listOf(CAMERA, NFC, BLUETOOTH, TEXT, SOUND, VIDEO)
            for(channelO in listOfConsts) {
                if (channelO.channel == channel) {
                    return  channelO
                }
            }
            throw RuntimeException("Channel not defined")
        }

    }

    fun getName(context: Context): String {
        return context.getResources().getStringArray(R.array.channel_values)[channel]
    }

    override fun equals(other: Any?): Boolean {
        if (other is Channels) {
            return channel == other.channel
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return channel
    }

}
