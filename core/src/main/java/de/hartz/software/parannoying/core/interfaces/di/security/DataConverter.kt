package de.hartz.software.parannoying.core.interfaces.di.security

interface DataConverter {
    fun longToString(int: Long) : String

    fun stringToLong(string: String) : Long

    fun intToString(int: Int) : String

    fun stringToInt(string: String) : Int

    fun byteArrayToString(string: ByteArray) :String

    fun stringToByteArray(string: String) : ByteArray

    fun base64LengthOfInt() : Int

    fun base64LengthOfLong() : Int
    fun base64Encode(string: ByteArray): String
    fun base64Decode(string: String): ByteArray

    fun objectToString(any: Any): String
    fun <T> stringToObject(string: String, clazz: Class<T>): T
}