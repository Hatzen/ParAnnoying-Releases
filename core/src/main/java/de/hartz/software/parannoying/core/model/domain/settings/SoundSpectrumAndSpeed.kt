package de.hartz.software.parannoying.core.model.domain.settings

import android.content.Context
import de.hartz.software.parannoying.core.R

class SoundSpectrumAndSpeed(var speed: Int = UNKNOWN_VAL ) {
    companion object {
        const val UNKNOWN_VAL: Int = -1
        private const val AUDIBLE_NORMAL_VAL: Int = 0
        private const val AUDIBLE_FAST_VAL: Int = 1
        private const val AUDIBLE_FASTEST_VAL: Int = 2
        private const val ULTRASOUND_NORMAL_VAL: Int = 3
        private const val ULTRASOUND_FAST_VAL: Int = 4
        private const val ULTRASOUND_FASTEST_VAL: Int = 5
        private const val DT_NORMAL_VAL: Int = 6
        private const val DT_FAST_VAL: Int = 7
        private const val DT_FASTEST_VAL: Int = 8

        val AUDIBLE_NORMAL = SoundSpectrumAndSpeed(AUDIBLE_NORMAL_VAL)
        val AUDIBLE_FAST = SoundSpectrumAndSpeed(AUDIBLE_FAST_VAL)
        val AUDIBLE_FASTEST = SoundSpectrumAndSpeed(AUDIBLE_FASTEST_VAL)
        val ULTRASOUND_NORMAL = SoundSpectrumAndSpeed(ULTRASOUND_NORMAL_VAL)
        val ULTRASOUND_FAST = SoundSpectrumAndSpeed(ULTRASOUND_FAST_VAL)
        val ULTRASOUND_FASTEST = SoundSpectrumAndSpeed(ULTRASOUND_FASTEST_VAL)
        val DT_NORMAL = SoundSpectrumAndSpeed(DT_NORMAL_VAL)
        val DT_FAST = SoundSpectrumAndSpeed(DT_FAST_VAL)
        val DT_FASTEST = SoundSpectrumAndSpeed(DT_FASTEST_VAL)


        val SPEED_LIST = listOf(AUDIBLE_NORMAL, AUDIBLE_FAST, AUDIBLE_FASTEST,ULTRASOUND_NORMAL, ULTRASOUND_FAST, ULTRASOUND_FASTEST
                ,DT_NORMAL, DT_FAST, DT_FASTEST)

        fun getBySpeed(speed: Int) : SoundSpectrumAndSpeed {
            for(option in SPEED_LIST) {
                if (option.speed == speed) {
                    return option
                }
            }
            throw RuntimeException("Speed not defined")
        }

    }

    fun getName(context: Context): String {
        return context.getResources().getStringArray(R.array.sound_values)[speed]
    }

    override fun equals(other: Any?): Boolean {
        if (other is SoundSpectrumAndSpeed) {
            return speed == other.speed
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return speed
    }

}
