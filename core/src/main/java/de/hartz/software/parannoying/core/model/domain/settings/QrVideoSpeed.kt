package de.hartz.software.parannoying.core.model.domain.settings

import android.content.Context
import de.hartz.software.parannoying.core.R

class QrVideoSpeed(var speed: Int = UNKNOWN_VAL ) {
    companion object {
        const val UNKNOWN_VAL: Int = -1
        private const val SLOW_VAL: Int = 0
        private const val NORMAL_VAL: Int = 1
        private const val FAST_VAL: Int = 2

        val NORMAL = QrVideoSpeed(NORMAL_VAL)
        val SLOW = QrVideoSpeed(SLOW_VAL)
        val FAST = QrVideoSpeed(FAST_VAL)


        val SPEED_LIST = listOf(SLOW, NORMAL, FAST)

        fun getBySpeed(speed: Int) : QrVideoSpeed {
            for(option in SPEED_LIST) {
                if (option.speed == speed) {
                    return option
                }
            }
            throw RuntimeException("Speed not defined")
        }

    }

    fun getName(context: Context): String {
        return context.getResources().getStringArray(R.array.video_speed_values)[speed]
    }

    override fun equals(other: Any?): Boolean {
        if (other is QrVideoSpeed) {
            return speed == other.speed
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return speed
    }

}
