package de.hartz.software.parannoying.offline.helper.security.serializer.file

import android.util.Base64
import android.util.Log
import de.hartz.software.parannoying.core.helper.io.FileHelper
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.helper.security.DataSecurityHelper
import de.hartz.software.parannoying.core.helper.security.DataSecurityHelper.Companion.FILE_CHUNK_SEPERATOR
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.offline.helper.security.serializer.AbstractDeserializer
import de.hartz.software.parannoying.offline.helper.security.serializer.SourceUserDeterminer
import de.hartz.software.parannoying.offline.model.domain.DecryptionKeyCloakForUser
import de.hartz.software.parannoying.offline.model.domain.dialogs.SimpleDialog
import de.hartz.software.parannoying.offline.model.domain.messages.FileMessage
import de.hartz.software.parannoying.offline.model.domain.messages.FileMetaData
import java.io.File
import java.io.FileInputStream
import java.util.Scanner

class FileDeserializerImpl(val securityInterfaceHolder: SecurityInterfaceHolder, val dataSecurityHelper: DataSecurityHelper): AbstractDeserializer<FileMessage>() {

    override fun decryptMessage(inputfile: String, sourceUser: SimpleDialog, symmetricToken: String): FileMessage {
        return FileDeserializer(securityInterfaceHolder, dataSecurityHelper).decryptFile(inputfile, sourceUser, symmetricToken)
    }

    override fun decryptMessage(inputfile: String, keys: DecryptionKeyCloakForUser, symmetricToken: String): Pair<FileMessage, SimpleDialog> {
        val message = FileDeserializer(securityInterfaceHolder, dataSecurityHelper).decryptFile(inputfile, keys, symmetricToken)
        return Pair(message, message.sender!!)
    }
}

// TODO: We should use cachedir only and delete it when launching app etc. context.cacheDir.deleteRecursviely
class FileDeserializer(val securityInterfaceHolder: SecurityInterfaceHolder, val dataSecurityHelper: DataSecurityHelper) {

    val FILE_SUFFIX = ".txt"
    val ivSize = securityInterfaceHolder.symmetricEncryptionHelper.SEED_SIZE

    fun decryptFile(inputfile: String, dialog: SimpleDialog, token: String): FileMessage {
        if (!testUserForFile(inputfile, dialog, token)) {
            throw RuntimeException("User wont fit for file decryption.")
        }
        val message = decryptFile(inputfile, dialog.decryptionKeyCloakForUser!!, token)
        message.sender = dialog
        return message
    }

    private fun testUserForFile(inputfile: String, dialog: SimpleDialog, token: String): Boolean {
        val decrypted: String
        try {
            decrypted = test(inputfile, dialog.decryptionKeyCloakForUser!!, token)
        } catch (e: Exception) {
            Log.v(javaClass.simpleName, "Test for " + dialog.originalName + " and token " + token + " failed", e)
            return false
        }
        val metaData = FileMetaData.getFromString(securityInterfaceHolder.dataConverter, decrypted)
        if (metaData.sourcehash == dialog.hash) {
            return true
        }
        // TODO: when receiving messages from offlnine group the hash check wont work: metaData.sourcehash == dialog.hash
        return dialog.hash.contains(DataSecurityHelper.NOTIFICATION_ID_PREFIX_GROUP_OFFLINE) // false when not offline groups.
    }

