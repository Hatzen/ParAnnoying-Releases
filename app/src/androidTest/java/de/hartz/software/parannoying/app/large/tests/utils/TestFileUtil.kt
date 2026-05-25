package de.hartz.software.parannoying.app.large.tests.utils

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import de.hartz.software.parannoying.core.model.domain.DeviceRole
import java.io.File
import java.io.FileOutputStream

object TestFileUtil {

    fun getTestFile(activity: Context, largeFile: Boolean = false): File {
        val testFileName = if (largeFile)
                "Big_Buck_Bunny_1080_10s_30MB.mp4"
            else
                "cosmos-carl-sagan.gif"
        return getExampleFile(testFileName, activity)
    }

    fun getFile(deviceId: String, deviceRole: DeviceRole, activity: Context): File {
        val role = if(deviceRole.roleId == DeviceRole.OFFLINE) {
            "offline"
        } else {
            "online"
        }
        val fileName = "deviceData-" + role + "-" + deviceId
        val newTmpFile = getFile(fileName, activity)
        return newTmpFile
    }

    private fun getExampleFile(fileName: String, activity: Context): File {
        // TODO Unify with WelcomeImportBackupFragment test imports.
        val path = "e2e/files/example/"
        return getFile(fileName, activity, path)
    }

    // TODO Unify with WelcomeImportBackupFragment test imports.
    private fun getFile(fileName: String, activity: Context, path: String = "e2e/files/"): File {
        val inputstream = InstrumentationRegistry.getInstrumentation().context.resources.assets.open(path + fileName)

        val newTmpFile = File(activity.filesDir, fileName + ".txt")

        val outputStream = FileOutputStream(newTmpFile)
        val inputStream = inputstream
        val buf = ByteArray(1024)
        var bytesRead: Int
        while (inputStream.read(buf).also { bytesRead = it } > 0) {
            outputStream.write(buf, 0, bytesRead)
        }
        outputStream.close()
        return newTmpFile
    }
}