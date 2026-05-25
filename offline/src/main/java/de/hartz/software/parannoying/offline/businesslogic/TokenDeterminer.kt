package de.hartz.software.parannoying.offline.businesslogic

import android.util.Log
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.offline.model.domain.dialogs.OfflineGroup
import de.hartz.software.parannoying.offline.model.domain.dialogs.SimpleDialog
import de.hartz.software.parannoying.offline.model.domain.dialogs.User
import de.hartz.software.parannoying.offline.model.domain.messages.AbstractMessage
import de.hartz.software.parannoying.offline.model.domain.messages.MessageTokenWrapper

class TokenDeterminer(val securityInterfaceHolder: SecurityInterfaceHolder) {

    /**
     * Message to generate a combined token to be validated on other device, so the other user knows which tokens were send.
     * Same method used on in both directions so the token created is the same.
     *
     * TODO: Probably is currently easily broken when send and received is not in order.
     */
    fun generateCombinedTokenForUserAndConfirmTokens (
        user: SimpleDialog,
        messages: List<AbstractMessage>,
        verifyCombinedToken: String? = null
    ): String? {
        // Only consider foreign messages as tokens are initially the same and lead to wrong identification.
        //  Further, take only the last received messages until first own message as we assume messages get synced usually in order

        val listOfRelevantMessages = messages.reversed().takeWhile { it.sender == user}
        val listOfRelevantTokens = listOfRelevantMessages.map { it.metaData.newToken }
        if (listOfRelevantTokens.isEmpty()) {
            return null
        }
        val combinedToken = securityInterfaceHolder.hashHelper
            .hashWithMaxLength(
                *listOfRelevantTokens.toTypedArray(),
                length = securityInterfaceHolder.symmetricEncryptionHelper.SEED_SIZE
            )

        if (verifyCombinedToken != null) {
            if (combinedToken == verifyCombinedToken) {
                Log.e(javaClass.simpleName,
                    "Combined token confirming: " + combinedToken + " consisting of " + listOfRelevantTokens.size)

                listOfRelevantTokens.reversed().forEach { confirmGeneratedToken(user, it, messages) }
            } else {
                listOfRelevantMessages.forEach { it.messageTokenSkipped = true }
            }
        }
        return combinedToken
    }

    fun getTokensForMessageMetaData(user: SimpleDialog, messages: List<AbstractMessage>): MessageTokenWrapper {
        if (user is OfflineGroup) {
            val defaultToken = user.newestReceivedToken
            return MessageTokenWrapper(defaultToken, defaultToken, defaultToken)
        }
        val randomTokenSize = securityInterfaceHolder.symmetricEncryptionHelper.SEED_SIZE
        val randomToken = securityInterfaceHolder.randomHelper.computeRandomHashWithSpecificLength(randomTokenSize)
        user.pushGeneratedToken(randomToken)
        val newToken = user.getGeneratedTokenAt(0)!!
        // before init there were a new created token without clearing any. So unwrapping has to work.
        val previousToken = user.getGeneratedTokenAt(1)!!

        // determine all tokens that needs to be confirmed
        val tokenCheckSum = generateCombinedTokenForUserAndConfirmTokens(user, messages) ?: ""
        return MessageTokenWrapper(newToken, previousToken, tokenCheckSum)
    }

    // TODO: can be private but currently for test is not..
    fun confirmGeneratedToken (user: SimpleDialog, aToken: String, messages: List<AbstractMessage>) {
        val indexOfConfirmedToken = user.unconfirmedGeneratedSendTokensForDecryption.indexOf(aToken)
        if (indexOfConfirmedToken == -1) {
            Log.e(javaClass.simpleName, "Token for user " + user.nickname + " not found.")
            return
        }
        for (userMessage in messages.asReversed()) {
            // Maybe better check only self written messages
            if ((userMessage.sender as? User)?.isCurrentUser() == true
                && userMessage.metaData.newToken == aToken) {
                userMessage.messageConfirmed = true
                break // A token is only valid for one message
            }
        }

        val offsetForPreviousToken = 0 // We can set this to 0 as slice endindex is inclusive so the previous token is always the current token.
        Log.e(javaClass.simpleName, "Unconfirmed tokenssize (" + user.unconfirmedGeneratedSendTokensForDecryption.size + ") before confirming token " + aToken + " with current index " + indexOfConfirmedToken)

        val endIndex = indexOfConfirmedToken + offsetForPreviousToken
        val newunconfirmedTokens = ArrayList(user.unconfirmedGeneratedSendTokensForDecryption.slice(0..endIndex))
        val confirmedTokens = user.unconfirmedGeneratedSendTokensForDecryption.subList(endIndex, user.unconfirmedGeneratedSendTokensForDecryption.size)
        Log.e(javaClass.simpleName, "Removed tokens $confirmedTokens")

        // TODO: Testwise deactivate manipulating tokens for checking source of errors??
        user.unconfirmedGeneratedSendTokensForDecryption = newunconfirmedTokens
        // TODO: Is this correct, can we delete all these tokens before? The messages might get received in a different order??

        Log.e(javaClass.simpleName, "Unconfirmed tokenssize (" + user.unconfirmedGeneratedSendTokensForDecryption.size + ") after confirming token " + aToken + " with current index " + indexOfConfirmedToken)

        // TODO: This can lead to errors for communication with internet but keeps performance better.
        // It handles abandoned tokens which exist caused by sending multiple messages before receiving one.
        if (user.unconfirmedGeneratedSendTokensForDecryption.size > 100) {
            user.unconfirmedGeneratedSendTokensForDecryption = ArrayList(user.unconfirmedGeneratedSendTokensForDecryption.slice(0..100))
        }
    }
}