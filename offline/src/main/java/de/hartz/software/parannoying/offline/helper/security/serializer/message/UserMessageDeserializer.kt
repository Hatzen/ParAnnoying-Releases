package de.hartz.software.parannoying.offline.helper.security.serializer.message

import android.content.Context
import android.util.Log
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject
import de.hartz.software.parannoying.offline.helper.security.impl.asymmetric.rsa.KeyConverter
import de.hartz.software.parannoying.offline.helper.security.serializer.AbstractDeserializer
import de.hartz.software.parannoying.offline.helper.security.serializer.SourceUserDeterminer
import de.hartz.software.parannoying.offline.model.OfflineStorage
import de.hartz.software.parannoying.offline.model.domain.DecryptionKeyCloakForUser
import de.hartz.software.parannoying.offline.model.domain.dialogs.OfflineGroup
import de.hartz.software.parannoying.offline.model.domain.dialogs.SimpleDialog
import de.hartz.software.parannoying.offline.model.domain.dialogs.User
import de.hartz.software.parannoying.offline.model.domain.messages.MetaData
import de.hartz.software.parannoying.offline.model.domain.messages.UserMessage
import de.hartz.software.parannoying.offline.model.domain.settings.MessageSecurity
import de.hartz.software.parannoying.offline.model.exceptions.MessageModifiedException
import java.security.InvalidKeyException
import java.security.PrivateKey
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException


class UserMessageDeserializerImpl(val context: Context, val securityInterfaceHolder: SecurityInterfaceHolder): AbstractDeserializer<UserMessage>() {

    override fun decryptMessage (data: String, sourceUser: SimpleDialog, symmetricToken: String) : UserMessage {
        return UserMessageDeserializer(context, securityInterfaceHolder, data, sourceUser, symmetricToken).deserializeMessage()
    }

    override fun decryptMessage (data: String, keys: DecryptionKeyCloakForUser, symmetricToken: String) : Pair<UserMessage, SimpleDialog> {
        val serializer =  UserMessageDeserializer(context, securityInterfaceHolder, data, keys, symmetricToken)
        val message = serializer.deserializeMessage()
        return Pair(message, serializer.sourceUser!!)
    }
}

class UserMessageDeserializer {

    private val rawData: String
    private val securityInterfaceHolder: SecurityInterfaceHolder
    private val context: Context
    private val symmetricToken: String
    private val symmetricKey: String
    private val privateKey: PrivateKey
    var sourceUser: SimpleDialog? = null

    val base64LengthOfInt get() = securityInterfaceHolder.dataConverter.base64LengthOfInt()
    val base64LengthOfLong get() = securityInterfaceHolder.dataConverter.base64LengthOfLong()
    val ivSize get() = securityInterfaceHolder.symmetricEncryptionHelper.SEED_SIZE

    private lateinit var randomIV: String

    private val result: UserMessage

    internal constructor(context: Context, securityInterfaceHolder: SecurityInterfaceHolder, data: String, sourceUser: SimpleDialog, symmetricToken: String) {
        rawData = data
        this.securityInterfaceHolder = securityInterfaceHolder
        this.context = context
        this.sourceUser = sourceUser
        this.symmetricToken = symmetricToken
        result = UserMessage()
        if (OfflineStorage.INSTANCE.readSettings().messageSecurity == MessageSecurity.DELETE) {
            result.persistenceId = UniqueRealmObject.ID_META_DO_NOT_PERSIST
        }
        result.relatedDialog = sourceUser
        result.metaData = MetaData()
        if (sourceUser is User) {
            result.sender = sourceUser
        } else if (sourceUser is OfflineGroup) {
            // Sender will be determined later where we usually verfiy the sender
        }

        privateKey = sourceUser.decryptionKeyCloakForUser!!.decryptionKey
        symmetricKey = sourceUser.decryptionKeyCloakForUser!!.symmetricKey
    }

    internal constructor(context: Context, securityInterfaceHolder: SecurityInterfaceHolder, data: String, keys: DecryptionKeyCloakForUser, symmetricToken: String) {
        rawData = data
        this.securityInterfaceHolder = securityInterfaceHolder
        this.context = context
        this.symmetricToken = symmetricToken

        result = UserMessage()
        if (OfflineStorage.INSTANCE.readSettings().messageSecurity == MessageSecurity.DELETE) {
            result.persistenceId = UniqueRealmObject.ID_META_DO_NOT_PERSIST
        }
        result.metaData = MetaData()

        privateKey = keys.decryptionKey
        symmetricKey = keys.symmetricKey
    }

