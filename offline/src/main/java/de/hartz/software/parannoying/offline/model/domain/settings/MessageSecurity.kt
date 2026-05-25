package de.hartz.software.parannoying.offline.model.domain.settings

import java.lang.RuntimeException

data class MessageSecurity(var security: Int = UNKNOWN_VAL) {
    companion object {
        const val UNKNOWN_VAL: Int = -1
        const val NONE_VAL: Int = 0
        const val HIDE_VAL: Int = 1
        const val DELETE_VAL: Int = 2

        // Must be same order as arrays.xml -> channel_values
        val NONE = MessageSecurity(NONE_VAL)
        val HIDE = MessageSecurity(HIDE_VAL)
        val DELETE = MessageSecurity(DELETE_VAL)

        fun getBySecurity(security: Int) : MessageSecurity {
            val listOfConsts = listOf(NONE, HIDE, DELETE)
            for(channelO in listOfConsts) {
                if (channelO.security == security) {
                    return  channelO
                }
            }
            throw RuntimeException("The security level is not defined.")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is MessageSecurity) {
            return security == other.security
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return security
    }

    constructor() : this(UNKNOWN_VAL)
}