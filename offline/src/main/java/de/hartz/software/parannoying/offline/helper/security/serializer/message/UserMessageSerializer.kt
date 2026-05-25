package de.hartz.software.parannoying.offline.helper.security.serializer.message

import android.util.Log
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.offline.model.OfflineStorage
import de.hartz.software.parannoying.offline.model.domain.dialogs.SimpleDialog
import de.hartz.software.parannoying.offline.model.domain.messages.MetaData


/**
 * General message structure:
 *
 * |x| String length of the entity x
 * +  New Entity
 * {} Stands for encrypted layer
 * TODO: onlineIdForTargetUser is it really the onlineId or should it be the notificationId, where the notification is sent to?
 * rawData: { { MetaData + Timestamp + |ownOnlineId| + { Data + Int(HMAC) } + ownOnlineId + |MetaData| } + onlineIdForTargetUser + |onlineIdForTargetUser| }
 *
 * Layer 1: Hardcoded encryption.
 * Data: { { Layer 2 } + onlineIdForTargetUser + |onlineIdForTargetUser| }
 * Layer 2: Symmetric encryption.
 * Data: { MetaData + Timestamp  + |ownOnlineId| + { Layer 1 } + ownOnlineId + |MetaData| }
 * Layer 3: Asymmetric encryption.
 * Data: { Data + Int(HMAC) }
 */
class UserMessageSerializer {

    companion object {
        fun serializeAndEncrypt (securityInterfaceHolder: SecurityInterfaceHolder, data: String, targetUser: SimpleDialog, metaData: MetaData, targetHash: String) : String {
            return UserMessageSerializer(securityInterfaceHolder, data, targetUser, metaData, targetHash).serializeMessage()
        }
    }
    val ivSize get() = securityInterfaceHolder.symmetricEncryptionHelper.SEED_SIZE

    private val securityInterfaceHolder: SecurityInterfaceHolder
    private val rawData: String
    private val targetUser: SimpleDialog
    private val metaData: MetaData
    private val targetHash: String

    private val randomIv: String

    private constructor(securityInterfaceHolder: SecurityInterfaceHolder, data: String, targetUser: SimpleDialog, metaData: MetaData, targetHash: String) {
        this.securityInterfaceHolder = securityInterfaceHolder
        rawData = data
        this.targetUser = targetUser
        this.metaData = metaData
        this.randomIv = securityInterfaceHolder.randomHelper.computeRandomHashWithSpecificLength(ivSize)
        this.targetHash = targetHash
    }

    private fun serializeMessage () : String {
        // TODO: Layernaming is fucked up. It is reversed order from this perspective.
        val layer1String = prepareLayer1()

        val publicKey = targetUser.encryptionKeyCloakForUser.encryptionKey
        val symmetricKey = targetUser.encryptionKeyCloakForUser.symmetricKey

        val layer1EncryptedString = securityInterfaceHolder.asymmetricEncryptionHelper.encrypt(layer1String, publicKey)!!
        val layer2String = prepareLayer2(layer1EncryptedString)
        val ivString = securityInterfaceHolder.hashHelper.hashWithSpecificLength(targetUser.newestReceivedToken, randomIv, length = ivSize)
        val layer2EncryptedString = securityInterfaceHolder.symmetricEncryptionHelper.encrypt(layer2String,
            symmetricKey,
            ivString)!!
        Log.v(javaClass.simpleName, "Use Tokens for encryption \n" +
                "newestToken: " + targetUser.newestReceivedToken + "\n" +
                "randomIv: " + randomIv + "\n" +
                "From device" + OfflineStorage.INSTANCE.currentUser.originalName + "\n" +
                "To device" + targetUser.originalName + "\n" +
                "combinedIv/for encryption  " + ivString + "\n" +
                "Layer2Encrypted " + layer2EncryptedString + "\n" +
                "Decrypted Layer2 " + layer2String
        )
        Log.v(javaClass.simpleName, "Use Key for encryption: " + symmetricKey)

        var layer3String = perpareLayer3(layer2EncryptedString, randomIv)
        val layer3EncryptedString = securityInterfaceHolder.hardcodedEncryptionHelper.encrypt(layer3String)
        val combined = layer3EncryptedString
        return combined
    }

    private fun prepareLayer1 () : String {
        val HMAC = securityInterfaceHolder.hmacHelper.getHMACForMessage(rawData)
        // val HMAC_KEY = HashHelper.hashWithSpecificLength(Storage.currentUser.originalName, targetUser.originalName, length = DataSecurityHelper.KEY_SIZE) // TODO: Change to original name
        // val HMAC = SymmetricEncryptionHelper(HMAC_KEY.toByteArray(Charset.forName("UTF-8")), metaData.newToken.toByteArray(Charset.forName("UTF-8"))).encrypt(DataSecurityHelper.intToString(checksum))
        Log.v(javaClass.simpleName, "HMAC is " + HMAC + "\n" +
                " of data " + rawData + "\n"
        )
        val layer1Content = rawData + HMAC

        return layer1Content
    }

    private fun prepareLayer2 (layer1: String) : String {
        // Append currentuserid to identifiy message.
        val layer1SourroundedByOwnUserHash =
            securityInterfaceHolder.dataConverter.intToString(OfflineStorage.INSTANCE.currentUser.hash.length) +
                layer1 +
                OfflineStorage.INSTANCE.currentUser.hash
        val currentDate = IOHelper.getCurrentDateAsUnixTimestamp()
        val timestampFollowedByLayer1SourroundedByOwnUserHash =  securityInterfaceHolder.dataConverter.longToString(currentDate) + layer1SourroundedByOwnUserHash

        val metaDataString = metaData.toString()
        val timestampFollowedByLayer1SourroundedByOwnUserHashAndSourroundedByMetadata
                = metaDataString + timestampFollowedByLayer1SourroundedByOwnUserHash + securityInterfaceHolder.dataConverter.intToString(metaDataString.length)
        return timestampFollowedByLayer1SourroundedByOwnUserHashAndSourroundedByMetadata
    }

    private fun perpareLayer3 (layer2: String, randomIV: String) : String {
        val onlineIdForTargetUser = targetHash
        val layer2FollowedByTargetUserHash= layer2 + onlineIdForTargetUser + securityInterfaceHolder.dataConverter.intToString(onlineIdForTargetUser.length)
        val layer2PrependWithRandomIvFollowedByTargetUserHash = randomIV + layer2FollowedByTargetUserHash
        return layer2PrependWithRandomIvFollowedByTargetUserHash
    }

}
