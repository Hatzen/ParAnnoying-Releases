package de.hartz.software.parannoying.air.gap.helpers

import android.util.Base64
import android.util.Log
import de.hartz.software.parannoying.air.gap.extensions.chunkedSequence
import de.hartz.software.parannoying.air.gap.model.ExchangeDataWrapperImpl
import de.hartz.software.parannoying.core.helper.security.DataSecurityHelper
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ExchangeDataWrapper
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.Charset


class DatasetProcessor() {
    companion object {
        val DATASET_SEPERATOR = "||__||"
        val CLEARTEXT_FILE_SEPERATOR = "|$%|__|%$|"
        val FILE_SUFFIX = "txt"
        val RESULT_EXTRA_FILE_NAME = "FILE_NAME"
        val CASUAL_SEPERATOR_LIST = listOf(
                DataSecurityHelper.FILE_CHUNK_SEPERATOR, DATASET_SEPERATOR, CLEARTEXT_FILE_SEPERATOR)
    }


    private val DUMMY_DATA = ExchangeDataWrapperImpl("DUMMY_MESSAGE", true)
    private var currentFile: File? = null
    private var wrapperCount = 0L

    var textChunkCount = 0L


    fun readFileChunks(filePath: String): Sequence<String> {
        textChunkCount = 0
        val iterator = DatasetChunkIterator(filePath,
                CASUAL_SEPERATOR_LIST
                )
        return iterator.asSequence()
                .map {
                    textChunkCount++
                    it.chunkData + it.delimiter
                }
    }

    fun readFile(filePath: String): Sequence<ExchangeDataWrapper> {
        val iterator = DatasetChunkIterator(filePath,
                CASUAL_SEPERATOR_LIST)

        val resultFlow =
            iterator.asSequence()
                .map {
                    textChunkCount++
                    mapResult(it)
                }
                .filter { DUMMY_DATA != it }
        return resultFlow
    }

    internal fun writeFile(dataList: List<ExchangeDataWrapper>): File {
        textChunkCount = 0
        val output = File.createTempFile("air-gap-send-file", FILE_SUFFIX)

        val fo = FileOutputStream(output)

        dataList.forEach {
            val inputStream: Sequence<String> = if (it.isFile) {
                val isChunked = File(it.filePath)
                        .readLines()
                        .find { it.contains(DataSecurityHelper.FILE_CHUNK_SEPERATOR) } != null
                if (isChunked) {
                    File(it.filePath)
                        .readLines()
                        .asSequence()
                        .map {
                            textChunkCount++
                            it
                        }
                } else {
                    // TODO: find an efficient chunksize. 10 makes huge qrcodes..
                    val CHUNK_SIZE_RANDOM = 5
                    File(it.filePath)
                        .chunkedSequence(DataSecurityHelper.MAX_MESSAGE_SIZE * CHUNK_SIZE_RANDOM)
                        .map {
                            String(
                                Base64.encode(it, Base64.DEFAULT)
                            , Charset.forName("UTF-8")
                        ) }
                        .map {
                            textChunkCount++
                            it.plus(CLEARTEXT_FILE_SEPERATOR)
                        }
                }
            } else {
                sequenceOf(it.exchangeData)
            }
            val chunk = inputStream.plus(DATASET_SEPERATOR)
            textChunkCount++

            val writer = OutputStreamWriter(fo, "UTF-8")
            chunk.forEach {
                writer.write(it)
            }
            writer.flush();
        }
        fo.close()
        return output
    }

    private fun mapResult(it: ChunkResult) : ExchangeDataWrapperImpl {
        var result = DUMMY_DATA
        val isFileChunk = it.delimiter == DataSecurityHelper.FILE_CHUNK_SEPERATOR
        val isCleartextFileChunk = it.delimiter == CLEARTEXT_FILE_SEPERATOR
        val data = it.chunkData
        if (isFileChunk) {
            if (currentFile == null) {
                currentFile = File.createTempFile("air-gap-file", FILE_SUFFIX)
                result = ExchangeDataWrapperImpl(currentFile!!.absolutePath , true)
            }
            currentFile!!.appendText(data + DataSecurityHelper.FILE_CHUNK_SEPERATOR)
        } else if (isCleartextFileChunk) {
            if (currentFile == null) {
                currentFile = File.createTempFile("air-gap-cleartext-file", FILE_SUFFIX)
                result = ExchangeDataWrapperImpl(currentFile!!.absolutePath, true)
            }
            currentFile!!.appendBytes(Base64.decode(data, Base64.DEFAULT))
        } else {
            currentFile = null
            // Might be empty when fileseperator is before dataset seperator.
            if (data.isNotBlank()) {
                result = ExchangeDataWrapperImpl(data)
            }
        }
        wrapperCount++
        Log.v(javaClass.simpleName, "load $wrapperCount: $data")
        return result
    }

}