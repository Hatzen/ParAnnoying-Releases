package de.hartz.software.parannoying.core.helper.io

import android.util.Log
import de.hartz.software.parannoying.core.helper.security.DataSecurityHelper
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URLConnection
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


object FileHelper {

    // https://stackoverflow.com/a/17194696/8524651
    fun isFileProbablyTextFile(file: String): Boolean {
        val file = File(file)
        val `is`: InputStream = BufferedInputStream(FileInputStream(file))
        val mimeType = URLConnection.guessContentTypeFromStream(`is`)
        return mimeType.contains("text") // TODO: Check if this working
    }

    // https://www.techiedelight.com/split-string-into-fixed-length-chunks-java/
    fun getChunks(data: String): List<String> {
        val chunkSize = DataSecurityHelper.MAX_MESSAGE_SIZE
        val chunks = ArrayList<String>();
        for (i in data.indices step chunkSize) {
            chunks.add(data.substring(i, Math.min(data.length, i + chunkSize)));
        }
        return chunks
    }

    fun zip(files: Array<String>, zipFileName: String): File {
        try {
            val targetFile = File.createTempFile(zipFileName, ".zip")
            val zipper = ZipUtil()
            for (i in files.indices) {
                Log.v("Compress", "Adding: " + files[i])
                zipper.zipFiles(files[i], targetFile.canonicalPath)
            }
            return targetFile
        } catch (e: java.lang.Exception) {
            throw RuntimeException(e)
        }
    }

    // https://stackoverflow.com/questions/25562262/how-to-compress-files-into-zip-folder-in-android
    fun unzip(zipFile: String, targetLocation: String) {

        //create target location folder if not exist
        dirChecker(targetLocation)
        try {
            val fin = FileInputStream(zipFile)
            val zin = ZipInputStream(fin)
            var ze: ZipEntry? = null
            while (zin.nextEntry.also { ze = it } != null) {

                //create dir if required while unzipping
                if (ze!!.isDirectory) {
                    dirChecker(ze!!.name)
                } else {
                    val fout = FileOutputStream(targetLocation + ze!!.name)
                    var c = zin.read()
                    while (c != -1) {
                        fout.write(c)
                        c = zin.read()
                    }
                    zin.closeEntry()
                    fout.close()
                }
            }
            zin.close()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun dirChecker(dir: String) {
        val f = File(dir)
        if (!f.isDirectory) {
            f.mkdirs()
        }
    }
}