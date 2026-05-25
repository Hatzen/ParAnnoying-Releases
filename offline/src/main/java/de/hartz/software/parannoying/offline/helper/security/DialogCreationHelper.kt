package de.hartz.software.parannoying.offline.helper.security

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.helper.security.DataGeneratorHelper
import de.hartz.software.parannoying.core.helper.security.DataSecurityHelper
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.offline.helper.security.impl.asymmetric.rsa.KeyConverter
import de.hartz.software.parannoying.offline.model.OfflineStorage
import de.hartz.software.parannoying.offline.model.domain.DecryptionKeyCloakForUser
import de.hartz.software.parannoying.offline.model.domain.EncryptionKeyCloakForUser
import de.hartz.software.parannoying.offline.model.domain.OfflineKeyPair
import de.hartz.software.parannoying.offline.model.domain.UserSecurity
import de.hartz.software.parannoying.offline.model.domain.dialogs.BaseDialog
import de.hartz.software.parannoying.offline.model.domain.dialogs.CurrentUser
import de.hartz.software.parannoying.offline.model.domain.dialogs.OfflineGroup
import de.hartz.software.parannoying.offline.model.domain.dialogs.OnlineGroup
import de.hartz.software.parannoying.offline.model.domain.dialogs.SimpleDialog
import de.hartz.software.parannoying.offline.model.domain.dialogs.User
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.util.UUID
import javax.inject.Inject

