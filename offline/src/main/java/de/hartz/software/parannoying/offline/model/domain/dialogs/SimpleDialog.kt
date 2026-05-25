package de.hartz.software.parannoying.offline.model.domain.dialogs

import android.util.Log
import de.hartz.software.parannoying.offline.model.domain.DecryptionKeyCloakForUser
import de.hartz.software.parannoying.offline.model.domain.EncryptionKeyCloakForUser


abstract class SimpleDialog(
        nickname: String,
        hash: String // OnlineId or fake onlineId starting with prefix
    ): BaseDialog(nickname, hash) {



    /**
     * This is one of the generated userId by this device. And will be assigned as soon as we get
     * a message first time from this user so we know which userId got scanned.
     */
    var decryptionKeyCloakForUser: DecryptionKeyCloakForUser? = null

    /**
     * The scanned userId of the other device.
     */
    lateinit var encryptionKeyCloakForUser: EncryptionKeyCloakForUser

    /**
     * The token that the other user (represented by this class) randomly generated and send via message.
     * It is the exact same token as the last received messages metadata.newToken.
     * It will be the IV for encrypting on this device when sending the next Message for this user.
     *
     * NOTICE: IVs cannot be determined by messages.metaData as they might not be stored.
     */
    var newestReceivedToken: String = ""

    /**
     * Stores the previous value of newestReceivedToken to check if there were any messages inbetween.
     */
    var previousReceivedToken: String = ""

    // TODO: update persistence layer
    // TODO: When one device gets hijacked and the whole communication is present, it is possible to decrypt all SEND messages
    // TODO: Probably storing this token will resolve the current problem (12.10.2021) that one device can send any amount of messages, while the sender is not able to send any message
    // This would give us the possibilty to reset the chat at any time to make communication work again at any time. (if anything works as expected it is not necessary as we only invalidate tokens when they are confirmed...)
    var initialToken: String = ""

    var unconfirmedGeneratedSendTokensForDecryption: ArrayList<String> // ivs for decrypting messages of this user on this device.

    init {
        unconfirmedGeneratedSendTokensForDecryption = ArrayList()
    }

    // Wrapper functions to ensure the last generated token is on top.
    fun pushGeneratedToken (newToken: String) {
        Log.e("TokenCheck", newToken)
        if (unconfirmedGeneratedSendTokensForDecryption.contains(newToken)) {
            throw RuntimeException("Duplicate token could lead to issues")
        }
        unconfirmedGeneratedSendTokensForDecryption.add(0, newToken)
    }

    // Wrapper functions to ensure the last generated token is on top.
    fun numberOfGeneratedTokens (): Int {
        return unconfirmedGeneratedSendTokensForDecryption.size
    }

    fun getGeneratedTokenAt (index: Int): String?  {
        if (index >= unconfirmedGeneratedSendTokensForDecryption.size) {
            return null
        }
        return unconfirmedGeneratedSendTokensForDecryption[index]
    }

}