package de.hartz.software.parannoying.offline.model.domain.messages

import java.io.File
import java.util.zip.ZipFile
import kotlin.math.round

class FileMessage : AbstractMessage() {

    lateinit var fileMetaData: FileMetaData
    lateinit var filePath: String

    val numberOfFiles: Int by lazy {
        val file = File(filePath)
        val fileExtension = file.extension.uppercase()

        if (fileExtension == "ZIP") {
            val zipFile = ZipFile(file)
            val number = zipFile.entries().asSequence().count()
            zipFile.close()
            return@lazy number
        }
        return@lazy 1
    }
    val messageContentTypes: Set<MessageContentType> by lazy {
        val file = File(filePath)
        val fileExtension = file.extension.uppercase()
        val result = setOf<MessageContentType>()

        /*
        if (fileExtension == "ZIP") {
            val zipFile = ZipFile(file)
            val zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                val fileName = zipEntries.nextElement().name
                val extension = File(fileName).extension
                result.plus(getExtension(extension))
            }
            zipFile.close()
            return@lazy result
        }
         */
        return@lazy result.plus(getExtension(fileExtension))
    }

    val fileSize: Long by lazy {
        val file = File(filePath)
        return@lazy file.length()
    }

    val fileSizeText: String by lazy {
        val size =  round(fileSize / 1024.0f / 1024.0f * 100) / 100
        return@lazy "" + File(filePath).name + " - " + size + " MB"
    }

    fun getExtension(fileExtension: String): MessageContentType {
        if (fileExtension == "ZIP" || fileExtension == "PDF") {
            return MessageContentType.CONTENT_TYPE_SIMPLE_FILE
        } else if (fileExtension == "JPG" || fileExtension == "PNG") {
            return MessageContentType.CONTENT_TYPE_IMAGE
        } else if (fileExtension == "MP4" || fileExtension == "AVI" || fileExtension == "GIF") {
            return MessageContentType.CONTENT_TYPE_VIDEO
        }
        return MessageContentType.CONTENT_TYPE_SIMPLE_FILE
    }

    override var metaData: MetaData get() = fileMetaData.metaData
        set(value) { // will be called by mapper..
        }

    override fun getText(): String {
        return fileMetaData.fileName
    }
}
