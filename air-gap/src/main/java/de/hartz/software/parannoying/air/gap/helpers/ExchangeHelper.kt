package de.hartz.software.parannoying.air.gap.helpers

import android.util.Log
import de.hartz.software.parannoying.air.gap.helpers.DatasetProcessor.Companion.DATASET_SEPERATOR
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import java.util.Scanner

class ExchangeHelper(val securityInterfaceHolder: SecurityInterfaceHolder) {

    companion object {
        val DELIMITER = "#"
        val DEFAULT_TOKEN = "MY-DEFAULT-Token"
    }

    fun getPreamble(token: String, messageCount: Int): String {
        var result = securityInterfaceHolder.hashHelper.hash(token)
        result += "$DELIMITER$messageCount"
        return securityInterfaceHolder.hardcodedEncryptionHelper.encrypt(result) + DATASET_SEPERATOR
    }

    fun isPreamble(receivedData: String, verifiedToken: String): Boolean {
        return try {
            getMessageCount(receivedData, verifiedToken) > 0
        } catch (e: Exception) {
            Log.v(javaClass.simpleName, "Testing Text for preamble didnt succeed", e)
            false
        }
    }

    fun getMessageCount(preamble: String, verifiedToken: String): Int {
        var cleaned = preamble
        DatasetProcessor.CASUAL_SEPERATOR_LIST.forEach {
            cleaned = cleaned.replace(it, "")
        }
        val cleartext = securityInterfaceHolder.hardcodedEncryptionHelper.decrypt(cleaned)
        val scanner = Scanner(cleartext)
        scanner.useDelimiter(DELIMITER)
        val hash = scanner.next()
        val expectedHash = securityInterfaceHolder.hashHelper.hash(verifiedToken)
        if (hash != expectedHash) {
            throw IllegalArgumentException()
        }
        val messageCount = scanner.nextInt()
        return messageCount
    }
}