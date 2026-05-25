package de.hartz.software.parannoying.online.model.domain

import android.content.Context

class ServerType(var type: Int = -1 ) {
    companion object {
        const val UNKNOWN_VAL: Int = -1
        private const val FIREBASE_VAL: Int = 0
        private const val SUPABASE_VAL: Int = 1
        private const val NONE_VAL: Int = 2
        private const val DEFAULT_FIREBASE_VAL: Int = 3

        val FIREBASE = ServerType(FIREBASE_VAL)
        val SUPABASE = ServerType(SUPABASE_VAL)
        val NONE = ServerType(NONE_VAL)
        val DEFAULT_FIREBASE = ServerType(DEFAULT_FIREBASE_VAL)


        val SERVER_TYPE_LIST = listOf(FIREBASE, SUPABASE, NONE, DEFAULT_FIREBASE)

        fun getByType(type: Int) : ServerType {
            for(option in SERVER_TYPE_LIST) {
                if (option.type == type) {
                    return option
                }
            }
            throw RuntimeException("type not defined")
        }

    }

    fun getName(context: Context): String {
        return when (type) {
            FIREBASE_VAL -> {
                "Firebase"
            }
            SUPABASE_VAL -> {
                "Supabase"
            }
            NONE_VAL -> {
                "No Server"
            }
            DEFAULT_FIREBASE_VAL -> {
                "Firebase default"
            }
            else -> {
                throw RuntimeException("$type is not a valid value")
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is ServerType) {
            return type == other.type
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return type
    }

}