class DialogCreationHelper @Inject constructor(
    val context: Context,
    val securityInterfaceHolder: SecurityInterfaceHolder)
{

    private val storage = (context.app.Storage as OfflineStorage)
    private val PERMISSION_READ_STATE: Int = 1524


    @SuppressLint("MissingPermission")
    private fun generateDeviceId(context: Context): String {
        if (uniqueIdPossible(context)) {
            // TODO: Besser? https://stackoverflow.com/a/50677233/8524651
            // https://stackoverflow.com/a/2853253/8524651
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val tmDevice: String = "" + tm.deviceId
            val tmSerial: String = "" + tm.simSerialNumber
            var androidId: String = "" + android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID)
            androidId = securityInterfaceHolder.hashHelper.hash(androidId, tmSerial)
            val deviceUuid = UUID(androidId.hashCode().toLong(), tmDevice.hashCode().toLong()) //  shl 32 or tmSerial.hashCode()
            return deviceUuid.toString()
        }
        return securityInterfaceHolder.randomHelper.getRandomUUIDv4()
    }

    /**
     * Extract userid from data
     */
    private fun decryptUserId(userId: String) : String {
        val base64LengthOfInt = securityInterfaceHolder.dataConverter.base64LengthOfInt()
        val cleanData = userId.substring(base64LengthOfInt, userId.length - base64LengthOfInt)
        return cleanData
    }


    /**
     * Data: |Username| + Username + |Key| + Key + DeviceId + OnlineId + |OnlineId|
     */
    fun getUserNameFromUserId(data: String) : String {
        val base64LengthOfInt = securityInterfaceHolder.dataConverter.base64LengthOfInt()
        val positionOfData = 0
        val userNameLength = securityInterfaceHolder.dataConverter.stringToInt(data.substring(positionOfData, base64LengthOfInt))
        val userId = decryptUserId(data)
        var userName = userId.substring(positionOfData, userNameLength)
        return userName
    }

    /**
     * Data: |Username| + Username + |Key| + Key + DeviceId + OnlineId + |OnlineId|
     */
    fun getOnlineIdFromUserId(data: String) : String {
        val base64LengthOfInt = securityInterfaceHolder.dataConverter.base64LengthOfInt()
        val positionOfData = data.length - base64LengthOfInt
        val onlineIdLength = securityInterfaceHolder.dataConverter.stringToInt(data.substring(positionOfData, data.length))
        val userId = decryptUserId(data)
        val onlineIdWithPrefix = userId.substring(userId.length - onlineIdLength, userId.length)
        // Prefix needed so the online device can still determine if it is a valid onlineId from a message.
        return onlineIdWithPrefix
    }

    fun getRandomUserId(
        context: Context,
        currentUser: CurrentUser = storage.currentUser)
    : Pair<String, DecryptionKeyCloakForUser>  {
        val randomSymmetricKey = securityInterfaceHolder.randomHelper
            .computeRandomHashWithSpecificLength(securityInterfaceHolder.symmetricEncryptionHelper.KEY_SIZE)
        val kp: KeyPair = securityInterfaceHolder.asymmetricEncryptionHelper.getKeyPair()
        val randomAssymmetricKey = OfflineKeyPair(kp.public, kp.private)

        val encryptionKeyCloakForUser = EncryptionKeyCloakForUser()
        encryptionKeyCloakForUser.encryptionKey = randomAssymmetricKey.publicKey
        encryptionKeyCloakForUser.symmetricKey = randomSymmetricKey
        encryptionKeyCloakForUser.signedKey = currentUser.signKey.privateKey

        val startIv = securityInterfaceHolder.randomHelper
            .computeRandomHashWithSpecificLength(securityInterfaceHolder.symmetricEncryptionHelper.SEED_SIZE)

        val decryptionKeyCloakForUser = DecryptionKeyCloakForUser()
        decryptionKeyCloakForUser.decryptionKey = randomAssymmetricKey.privateKey
        decryptionKeyCloakForUser.symmetricKey = randomSymmetricKey
        decryptionKeyCloakForUser.initialToken = startIv

        val deviceUserId = ForeignUserIdSerializer(securityInterfaceHolder, storage)
            .serializeUserId(
                currentUser.name,
                currentUser.hash,
                encryptionKeyCloakForUser,
                startIv
            )
        return Pair(deviceUserId, decryptionKeyCloakForUser)
    }

    fun createCurrentUserData(
        userName: String,
        fullOnlineUserId: String,
        context: Context,
        callback: ((incrementProgess: Int) -> Unit)? = null)
    {
        // Create own user hash.
        DataGeneratorHelper(securityInterfaceHolder)
            .storeFullOnlineUserDataForOfflineDevice(fullOnlineUserId, storage)

        val deviceId = generateDeviceId(context)
        storage.setUserId(deviceId)

        callback?.invoke(10)
        val userHash = storage.onlineUserId!!
        val ownUser = CurrentUser(userName, userHash)
        ownUser.originalName = userName
        ownUser.createdAtTimestamp = IOHelper.getCurrentDateAsUnixTimestamp()

        callback?.invoke(10)

        // Create sign key.
        val kp: KeyPair = securityInterfaceHolder.asymmetricEncryptionHelper.getKeyPair()
        ownUser.signKey = OfflineKeyPair(kp.public, kp.private)
        callback?.invoke(30)

        // Create user id.
        val userIdTuple = getRandomUserId(context, ownUser)
        val userId = userIdTuple.first
        val relatedKeys = userIdTuple.second

        callback?.invoke(40)
        ownUser.decryptionKeyCloakForUser = relatedKeys

        // Create Symmetric key for himself.
        setKeysForSimpleDialog(userId, ownUser)
        callback?.invoke(10)

        ownUser.userSecurityIssues.addAll(UserSecurity.getAllByContext(context, storage.readSettings()))

        storage.addDialog(ownUser)
        callback?.invoke(10)
    }

    private fun createOnlineGroupOnlineId(firebaseIds: List<String>): String {
        // Sort so we cannot create multiple groups with same users by accident.
        return firebaseIds.sorted().joinToString(DataSecurityHelper.ONLINE_GROUP_ONLINE_ID_SEPARATOR)
    }

    private fun createOfflineGroupOnlineId(): String {
        return DataSecurityHelper.NOTIFICATION_ID_PREFIX_GROUP_OFFLINE + securityInterfaceHolder.randomHelper.getRandomUUIDv4()
    }

    fun createAndStoreOfflineGroup(groupName: String, context: Context) {
        val group = createOfflineGroup(groupName, context)
        // Save only if the user doesnt exists yet.
        try {
            storage.addDialog(group)
        } catch (e: IllegalArgumentException) {
            Log.e("DataSecurityHelper", "There was an error creating the group user.", e)
            DialogHelper.showDialog(context, "Error", "Could not create group. Unique id is already used.")
        }
    }

    fun createOfflineGroup(groupName: String, context: Context): OfflineGroup {
        val fakeOnlineId = createOfflineGroupOnlineId()
        val group = OfflineGroup(groupName, fakeOnlineId)

        var deviceId =  securityInterfaceHolder.randomHelper.getRandomUUIDv4()
        // Append key.
        val constantKeys = ConstantKeys(context, securityInterfaceHolder.asymmetricEncryptionHelper)
        val randomIndex = SecureRandom().nextInt(constantKeys.keyMap.size)
        val stringSignKey = KeyConverter().convertToDatabaseValue(constantKeys.keyMap.entries.toMutableList()[randomIndex].value.private)

        val randomIndexEncryption = SecureRandom().nextInt(constantKeys.keyMap.size)
        val stringEncryptionKey = constantKeys.keyMap.keys.toMutableList()[randomIndexEncryption]

        val randomSymmetricKey = securityInterfaceHolder.randomHelper
            .computeRandomHashWithSpecificLength(securityInterfaceHolder.symmetricEncryptionHelper.KEY_SIZE)
        val startIv = securityInterfaceHolder.randomHelper
            .computeRandomHashWithSpecificLength(securityInterfaceHolder.symmetricEncryptionHelper.SEED_SIZE)

        deviceId = stringSignKey + deviceId
        deviceId = securityInterfaceHolder.dataConverter.intToString(stringSignKey!!.length) + deviceId
        // Append ids.
        deviceId = groupName + deviceId
        deviceId = securityInterfaceHolder.dataConverter.intToString(groupName.length) + deviceId

        val stringPublicKey = stringEncryptionKey
        deviceId += stringPublicKey
        deviceId += securityInterfaceHolder.dataConverter.intToString(stringPublicKey.length)

        deviceId += randomSymmetricKey
        deviceId += securityInterfaceHolder.dataConverter.intToString(randomSymmetricKey.length)

        deviceId += startIv
        deviceId += securityInterfaceHolder.dataConverter.intToString(startIv.length)

        deviceId += fakeOnlineId
        deviceId += securityInterfaceHolder.dataConverter.intToString(fakeOnlineId.length)


        // All data stored. Now Encrypt
        deviceId = encryptUserId(deviceId)

        group.groupId = deviceId
        setKeysForSimpleDialog(deviceId, group)

        val keyCloak = DecryptionKeyCloakForUser().apply {
            decryptionKey = group.extractPrivateKey(context, securityInterfaceHolder)
            symmetricKey = randomSymmetricKey
            initialToken = startIv
        }
        group.decryptionKeyCloakForUser = keyCloak

        group.userSecurityIssues.add(UserSecurity.OFFLINE_GROUP)

        return group
    }


    // TODO: what is the maximum of firebaseIds? At least for qrcodes there is a limit...
    fun createOnlineGroup(groupName: String, firebaseIds: List<String>, context: Context) {
        val joinedOnlineIds = createOnlineGroupOnlineId(firebaseIds)
        val group = OnlineGroup(groupName, joinedOnlineIds)

        var deviceId = securityInterfaceHolder.randomHelper.getRandomUUIDv4()

        // TODO: For online group we would need only the firebaseIds with the name. But would make scanning
        //  and extracting of name and firebaseId(s) differently. So everything else is trash..
        // Append key.
        val constantKeys = ConstantKeys(context, securityInterfaceHolder.asymmetricEncryptionHelper)
        val randomIndex = SecureRandom().nextInt(constantKeys.keyMap.size)
        val stringKey = constantKeys.keyMap.keys.toMutableList()[randomIndex]

        deviceId = stringKey + deviceId
        deviceId = securityInterfaceHolder.dataConverter.intToString(stringKey.length) + deviceId
        // Append ids.
        deviceId = groupName + deviceId
        deviceId += joinedOnlineIds
        // All data stored. Now Encrypt
        deviceId = encryptUserId(deviceId)
        // Append length.
        deviceId = securityInterfaceHolder.dataConverter.intToString(groupName.length) + deviceId
        deviceId += securityInterfaceHolder.dataConverter.intToString(joinedOnlineIds.length)

        group.groupId = deviceId
        group.firebaseEmailIds = firebaseIds
        group.updateSecurityIssues()

        // Save only if the user doesnt exists yet.
        try {
            storage.addDialog(group)
        } catch (e: IllegalArgumentException) {
            Log.e("DataSecurityHelper", "There was an error creating the group user.", e)
            DialogHelper.showDialog(context, "Error", "Could not create group. Unique id is already used.")
        }
    }

    fun createABaseDialogFromUserId(userId: String, context: Context) : BaseDialog {
        val userName = getUserNameFromUserId(userId)
        val onlineId = getOnlineIdFromUserId(userId)

        // TODO: Heavy Tasks. do async..

        if (DataSecurityHelper(securityInterfaceHolder).isOnlineIdForOnlineGroup(onlineId)) {
            val onlineGroup = OnlineGroup(userName, onlineId)
            onlineGroup.groupId = userId
            onlineGroup.createdAtTimestamp = IOHelper.getCurrentDateAsUnixTimestamp()
            onlineGroup.firebaseEmailIds = ArrayList(onlineId.split(
                DataSecurityHelper.ONLINE_GROUP_ONLINE_ID_SEPARATOR))
            onlineGroup.showWarningForUnknownMembersIfNeeded(context)
            onlineGroup.updateSecurityIssues()
            return onlineGroup
        }

        val dialog: SimpleDialog
        val isOfflineGroup = onlineId.startsWith(DataSecurityHelper.NOTIFICATION_ID_PREFIX_GROUP_OFFLINE)
        if (isOfflineGroup) {
            dialog = OfflineGroup(userName, onlineId)
            dialog.groupId = userId

        }  else { // Else it is a simple user.
            dialog = User(userName, onlineId)
        }
        setKeysForSimpleDialog(userId, dialog)
        dialog.originalName = userName
        dialog.createdAtTimestamp = IOHelper.getCurrentDateAsUnixTimestamp()

        if (dialog is OfflineGroup) {
            val deserializer = ForeignUserIdSerializer(securityInterfaceHolder, storage)
            deserializer.deserializeUserId(userId)
            val keyCloak = DecryptionKeyCloakForUser()
            keyCloak.decryptionKey = dialog.extractPrivateKey(context, securityInterfaceHolder)
            keyCloak.symmetricKey = deserializer.symmetricKey
            keyCloak.initialToken = deserializer.iv
            dialog.decryptionKeyCloakForUser = keyCloak
            dialog.userSecurityIssues.add(UserSecurity.OFFLINE_GROUP)
        }

        return dialog
    }

    private fun setKeysForSimpleDialog (userId: String, simpleDialog: SimpleDialog) {
        val deserializer = ForeignUserIdSerializer(securityInterfaceHolder, storage)
        deserializer.deserializeUserId(userId)
        val iv = deserializer.iv

        simpleDialog.newestReceivedToken = iv
        simpleDialog.previousReceivedToken = iv
        // Here we add the token for a foreign user.
        simpleDialog.pushGeneratedToken(iv)

        val privateKey = KeyConverter().convertToEntityProperty(deserializer.stringSignKey) as PrivateKey

        val encryptionKeyCloakForUser = EncryptionKeyCloakForUser()
        encryptionKeyCloakForUser.encryptionKey = KeyConverter()
            .convertToEntityProperty(deserializer.stringPublicKey) as PublicKey
        encryptionKeyCloakForUser.symmetricKey = deserializer.symmetricKey
        encryptionKeyCloakForUser.signedKey = privateKey

        simpleDialog.encryptionKeyCloakForUser = encryptionKeyCloakForUser
    }


    private fun uniqueIdPossible(context: Context) : Boolean {
        // TODO: Move tests to isntrumented test with activity context...
        // For Tests there is no Activity context.
        if (context as? Activity == null) {
            return false
        }

        // TODO: Wait fir user aggreement
        if ( ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(context, arrayOf(android.Manifest.permission.READ_PHONE_STATE), PERMISSION_READ_STATE)
            return false
        }

        // TODO: Handle device id properly https://stackoverflow.com/a/60653814/8524651
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return false
        }
        return true
    }

    private fun encryptUserId(userId: String) : String {
        // TODO: Encrypt user id. Not really necessary but nice too have..
        return userId
    }

}