    private fun testUserForFile(inputfile: String, decryptionKeyCloakForUser: DecryptionKeyCloakForUser, token: String): Boolean {
        val decrypted: String
        try {
            decrypted = test(inputfile, decryptionKeyCloakForUser, token)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    private fun test(inputfile: String, decryptionKeyCloakForUser: DecryptionKeyCloakForUser, token: String): String {
        val file = File(inputfile)
        val stream = FileInputStream(file)
        return getPrefix(stream, decryptionKeyCloakForUser, token).decrypted
    }

    private fun getPrefix(stream: FileInputStream, decryptionKeyCloakForUser: DecryptionKeyCloakForUser, token: String): DecryptedAndIv {
        val scanner = Scanner(stream)
        scanner.useDelimiter(FILE_CHUNK_SEPERATOR)

        val prefix = scanner.next()

        val cleanedPrefix = securityInterfaceHolder.hardcodedEncryptionHelper.decrypt(prefix)
        val randomIv = cleanedPrefix.substring(0, ivSize)
        val combinedIv =  securityInterfaceHolder.hashHelper.hashWithSpecificLength(token, randomIv, length = ivSize)
        val targetOnlineId = dataSecurityHelper.getLastStringFromUserId(cleanedPrefix)
        val cleanedPrefixWithoutOnlineId = dataSecurityHelper.removeLastString(cleanedPrefix.substring(ivSize))

        // analogues to UserMessageSerializer
        Log.e(javaClass.simpleName, "Use Tokens for decryption \n" +
                "newestToken: " + token + "\n" +
                "randomIv: " + randomIv + "\n" +
                "combinedIv: " + combinedIv + "\n" +
                "symmetric key " + decryptionKeyCloakForUser.symmetricKey + "\n" +
                "assymetric key decryption " + decryptionKeyCloakForUser.decryptionKey
        )

        val decrypted = securityInterfaceHolder.symmetricEncryptionHelper
                .decrypt(cleanedPrefixWithoutOnlineId, decryptionKeyCloakForUser.symmetricKey, combinedIv)!!
        return DecryptedAndIv(decrypted, combinedIv)
    }

    fun decryptFile(inputfile: String, decryptionKeyCloakForUser: DecryptionKeyCloakForUser, token: String): FileMessage {
        // Will be called twice for user decryption..
        if (!testUserForFile(inputfile, decryptionKeyCloakForUser, token)) {
            throw RuntimeException("User wont fit for file decryption.")
        }
        val file = File(inputfile)
        val stream = FileInputStream(file)
        var scanner = Scanner(stream)
        scanner.useDelimiter(FILE_CHUNK_SEPERATOR)

        val prefix = getPrefix(stream, decryptionKeyCloakForUser, token)
        val fullIv = prefix.iv
        val metaData = FileMetaData.getFromString(securityInterfaceHolder.dataConverter, prefix.decrypted)

        val result = FileMessage()
        result.fileMetaData = metaData
        result.createdAtTimestamp = IOHelper.getCurrentDateAsUnixTimestamp()

        val sourceUser = SourceUserDeterminer.determine(null, metaData.sourcehash)
        result.sender = sourceUser

        val output = File.createTempFile("decryptedFile", FILE_SUFFIX)
        result.filePath = output.absolutePath

        var textBuffer = mutableListOf<String>()
        var crossSum = 0

        // reset scanner as the pointer is not correctly set afteer getPrefix for some reason.
        scanner = Scanner(FileInputStream(file))
        scanner.useDelimiter(FILE_CHUNK_SEPERATOR)
        val skipped = scanner.next() // Skip prefix.

        var counter = 1
        var hmac = StringBuilder()
        while(scanner.hasNext()) {
            Log.v(javaClass.simpleName, "Processing chunk $counter")
            counter++
            for (i in 1..FileSerializer.SHUFFELD_CHUNKS_SIZE) {
                if (!scanner.hasNext()) {
                    break
                }
                val chunk = scanner.next()
                val toDecrypt = securityInterfaceHolder.hardcodedEncryptionHelper.decrypt(chunk)
                val decryptedSecond = securityInterfaceHolder.symmetricEncryptionHelper
                        .decrypt(toDecrypt, decryptionKeyCloakForUser.symmetricKey, fullIv)!!
                val decrypted = securityInterfaceHolder.asymmetricEncryptionHelper
                    .decrypt(decryptedSecond, decryptionKeyCloakForUser.decryptionKey)!!

                textBuffer.add(decrypted)
            }

            val fullText = textBuffer.joinToString("")
            val unshuffeldText = securityInterfaceHolder.hardcodedEncryptionHelper.decrypt(fullText)
            val unshuffeledChunks = FileHelper.getChunks(unshuffeldText)
            textBuffer.clear()

            for (chunk in unshuffeledChunks) {
                val content = Base64.decode(chunk, Base64.DEFAULT)
                hmac.append(content)
                output.appendBytes(content)
            }

            if (!scanner.hasNext()) {
                break
            }
        }

        val hmacResult = securityInterfaceHolder.hashHelper.hashWithSpecificLength(hmac.toString(), length = FileSerializer.HMAC_LENGTH)
        if (hmacResult != metaData.hmac) {
            throw RuntimeException("Message seems to be modified, hmac not matching.")
        }

        return result
    }

}

data class DecryptedAndIv(val decrypted: String, val iv: String)