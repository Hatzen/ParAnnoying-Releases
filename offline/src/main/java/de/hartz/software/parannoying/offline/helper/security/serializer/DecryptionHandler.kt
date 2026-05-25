package de.hartz.software.parannoying.offline.helper.security.serializer

import android.content.Context
import android.util.Log
import de.hartz.software.parannoying.core.helper.security.DataSecurityHelper
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.offline.helper.security.serializer.file.FileDeserializerImpl
import de.hartz.software.parannoying.offline.helper.security.serializer.message.UserMessageDeserializerImpl
import de.hartz.software.parannoying.offline.model.OfflineStorage
import de.hartz.software.parannoying.offline.model.domain.DecryptionKeyCloakForUser
import de.hartz.software.parannoying.offline.model.domain.dialogs.SimpleDialog
import de.hartz.software.parannoying.offline.model.domain.dialogs.User
import de.hartz.software.parannoying.offline.model.domain.messages.AbstractMessage
import de.hartz.software.parannoying.offline.model.domain.messages.FileMessage
import de.hartz.software.parannoying.offline.model.domain.messages.UserMessage
import de.hartz.software.parannoying.offline.model.exceptions.UserNotFoundException

class DecryptionHandler(val context: Context, val securityInterfaceHolder: SecurityInterfaceHolder, val Storage: OfflineStorage) {

    fun getFileMessageAndUser(rawMessage: String):  Pair<FileMessage, SimpleDialog> {
        val dataSecurityHelper = DataSecurityHelper(securityInterfaceHolder)
        return getMessageAndUser(rawMessage, FileDeserializerImpl(securityInterfaceHolder, dataSecurityHelper))
    }

    fun getUserMessageAndUser(rawMessage: String):  Pair<UserMessage, SimpleDialog> {
        return getMessageAndUser(rawMessage, UserMessageDeserializerImpl(context, securityInterfaceHolder))
    }

    private fun <T: AbstractMessage> getMessageAndUser(rawMessage: String, deserializer: AbstractDeserializer<T>): Pair<T, SimpleDialog> {
        var unconfirmedKeys = Storage.readUnconfirmedKeySet()

        // TODO: Why is this needed?
        /*
        unconfirmedKeys.removeAll {
            it.initialToken == null
        }
         */
        unconfirmedKeys = unconfirmedKeys.filter {
            it.initialToken != null
        }.toSet()

        // TODO: This wont work, at least offline groups never are virgin users.. and need different source user..
        val possibleSenders = listOf<SimpleDialog>(*Storage.users.toTypedArray(), *Storage.offlineGroups.toTypedArray())

        var user: SimpleDialog
        var message: T
        val maxUnconfirmedTokens = possibleSenders.maxOf { it.numberOfGeneratedTokens() }
        // This might be a performance issue as soon as there are many users and encryption iterations increase
        // unconfirmed tokens should be on top so usually the outer loop is just run once.
        for (i in 0..maxUnconfirmedTokens) {
            for (anyUser in possibleSenders) {
                // TODO: better use foreach streams with filter.
                if (anyUser is User && anyUser.isUnconfirmedUser()) {
                    continue
                }
                try {
                    val tokenToTest = anyUser.getGeneratedTokenAt(i)
                            ?: continue // User does not have so many tokens
                    message = deserializer.decryptMessage(rawMessage, anyUser, tokenToTest)
                    user = anyUser
                    // user.confirmGeneratedToken(tokenToTest)// TODO: Is this already replaced by generateCombinedTokenForUserAndConfirmTokens
                    return Pair(message, user)
                } catch (e: Exception) {
                    Log.v("addMessage", e.localizedMessage, e)
                } // Not correct user
                // TODO: Catch different exceptions and notify user. If it is an self owned exception it is useful for the user.
            }
        }


        // VirginUsers without any message received.
        unconfirmedKeys.forEach {
            val tokenToTest = it.initialToken!!
            decrypt(rawMessage, it, tokenToTest, deserializer).let {
                if (it != null) {
                    return@getMessageAndUser it
                }
            }
        }

        // VirginUsers with already received messages.
        for (i in 0..maxUnconfirmedTokens) {
            for (anyUser in Storage.users) {
                // TODO: better use foreach streams with filter.
                if (!anyUser.isUnconfirmedUser()) {
                    continue
                }

                unconfirmedKeys.forEach {
                    val tokenToTest = anyUser.getGeneratedTokenAt(i)
                            ?: return@forEach  // User does not have so many tokens

                    decrypt(rawMessage, it, tokenToTest, deserializer).let {
                        if (it != null) {
                            return@getMessageAndUser it
                        }
                    }
                }


            }
        }
        throw UserNotFoundException()
    }

    private fun <T: AbstractMessage> decrypt(rawMessage: String, key: DecryptionKeyCloakForUser, tokenToTest: String, deserializer: AbstractDeserializer<T>): Pair<T, User>? {
        val userMessage: Pair<T, SimpleDialog>
        try {
            userMessage = deserializer.decryptMessage(rawMessage, key, tokenToTest)
        } catch (e: Exception) {
            Log.v(javaClass.simpleName, "Unconfirmed token didnt match. " + key, e)
            return null
        }
        val sender = userMessage.second
        sender.decryptionKeyCloakForUser = key
        Log.e(javaClass.simpleName, "VirginUsers without any message received: found working token" + key.initialToken )
        // TODO: analyse if this is really needed.. Seems to be only a Problem with OnlineGroups for some reason..
        // TODO: maybe we should set this initialtoken in user as well to be able to reset communication
        // sender.pushGeneratedToken(key.initialToken!!)
        key.initialToken = null

        return userMessage as Pair<T, User>
    }
}