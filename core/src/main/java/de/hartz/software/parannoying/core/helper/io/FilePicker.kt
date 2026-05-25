package de.hartz.software.parannoying.core.helper.io

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import java.io.File
import java.io.IOException

class FilePicker() {

    private var callback: ((Uri?) -> Unit)? = null
    private lateinit var launcher: ActivityResultLauncher<Array<String>>

    fun register(fragment: Fragment) {
        launcher = fragment.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            callback?.invoke(uri)
        }
    }

    fun pick(mimeTypes: Array<String>, result: (Uri?) -> Unit) {
        callback = result
        launcher.launch(mimeTypes)
    }


    fun copyUriToTempFile(context: Context, uri: Uri): File {
        val fileName = queryFileName(context, uri) ?: "tempfile"
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Unable to open input stream for URI: $uri")
        val tempFile = File.createTempFile("prefix_", fileName, context.cacheDir)
        tempFile.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        return tempFile
    }

    fun queryFileName(context: Context, uri: Uri): String? {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        returnCursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst()) {
                return it.getString(nameIndex)
            }
        }
        return null
    }
}