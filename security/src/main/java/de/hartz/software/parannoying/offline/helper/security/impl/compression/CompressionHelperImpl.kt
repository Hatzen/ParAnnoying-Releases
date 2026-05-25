package de.hartz.software.parannoying.offline.helper.security.impl.compression

import android.util.Base64
import de.hartz.software.parannoying.core.interfaces.di.security.CompressionHelper
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.Charset
import java.util.zip.DataFormatException
import java.util.zip.Deflater
import java.util.zip.Inflater
import javax.inject.Inject

class CompressionHelperImpl @Inject constructor(): CompressionHelper {

    @Throws(IOException::class)
    override fun compress(data: ByteArray): String {
        // TODO: #90 inefficient compress with base64
        val deflater = Deflater(9, true)
        deflater.setInput(data)
        val outputStream = ByteArrayOutputStream(data.size)
        deflater.finish()
        val buffer = ByteArray(1024)
        while (!deflater.finished()) {
            val count = deflater.deflate(buffer) // returns the generated code... index
            outputStream.write(buffer, 0, count)
        }
        outputStream.close()
        val output = outputStream.toByteArray()
        deflater.end()
        return String(Base64.encode(output, Base64.DEFAULT), Charset.forName("UTF-8"))
    }

    @Throws(IOException::class, DataFormatException::class)
    override fun decompress(data: String): ByteArray {
        val  result = Base64.decode(data, Base64.DEFAULT)
        val inflater = Inflater(true)
        inflater.setInput(result)
        val outputStream = ByteArrayOutputStream(result.size)
        val buffer = ByteArray(1024)
        while (!inflater.finished()) {
            val count = inflater.inflate(buffer)
            outputStream.write(buffer, 0, count)
        }
        outputStream.close()
        inflater.end()
        val output = outputStream.toByteArray()
        return output
    }
}