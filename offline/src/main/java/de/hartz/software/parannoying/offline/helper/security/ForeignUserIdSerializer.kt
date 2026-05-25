package de.hartz.software.parannoying.offline.helper.security

import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.offline.helper.security.impl.asymmetric.rsa.KeyConverter
import de.hartz.software.parannoying.offline.helper.security.serializer.message.DataHolder
import de.hartz.software.parannoying.offline.model.OfflineStorage
import de.hartz.software.parannoying.offline.model.domain.EncryptionKeyCloakForUser


/**
 * PrivateKey is key for signing
 * PublicKey is the Key for decryption
 *
 * |Username| + Username + |PrivateKey| + PrivateKey + DeviceId + PublicKey + |PublicKey|
 *      + SymmetricKey + |SymmetricKey| + IV + |IV| + OnlineId + |OnlineId|
 */
class ForeignUserIdSerializer(val securityInterfaceHolder: SecurityInterfaceHolder, val storage: OfflineStorage) {

    fun serializeUserId(userName: String, onlineId: String, encryptionKeyCloakForUser: EncryptionKeyCloakForUser, iv: String) : String {
        var deviceId = storage.userId
        val dataHolder = DataHolder(deviceId, securityInterfaceHolder)

        val stringSignKey = KeyConverter().convertToDatabaseValue(encryptionKeyCloakForUser.signedKey)!!
        val stringPublicKey = KeyConverter().convertToDatabaseValue(encryptionKeyCloakForUser.encryptionKey)!!

        return dataHolder
            .prependData(stringSignKey)
            .prependData(userName)
            // userId
            .appendData(stringPublicKey)
            .appendData(encryptionKeyCloakForUser.symmetricKey)
            .appendData(iv)
            .appendData(onlineId)
            .finalData
    }

    lateinit var userName: String
    lateinit var onlineId: String
    lateinit var stringSignKey: String
    lateinit var stringPublicKey: String
    lateinit var symmetricKey: String
    lateinit var iv: String
    lateinit var deviceId: String

    fun deserializeUserId(data: String) {
        val dataHolder = DataHolder(data, securityInterfaceHolder)

        onlineId = dataHolder.removeAppendedData()
        iv = dataHolder.removeAppendedData()
        symmetricKey = dataHolder.removeAppendedData()
        stringPublicKey = dataHolder.removeAppendedData()

        userName = dataHolder.removePrependedData()
        stringSignKey = dataHolder.removePrependedData()

        deviceId = dataHolder.finalData
    }

}