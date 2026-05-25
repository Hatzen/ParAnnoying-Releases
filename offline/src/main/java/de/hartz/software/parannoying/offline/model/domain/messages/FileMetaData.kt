package de.hartz.software.parannoying.offline.model.domain.messages

import de.hartz.software.parannoying.core.interfaces.di.security.DataConverter
import de.hartz.software.parannoying.offline.helper.security.serializer.file.FileSerializer.Companion.METADATA_SEPERATOR
import java.util.Scanner

class FileMetaData(
        var targetHash: String,
        var sourcehash: String,
        var hmac: String,
        var metaData: MetaData,
        var sendTimestamp: Long,
        var fileName: String
    ) {

    companion object {
        fun getFromString(dataConverter: DataConverter, prefix: String): FileMetaData {
            val scanner = Scanner(prefix)
            scanner.useDelimiter(METADATA_SEPERATOR)

            val targetHash = scanner.next()
            val hmac = scanner.next()
            val sourcehash = scanner.next()
            val metaData = MetaData()
            metaData.dataFromString(scanner.next())
            val sendTimestamp = dataConverter.stringToLong(scanner.next())
            val fileName = scanner.next()

            return FileMetaData(targetHash, sourcehash, hmac, metaData, sendTimestamp, fileName)
        }
    }

    fun string(dataConverter: DataConverter): String {
        var prefix = ""
        prefix += targetHash + METADATA_SEPERATOR
        prefix += hmac + METADATA_SEPERATOR
        prefix += sourcehash + METADATA_SEPERATOR
        prefix += metaData.toString() + METADATA_SEPERATOR
        prefix += dataConverter.longToString(sendTimestamp) + METADATA_SEPERATOR
        prefix += fileName + METADATA_SEPERATOR

        return prefix
    }

}