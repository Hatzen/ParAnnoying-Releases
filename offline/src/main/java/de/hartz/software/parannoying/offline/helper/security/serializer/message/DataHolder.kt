package de.hartz.software.parannoying.offline.helper.security.serializer.message

import android.util.Log
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder

// TODO: Use as wrapper so we can encapsulate this serialisation.
class DataHolder(var finalData: String = "", val securityInterfaceHolder: SecurityInterfaceHolder) {


    /**
     * |data| + data + finalData
     */
    fun prependData(data: String): DataHolder {
        finalData = data + finalData
        finalData = securityInterfaceHolder.dataConverter.intToString(data.length) + finalData
        Log.e(javaClass.simpleName, "prependData: " + data + " " +  data.length)
        return this
    }

    /**
    * finalData + data + |data|
    */
    fun appendData(data: String): DataHolder {
        finalData += data
        finalData += securityInterfaceHolder.dataConverter.intToString(data.length)
        Log.e(javaClass.simpleName, "appendData: " + data + " " + data.length)
        return this
    }


    /**
     * |data| + data + finalData
     * @return data
     */
    fun removePrependedData(): String {
        val base64LengthOfInt = securityInterfaceHolder.dataConverter.base64LengthOfInt()
        val positionOfDataLength = 0
        val dataLength = securityInterfaceHolder.dataConverter.stringToInt(finalData.substring(positionOfDataLength, base64LengthOfInt))
        val data = finalData.substring(base64LengthOfInt, dataLength + base64LengthOfInt)
        finalData = finalData.substring(dataLength + base64LengthOfInt)
        Log.e(javaClass.simpleName, "removePrependedData: " + data + " " + data.length)
        return data
    }

    /**
     * finalData + data + |data|
     * @return data
     */
    fun removeAppendedData(): String {
        val base64LengthOfInt = securityInterfaceHolder.dataConverter.base64LengthOfInt()
        val positionOfDataLength = finalData.length - base64LengthOfInt
        val dataLength = securityInterfaceHolder.dataConverter.stringToInt(finalData.substring(positionOfDataLength, finalData.length))
        val positionOfData = positionOfDataLength - dataLength
        val data = finalData.substring(positionOfData, positionOfData + dataLength)
        finalData = finalData.substring(0, positionOfData)
        Log.e(javaClass.simpleName, "removeAppendedData: " + data + " " +  data.length)
        return data
    }
}

open class DataSchema(val list: List<String>) {

}

class UserIdSchema: DataSchema(
    listOf(KEY_USERNAME)
) {
    companion object {
        val KEY_USERNAME = "KEY_USERNAME"
    }

}

class OnlineGroupIdSchema: DataSchema(
    listOf(KEY_USERNAME)
) {
    companion object {
        val KEY_USERNAME = "KEY_GROUPNAME"
    }

}