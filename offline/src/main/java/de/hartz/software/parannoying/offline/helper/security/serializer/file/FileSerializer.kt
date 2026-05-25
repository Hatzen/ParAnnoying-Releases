package de.hartz.software.parannoying.offline.helper.security.serializer.file

import android.util.Base64
import android.util.Log
import de.hartz.software.parannoying.core.helper.io.FileHelper
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.helper.security.DataSecurityHelper
import de.hartz.software.parannoying.core.helper.security.DataSecurityHelper.Companion.FILE_CHUNK_SEPERATOR
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.offline.model.OfflineStorage
import de.hartz.software.parannoying.offline.model.domain.dialogs.SimpleDialog
import de.hartz.software.parannoying.offline.model.domain.messages.FileMetaData
import de.hartz.software.parannoying.offline.model.domain.messages.MetaData
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.SequenceInputStream
import java.nio.charset.Charset
import javax.inject.Inject

// TODO: We should use cachedir only and delete it when launching app etc. context.cacheDir.deleteRecursviely
class FileSerializer @Inject constructor(val securityInterfaceHolder: SecurityInterfaceHolder)  {

    companion object {
        val HMAC_LENGTH = 10
        val SHUFFELD_CHUNKS_SIZE = 4
        val METADATA_SEPERATOR = "_-_"
        val FILE_SUFFIX = ".txt"
    }

    fun encryptFile(inputfile: String, dialog: SimpleDialog, metaData: MetaData): File {
        val tmpOutput = File.createTempFile("tmpEncryptedFile", FILE_SUFFIX)
        tmpOutput.deleteOnExit()

        val inputFile = File(inputfile)

        val numberOfBytesEncryptable = getBuffersizeForChunk()
        val ivSize = securityInterfaceHolder.symmetricEncryptionHelper.SEED_SIZE
        val randomIv = securityInterfaceHolder.randomHelper.computeRandomHashWithSpecificLength(ivSize)
        val combinedIv = securityInterfaceHolder.hashHelper
            .hashWithSpecificLength(randomIv, dialog.newestReceivedToken, length = ivSize)

        Log.e(javaClass.simpleName, "Use Tokens for encryption \n" +
                "newestToken: " + dialog.newestReceivedToken + "\n" +
                "randomIv: " + randomIv + "\n" +
                "combinedIv/for encryption  " + combinedIv + "\n" +
                "symmetric key " + dialog.encryptionKeyCloakForUser.symmetricKey + "\n" +
                "assymetric key encryption " + dialog.encryptionKeyCloakForUser.encryptionKey + "\n" +
                "assymetric key decryption " + dialog.decryptionKeyCloakForUser?.decryptionKey
        )

        val fi = FileInputStream(inputFile)
        val origin = BufferedInputStream(fi, numberOfBytesEncryptable)
        var count: Int
        var buffer = ByteArray(numberOfBytesEncryptable)

        // TODO: Better use StringBuilder?
        var textBuffer = mutableListOf<String>()
        var counter = 0
        var hmac = StringBuilder()
        while (true) {
            counter++
            var lastChunk = false
            for (i in 1..SHUFFELD_CHUNKS_SIZE) {
                origin.read(buffer, 0, numberOfBytesEncryptable).also { count = it } != -1
                lastChunk = count != numberOfBytesEncryptable
                if (lastChunk) {
                    // TODO: why is this working?? we overwrite buffer content after we read the real byte count..
                    // TODO: Why is count negativ when encrypting a zip file?
                    buffer = ByteArray(count)
                }
                val content = String(Base64.encode(buffer, Base64.DEFAULT), Charset.forName("UTF-8"))
                textBuffer.add(content)
                hmac.append(content) // securityInterfaceHolder.hmacHelper.getHMACForMessage(content)

                if (lastChunk) {
                    break
                }
            }

            val fullText = textBuffer.joinToString("")
            val shuffeldData = securityInterfaceHolder.hardcodedEncryptionHelper.encrypt(fullText)
            val shuffeledChunks = FileHelper.getChunks(shuffeldData)
            textBuffer.clear()

            for (chunk in shuffeledChunks) {
                val encrypted = securityInterfaceHolder.asymmetricEncryptionHelper
                    .encrypt(chunk, dialog.encryptionKeyCloakForUser.encryptionKey)!!
                val encryptedSecond = securityInterfaceHolder.symmetricEncryptionHelper
                    .encrypt(encrypted, dialog.encryptionKeyCloakForUser.symmetricKey, combinedIv)!!
                val salted = securityInterfaceHolder.hardcodedEncryptionHelper.encrypt(encryptedSecond)
                tmpOutput.appendText(salted + FILE_CHUNK_SEPERATOR)
            }
            if (lastChunk) {
                break
            }
        }

        val result = securityInterfaceHolder.hashHelper.hashWithSpecificLength(hmac.toString(), length =  HMAC_LENGTH)
        val currentDate = IOHelper.getCurrentDateAsUnixTimestamp()
        val prefix = FileMetaData(
            dialog.hash,
            OfflineStorage.INSTANCE.currentUser.hash,
            result,
            metaData,
            currentDate,
            inputFile.name
        ).string(securityInterfaceHolder.dataConverter)

        // Cant use asym encryption as header might be to long (filename etc.) is same as meta data of messages.
        val encryptedSecond = securityInterfaceHolder.symmetricEncryptionHelper
                .encrypt(prefix, dialog.encryptionKeyCloakForUser.symmetricKey, combinedIv)!!

        val ivEncryptedAndHash = randomIv + encryptedSecond + dialog.hash + securityInterfaceHolder.dataConverter.intToString(dialog.hash.length)

        val prefixTarget = securityInterfaceHolder.hardcodedEncryptionHelper.encrypt(ivEncryptedAndHash)

        val output = File.createTempFile("encryptedFile", FILE_SUFFIX)
        prefixFile(tmpOutput, output, prefixTarget + FILE_CHUNK_SEPERATOR)
        return output
    }

    private fun prefixFile(srcFile: File, destFile: File, text: String) {
        val fi = SequenceInputStream(
                text.toByteArray().inputStream(), FileInputStream(srcFile))
        val fo = FileOutputStream(destFile)

        fi.copyTo(fo)
        fi.close()
        fo.close()

    }

    /**
     *
    // https://stackoverflow.com/a/13378842/8524651

     * There are default allocation unit sizes for FAT32 under different storage capacities:

        64 MB or smaller: 512 bytes
        64 MB—128 MB: 1 KB
        128 MB—256 MB: 2 KB
        256 MB—8 GB: 4 KB
        8 GB—16 GB: 8 KB
        16 GB—32 GB: 16 KB
        32 GB—2 TB: 32 KB
     */
    private fun getBuffersizeForChunk(): Int {
        // Chunk size of file read, is max message size in base64
        return (DataSecurityHelper.MAX_MESSAGE_SIZE / 4 * 3)
    }
}