    fun deserializeMessage () : UserMessage {
        extractDataFromLayer1()
        return result
    }

    private fun extractDataFromLayer1() {
        val layer2String = extractDataFromLayer2()
        try {
            val layer1String = getLayer1Encrypted(layer2String)
            val layer1DecryptedString = decryptLayer2(layer1String) // This will set result and the related user, the return value isnt used.
        } catch (exception: RuntimeException) {
            throw RuntimeException("Layer 1 decryption exception", exception)
        }
    }

    private fun decryptLayer2 (layer1String: String) : String {
        val sourceUser = sourceUser
        if (sourceUser is OfflineGroup) {
            val privateKey = sourceUser.extractPrivateKey(context, securityInterfaceHolder)

            val layer1DecryptedString = securityInterfaceHolder.asymmetricEncryptionHelper.decrypt(layer1String, privateKey)!!
            extractRawMessage(layer1DecryptedString)
            if (layer1DecryptedString == null) {
                throw IllegalArgumentException("There was no matching private key.")
            }
            return layer1DecryptedString
        } else if (sourceUser is User || sourceUser == null) {
            var layer1DecryptedString: String? = null
            try {
                val privateKeyString = KeyConverter().convertToDatabaseValue(privateKey)!!
                layer1DecryptedString = securityInterfaceHolder.asymmetricEncryptionHelper.decrypt(layer1String, privateKey)!!
                extractRawMessage(layer1DecryptedString)
            } catch (e: Exception) {
                when(e) {
                    is InvalidKeyException,
                    is BadPaddingException,
                    is IllegalBlockSizeException -> {
                        throw IllegalStateException("a private key didnt match. For related public key " + sourceUser?.encryptionKeyCloakForUser?.encryptionKey.toString())
                    }
                    else -> {
                        throw IllegalStateException("An error occured, message could get decrypted but information are not valid.", e)
                    }
                }
            }
            return layer1DecryptedString
        }
        throw IllegalStateException("Cannot be reached.")
    }

    private fun extractDataFromLayer2() : String {
        val layer3String = extractDataFromLayer3()
        try {
            val layer2EncryptedString = getLayer2Encrypted(layer3String)
            val iv = securityInterfaceHolder.hashHelper.hashWithSpecificLength(symmetricToken, randomIV, length = ivSize)
            val layer2String = securityInterfaceHolder.symmetricEncryptionHelper.decrypt(layer2EncryptedString, symmetricKey, iv)!!
            Log.v(javaClass.simpleName, "Use Tokens for decryption \n" +
                    "newestToken: " + symmetricToken + "\n" +
                    "randomIv: " + randomIV + "\n" +
                    "combinedIv: " + securityInterfaceHolder.hashHelper.hashWithSpecificLength(symmetricToken, randomIV, length = ivSize) + "\n" +
                    "For device" + OfflineStorage.INSTANCE.currentUser.originalName + "\n" +
                    "From device" + sourceUser?.originalName + "\n" +
                    "Layer2Encrypted " + layer2EncryptedString + "\n" +
                    "Decrypted Layer2 " + layer2String
            )
            Log.v(javaClass.simpleName, "Use Key for decryption: " + symmetricKey)
            return layer2String
        } catch (exception: RuntimeException) {
            throw RuntimeException("Layer 2 decryption exception", exception)
        }
    }

    private fun extractDataFromLayer3() : String {
        try {
            val layer3EncryptedString = rawData
            val layer3StringWithIVPrefix = securityInterfaceHolder.hardcodedEncryptionHelper.decrypt(layer3EncryptedString)
            randomIV = layer3StringWithIVPrefix.substring(0, ivSize)
            val layer3String = layer3StringWithIVPrefix.substring(ivSize, layer3StringWithIVPrefix.length)
            return layer3String
        } catch (exception: RuntimeException) {
            throw RuntimeException("Layer 3 decryption exception", exception)
        }
    }

    private fun getLayer2Encrypted (layer3Decrypted: String) : String {
        // Reminder: substrings beginIndex is inclusive and endIndex is exclusive while getting the substring.
        val base64LengthOfOnlineId = layer3Decrypted.substring(layer3Decrypted.length - base64LengthOfInt, layer3Decrypted.length)
        val onlineIdLength = securityInterfaceHolder.dataConverter.stringToInt(base64LengthOfOnlineId)
        val layer2String = layer3Decrypted.substring( 0, layer3Decrypted.length  - base64LengthOfInt- onlineIdLength)
        return layer2String
    }

