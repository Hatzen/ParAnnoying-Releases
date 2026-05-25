package de.hartz.software.parannoying.air.gap.helpers

import android.util.Log
import java.io.File
import java.io.IOException


internal class ChunkResult(val chunkData: String,val delimiter: String)
internal class DatasetChunkIterator(val fileName: String, val delimiters: List<String>) : Iterator<ChunkResult> {
    // callback: (ChunkResult)-> Unit,

    companion object {
        const val NO_DELIMITER = "__NO_VALID_DELIMITER__"
        private const val BUFFER_SIZE = 4096
        private const val INDEX_NOT_FOUND = -1
        private const val STRING_BUILDER_BUFFER_SIZE = 10_000
        private const val STRING_EMPTY = ""
    }

    private var hasNext = true
    private var closed = false

    private val file = File(fileName)
    private val filestream = file.inputStream()

    private var currentChunkBuffer = StringBuilder()
    private var count = -1
    private var buffer = ByteArray(Companion.BUFFER_SIZE)

    init {
        currentChunkBuffer.ensureCapacity(Companion.STRING_BUILDER_BUFFER_SIZE)
    }

    override fun hasNext(): Boolean {
        if (!closed) {
            read()
        }
        val currentChunkBufferAlreadyContainsFullDatasegment = delimiters.any { currentChunkBuffer.contains(it) }
        return hasNext || currentChunkBufferAlreadyContainsFullDatasegment
    }

    override fun next(): ChunkResult {
        try {
            while (hasNext()) {
                val delimiter = delimiters.minBy {
                    val index = currentChunkBuffer.indexOf(it)
                    if (index == Companion.INDEX_NOT_FOUND) {
                        return@minBy Int.MAX_VALUE
                    }
                    return@minBy index
                }
                val indexOf = currentChunkBuffer.indexOf(delimiter)
                // We parsed some data but didnt found a seperator so far. There must be one coming next read..
                if (indexOf == -1) {
                    continue
                }
                var result = currentChunkBuffer.substring(0, indexOf + delimiter.length)
                currentChunkBuffer = StringBuilder(currentChunkBuffer.removePrefix(result))
                currentChunkBuffer.ensureCapacity(Companion.STRING_BUILDER_BUFFER_SIZE)
                result = result.replace(delimiter, Companion.STRING_EMPTY)
                // callback(ChunkResult(result, delimiter))
                return ChunkResult(result, delimiter)
            }

            hasNext = false

            if (currentChunkBuffer.isNotEmpty()) {
                throw RuntimeException("There is some unprocessed data: '$currentChunkBuffer'")
            }
            return ChunkResult(currentChunkBuffer.toString(), NO_DELIMITER)
        } catch (e: Exception) {
            close()
            throw RuntimeException("Error while processing item.", e)
        }
    }

    private fun close() {
        try {
            closed = true
            filestream.close()
        } catch(exception: IOException){
            Log.d(javaClass.simpleName, "Error occured on addtional close.", exception)
        }
    }

    private fun read() {
        hasNext = filestream.read(buffer).also { count = it } != -1
        if (hasNext) {
            val lastChunk = count != Companion.BUFFER_SIZE
            if (lastChunk) {
                buffer = ByteArray(count, { buffer.get(it) })
                close()
                hasNext = false
            }
            currentChunkBuffer.append(String(buffer))
        }
    }


}