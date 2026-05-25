package de.hartz.software.parannoying.core.helper.security

import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.interfaces.di.StorageInterface
import java.io.File
import java.io.FileInputStream
import java.util.Scanner
import javax.inject.Inject
import kotlin.math.min


class DataSecurityHelper @Inject constructor(val serviceHolder: SecurityInterfaceHolder){
    companion object {
        // Important Both need to have same length!
        const val NOTIFICATION_ID_PREFIX_VALID = "XMOD876"
        const val NOTIFICATION_ID_PREFIX_INVALID = "YMOD879"
        const val NOTIFICATION_ID_PREFIX_GROUP_OFFLINE = "ZMOD351"

        const val ONLINE_GROUP_ONLINE_ID_SEPARATOR = ";"
        const val ONLINE_GROUP_ONLINE_ID_TARGET_PREFIX_MARKER = "|"
        const val FILE_CHUNK_SEPERATOR = "%_%"

        const val ONLINE_EMAIL_POSTFIX = "@parannoying.de"

        //MAX_RSA_CONTENT_LENGTH = 127 for ASYMMETRIC_KEY_SIZE=1024, MAX_RSA_CONTENT_LENGTH = 255 for ASYMMETRIC_KEY_SIZE=2048,
        //MAX_RSA_CONTENT_LENGTH - 10 Chars because hmac will be added to rsa layer
        const val MAX_MESSAGE_SIZE = 118
    }

    /**
     * Data: |Username| + Username + |Key| + Key + DeviceId + OnlineId + |OnlineId| + Privatekey + |Privatekey|  + SymmetricKey + |SymmetricKey| + Iv + |Iv|
     */
    fun getLastStringFromUserId(data: String) : String {
        val base64LengthOfInt = serviceHolder.dataConverter.base64LengthOfInt()
        val endIndexOfData = data.length - base64LengthOfInt
        val lastStringLength =  serviceHolder.dataConverter.stringToInt(data.substring(endIndexOfData, data.length))
        val lastStringData = data.substring(endIndexOfData - lastStringLength, endIndexOfData)
        return lastStringData
    }

    fun removeLastString (data: String) : String {
        val base64LengthOfInt = serviceHolder.dataConverter.base64LengthOfInt()
        val endIndexOfData = data.length - base64LengthOfInt
        val lastStringLength = serviceHolder.dataConverter.stringToInt(data.substring(endIndexOfData, data.length))
        return data.substring(0, data.length - lastStringLength - base64LengthOfInt)
    }

    fun getOnlineIdFromMessage(data: String) : String {
        val cleanData =  serviceHolder.hardcodedEncryptionHelper.decrypt(data)
        val base64LengthOfInt = serviceHolder.dataConverter.base64LengthOfInt()
        val positionOfData = cleanData.length - base64LengthOfInt
        val onlineIdLength = serviceHolder.dataConverter.stringToInt(cleanData.substring(positionOfData, cleanData.length))
        val onlineIdWithPrefix = cleanData.substring(cleanData.length - onlineIdLength - base64LengthOfInt, cleanData.length - base64LengthOfInt)
        // val onlineId = onlineIdWithPrefix.substring(NOTIFICATIONID_PREFIX_VALID.length)
        // Prefix needed so the online device can still determine if it is a valid onlineId from a message.
        return onlineIdWithPrefix
    }

    fun getOnlineIdFromFile(inputfile: String): String  {
        val file = File(inputfile)
        val stream = FileInputStream(file)
        val scanner = Scanner(stream)
        scanner.useDelimiter(FILE_CHUNK_SEPERATOR)

        val prefix = scanner.next()
        scanner.close()

        val cleanedPrefix =  serviceHolder.hardcodedEncryptionHelper.decrypt(prefix)
        val targetOnlineId = getLastStringFromUserId(cleanedPrefix)

        return targetOnlineId
    }


    fun isOnlineIdForOnlineGroup(onlineId: String) : Boolean {
        if (onlineId.contains(ONLINE_GROUP_ONLINE_ID_SEPARATOR) || onlineId.contains(ONLINE_GROUP_ONLINE_ID_TARGET_PREFIX_MARKER) ) {
            return true
        }
        return false
    }

    /**
     * The onlineId of an online group may contain a specific target marked by ONLINE_GROUP_ONLINE_ID_TARGET_PREFIX_MARKER, so every
     * member gets an own message with slightly different online groupId.
     *  When there is no marker the first entry has it implicitly set.
     */
    fun getOnlineIdForTargetOfOnlineGroup(onlineId: String) : String {
        if (onlineId.contains(ONLINE_GROUP_ONLINE_ID_TARGET_PREFIX_MARKER)) {
            val startIndex = onlineId.indexOf(ONLINE_GROUP_ONLINE_ID_TARGET_PREFIX_MARKER)
            val lastIndexOfSeparator = onlineId.indexOf(ONLINE_GROUP_ONLINE_ID_SEPARATOR, startIndex)
            val endIndex = if(lastIndexOfSeparator > 0) lastIndexOfSeparator else onlineId.length
            return onlineId.substring(startIndex, endIndex)
                    .removePrefix(ONLINE_GROUP_ONLINE_ID_TARGET_PREFIX_MARKER)
                    .removeSuffix(ONLINE_GROUP_ONLINE_ID_SEPARATOR)
        }
        // When there is no marker the first entry has it implicitly set.
        val indexOfSeparator = onlineId.indexOf(ONLINE_GROUP_ONLINE_ID_SEPARATOR)
        val indexOfMarker = onlineId.indexOf(ONLINE_GROUP_ONLINE_ID_TARGET_PREFIX_MARKER)
        val endOfFirstOnlineId = if (indexOfSeparator < 0) {
            indexOfMarker // TODO: this case should not happen
        } else if (indexOfMarker < 0) {
            indexOfSeparator
        } else {
            min(indexOfSeparator, indexOfMarker)
        }
        return  onlineId.substring(0, endOfFirstOnlineId)
    }

    fun isOnlineIdValid(onlineId: String) : Boolean {
        if (onlineId.startsWith(NOTIFICATION_ID_PREFIX_VALID)) {
            return true
        }
        if (isOnlineIdForOnlineGroup(onlineId)) {
            return true
        }
        if (onlineId.startsWith(NOTIFICATION_ID_PREFIX_INVALID)
                || onlineId.startsWith(NOTIFICATION_ID_PREFIX_GROUP_OFFLINE)) {
            return false
        }
        // TODO: Better throw an exception?
        throw RuntimeException("This onlineId is completly invalid nether a groupId nor a valid or invalid onlineId")
        //return false
    }

    fun getFullOnlineUserData(Storage: StorageInterface<*, *>): String {
        if (Storage.onlineUserEmail == null) {
            return Storage.onlineUserId!!
        }
        return Storage.onlineUserEmail!! + Storage.onlineUserIdPassword!!
    }
}