    private fun getLayer1Encrypted (layer2Decrypted: String) : String {
        val timestampFollowedByLayer1SourroundedByOwnUserHashAndSourroundedByMetadata = layer2Decrypted
        val timestampFollowedByLayer1SourroundedByOwnUserHash = extractMetaDataAndReturnCleanedResult(timestampFollowedByLayer1SourroundedByOwnUserHashAndSourroundedByMetadata)
        val layer1SourroundedByOwnUserHash = extractTimestampAndReturnCleanedResult(timestampFollowedByLayer1SourroundedByOwnUserHash)
        val layer1Encrypted = extractUserhashAndReturnCleanedResult(layer1SourroundedByOwnUserHash)
        return layer1Encrypted
    }

    private fun extractRawMessage (rawDataFollowedByHmac: String) {
        val message = rawDataFollowedByHmac.substring(0, rawDataFollowedByHmac.length - base64LengthOfInt)
        val HMAC = rawDataFollowedByHmac.substring(rawDataFollowedByHmac.length - base64LengthOfInt, rawDataFollowedByHmac.length)
        val currentChecksum = securityInterfaceHolder.hmacHelper.getHMACForMessage(message)
        try {
            // val HMAC_KEY = HashHelper.hashWithSpecificLength(Storage.currentUser.originalName, sourceUser.originalName, length = DataSecurityHelper.KEY_SIZE)
           //  val checksum = SymmetricEncryptionHelper(HMAC_KEY.toByteArray(Charset.forName("UTF-8")), result.metaData.newToken.toByteArray(Charset.forName("UTF-8")))
           //         .encrypt(HMAC)
            if (HMAC != currentChecksum) {
                Log.w(javaClass.simpleName, "encrypted checksum is " + currentChecksum + "\n" +
                        "HMAC is " + HMAC + " with value " + HMAC + "\n" +
                        " of data " + rawDataFollowedByHmac + "\n"
                )
                throw MessageModifiedException()
            }
        } catch (exception: Exception) {
            Log.w(javaClass.simpleName, "encrypted checksum is " + currentChecksum + "\n" +
                    "HMAC is " + HMAC + "\n" +
                    " of data " + rawDataFollowedByHmac + "\n"
            )
            throw MessageModifiedException()
        }
        result.message = message
    }

    private fun extractUserhashAndReturnCleanedResult (layer1SourroundedByOwnUserHash: String) : String {
        // Verify user.
        val lengthOfUserHash = securityInterfaceHolder.dataConverter.stringToInt(layer1SourroundedByOwnUserHash.substring(0, base64LengthOfInt))
        val sourceUserHash = layer1SourroundedByOwnUserHash.substring( layer1SourroundedByOwnUserHash.length - lengthOfUserHash, layer1SourroundedByOwnUserHash.length)
        sourceUser = SourceUserDeterminer.determine(sourceUser, sourceUserHash) // TODO: Verify sourceUser must not only be assigned for virgin user
        result.sender = sourceUser
        val messageData = layer1SourroundedByOwnUserHash.substring( base64LengthOfInt, layer1SourroundedByOwnUserHash.length - lengthOfUserHash)
        return messageData
    }

    private fun extractTimestampAndReturnCleanedResult (timestampFollowedByLayer1SourroundedByOwnUserHash: String) : String {
        // Get sent date from message.
        val currentDate = securityInterfaceHolder.dataConverter.stringToLong(timestampFollowedByLayer1SourroundedByOwnUserHash.substring(0, base64LengthOfLong))
        result.createdAtTimestamp = currentDate
        return  timestampFollowedByLayer1SourroundedByOwnUserHash.substring(base64LengthOfLong, timestampFollowedByLayer1SourroundedByOwnUserHash.length)
    }

    // metaDataString + timestampFollowedByLayer1SourroundedByOwnUserHash + DataSecurityHelper.intToString(metaDataString.length)
    private fun extractMetaDataAndReturnCleanedResult (layer2Decrypted: String) : String {
        // Get meta data from message.
        val lengthOfMetaData = securityInterfaceHolder.dataConverter.stringToInt(layer2Decrypted.substring(layer2Decrypted.length - base64LengthOfInt, layer2Decrypted.length))
        result.metaData.dataFromString(layer2Decrypted.substring(0, lengthOfMetaData))
        return layer2Decrypted.substring(lengthOfMetaData, layer2Decrypted.length - base64LengthOfInt)
    }

}
