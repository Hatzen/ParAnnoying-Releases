package de.hartz.software.parannoying.core.helper.io

import android.content.Context
import android.net.Uri
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

// https://stackoverflow.com/a/6472872/8524651
// TODO: Can we use a more effective compression?
// https://github.com/hzy3774/AndroidP7zip
// https://peazip.github.io/maximum-compression-benchmark.html#:~:text=What%20is%20the%20overall%20best,for%20any%20other%20tested%20format.
class ZipUtil {
    /**
     * Zip function zip all files and folders
     */
    fun zipFiles(srcFolder: String, destZipFile: String): Boolean {
        var result = false
        try {
            println("Program Start zipping the given files")
            /*
             * send to the zip procedure
             */
            zipFolder(srcFolder, destZipFile)
            result = true
            println("Given files are successfully zipped")
        } catch (e: Exception) {
            println("Some Errors happned during the zip process")
        } finally {
            return result
        }
    }

    fun createZipFromUris(context: Context, uris: List<Uri>, outputZipUri: Uri) {
        val contentResolver = context.contentResolver

        // Open output stream for the ZIP file
        contentResolver.openOutputStream(outputZipUri)?.use { outputStream ->
            ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
                for (uri in uris) {
                    // Get the file name from the URI (you can adjust how you retrieve it)
                    val fileName = getFileNameFromUri(context, uri) ?: "file_${uris.indexOf(uri)}"

                    // Open input stream for the current URI
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        val buffer = ByteArray(4096)
                        var length: Int

                        // Create a new ZIP entry
                        val zipEntry = ZipEntry(fileName)
                        zipOut.putNextEntry(zipEntry)

                        // Copy data from the input stream into the ZIP entry
                        while (inputStream.read(buffer).also { length = it } > 0) {
                            zipOut.write(buffer, 0, length)
                        }

                        zipOut.closeEntry()
                    } ?: run {
                        // Optional: handle the case where input stream is null
                        println("Could not open input stream for URI: $uri")
                    }
                }
            }
        } ?: throw IOException("Could not open output stream for ZIP URI: $outputZipUri")
    }

    // Helper function to extract a filename from a URI
    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var name: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndexOpenableColumnsDisplayName()
                if (nameIndex >= 0) {
                    name = it.getString(nameIndex)
                }
            }
        }
        if (name == null) {
            name = uri.lastPathSegment?.substringAfterLast('/')
        }
        return name
    }

    // Helper extension function to get column index safely
    private fun android.database.Cursor.getColumnIndexOpenableColumnsDisplayName(): Int {
        return try {
            getColumnIndexOrThrow(android.provider.OpenableColumns.DISPLAY_NAME)
        } catch (e: IllegalArgumentException) {
            -1
        }
    }


    /*
     * zip the folders
     */
    @Throws(Exception::class)
    private fun zipFolder(srcFolder: String, destZipFile: String) {
        var zip: ZipOutputStream? = null
        var fileWriter: FileOutputStream? = null
        /*
         * create the output stream to zip file result
         */
        fileWriter = FileOutputStream(destZipFile)
        zip = ZipOutputStream(fileWriter)
        /*
         * add the folder to the zip
         */
        addFolderToZip("", srcFolder, zip)
        /*
         * close the zip objects
         */
        zip.flush()
        zip.close()
    }

    /*
     * recursively add files to the zip files
     */
    @Throws(Exception::class)
    private fun addFileToZip(path: String, srcFile: String, zip: ZipOutputStream, flag: Boolean) {
        /*
         * create the file object for inputs
         */
        val folder = File(srcFile)

        /*
         * if the folder is empty add empty folder to the Zip file
         */
        if (flag == true) {
            zip.putNextEntry(ZipEntry(path + "/" + folder.name + "/"))
        } else { /*
                 * if the current name is directory, recursively traverse it
                 * to get the files
                 */
            if (folder.isDirectory) {
                /*
                 * if folder is not empty
                 */
                addFolderToZip(path, srcFile, zip)
            } else {
                /*
                 * write the file to the output
                 */
                val buf = ByteArray(1024)
                var len: Int
                val `in` = FileInputStream(srcFile)
                zip.putNextEntry(ZipEntry(path + "/" + folder.name))
                while ((`in`.read(buf).also { len = it }) > 0) {
                    /*
                     * Write the Result
                     */
                    zip.write(buf, 0, len)
                }
            }
        }
    }

    /*
     * add folder to the zip file
     */
    @Throws(Exception::class)
    private fun addFolderToZip(path: String, srcFolder: String, zip: ZipOutputStream) {
        val folder = File(srcFolder)

        /*
         * check the empty folder
         */
        if (folder.list().size == 0) {
            println(folder.name)
            addFileToZip(path, srcFolder, zip, true)
        } else {
            /*
             * list the files in the folder
             */
            for (fileName in folder.list()) {
                if (path == "") {
                    addFileToZip(folder.name, "$srcFolder/$fileName", zip, false)
                } else {
                    addFileToZip(path + "/" + folder.name, "$srcFolder/$fileName", zip, false)
                }
            }
        }
    }
}