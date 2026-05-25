package de.hartz.software.parannoying.offline.helper

import android.app.Activity
import android.content.Intent
import android.util.Base64
import android.util.Log
import de.hartz.software.parannoying.core.extensions.launchActivity
import de.hartz.software.parannoying.core.helper.InitializationHelper
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.core.interfaces.AbstractApp
import de.hartz.software.parannoying.core.model.domain.DeviceRole
import de.hartz.software.parannoying.core.model.persistence.realm.RealmHelper
import de.hartz.software.parannoying.offline.activities.offline.BaseOfflineActivity
import de.hartz.software.parannoying.offline.activities.offline.OfflineMainActivity
import de.hartz.software.parannoying.offline.model.OfflineStorage
import de.hartz.software.parannoying.offline.model.domain.messages.FileMessage
import java.io.*
import java.nio.charset.Charset

// Import / export: https://medium.com/glucosio-project/example-class-to-export-import-a-realm-database-on-android-c429ade2b4ed
class ImportExportHelper(val securityInterfaceHolder: SecurityInterfaceHolder, val realmHelper: RealmHelper) {

    fun exportWithRequestUserPassword(activity: BaseOfflineActivity, callback: () -> Unit) {
        val dialogCallback = object: DialogHelper.InputDialogCallback() {
            override fun onFinish(input: String) {
                val hash = securityInterfaceHolder.hashHelper.hash(input)
                startExport(activity, hash, activity.Storage)
                callback()
            }

            override fun onCancel() {
                super.onCancel()
                callback()
            }
        }
        DialogHelper.showPasswordDialog(activity, "Enter a password for encrypting the export file", dialogCallback)
    }

    fun startExport (activity: Activity, password: String, offlineStorage: OfflineStorage) {
        try {
            // create a backup file // TODO: better use secure internal storage?
            val time = System.currentTimeMillis()
            val exportRealmFile = File(IOHelper.getBackupDir(activity), "exp.raw")
            exportRealmFile.parentFile?.mkdirs()


            // This seem to crash on emulator
            /*
                E  /tmp/realm-java/realm/realm-library/src/main/cpp/realm-core/src/realm/util/file.cpp:1101: [realm-core-13.13.0] Assertion failed: r == 0 && "File::unlock()" with (r, (*__errno())) =  [-1, 38]
                   <backtrace not supported on this platform>
                   !!! IMPORTANT: Please report this at https://github.com/realm/realm-core/issues/new/choose
                A  Fatal signal 6 (SIGABRT), code -1 (SI_QUEUE) in tid 22404 (parannoying.dev), pid 22404 (parannoying.dev)
             */
            if(IOHelper.isEmulator()) {
                DialogHelper.showAlert(activity, "Export doesnt seem to work on emulator. Maybe wrong..")
                return
            }
            // copy current realm to backup file
            val realm = realmHelper.getThreadInstance()
            realm.writeCopyTo(exportRealmFile)
            realm.close()

            val media = offlineStorage.getDialogs()
                .filterIsInstance<FileMessage>()
                .map { File(it.filePath) }

            val mediaFile = File(IOHelper.getBackupDir(activity), "media.zip")
            // if media file already exists, delete it
            mediaFile.delete()
            IOHelper.zipFiles(media, mediaFile)

            val encryptedExportFile = File(IOHelper.getBackupDir(activity), "exp-$time.raw")
            encryptedExportFile.parentFile?.mkdirs()

            val completeZip = File(IOHelper.getBackupDir(activity), "complete.zip")
            completeZip.delete()

            IOHelper.zipFiles(listOf<File>(mediaFile, exportRealmFile), completeZip)

            val key = securityInterfaceHolder.hashHelper
                .hashWithSpecificLength(password, length = securityInterfaceHolder.symmetricEncryptionHelper.KEY_SIZE)

            createEncryptedExportFile(completeZip, encryptedExportFile, key , realmHelper.password!!)

            exportRealmFile.delete()
            completeZip.delete()
            mediaFile.delete()
        } catch (e: java.lang.Exception) {
            UiHelper.showToastFromBackgroundTask(activity, "Failed to export data.")
            Log.e(javaClass.simpleName, "Failed to export data.", e)
        }
    }

    fun importWithRequestUserPassword(src: File, activity: Activity) {
        val callback = object: DialogHelper.InputDialogCallback() {
            override fun onFinish(input: String) {
                startImportExportedFile(src, input, activity)
            }
        }
        DialogHelper.showInputDialog(activity, "Enter the password for decrypting the import file", true, callback)
    }

    // not private only for test so we dont have to enter the dialog.
    fun startImportExportedFile(src: File, userPassword: String, activity: Activity) {
        val hash = securityInterfaceHolder.hashHelper.hash(userPassword)
        val key = securityInterfaceHolder.hashHelper
            .hashWithSpecificLength(hash, length = securityInterfaceHolder.symmetricEncryptionHelper.SEED_SIZE)

        val inputStream: InputStream = src.inputStream()
        val inputString = inputStream.bufferedReader().use { it.readText() }

        var decrypted = securityInterfaceHolder.hardcodedEncryptionHelper.decrypt(inputString)
        decrypted = securityInterfaceHolder.symmetricEncryptionHelper.decrypt(decrypted, key)!!

        val lengthOfKey = securityInterfaceHolder.dataConverter
            .stringToInt(decrypted.substring(0, securityInterfaceHolder.dataConverter.base64LengthOfInt()))
        val realmRandomPassword = decrypted.substring(decrypted.length - lengthOfKey)

        val dataStartIndex = securityInterfaceHolder.dataConverter.base64LengthOfInt()
        val dataEndIndex = decrypted.length - lengthOfKey
        val data = decrypted.substring(dataStartIndex, dataEndIndex)

        realmHelper.loadPassword(activity, realmRandomPassword)
        val result = Base64.decode(data, Base64.DEFAULT)

        val EXPORT_REALM_PATH = activity.filesDir.absolutePath
        val EXPORT_REALM_FILE_NAME = "/exp.raw"
        // create a backup file
        val exportRealmFile = File(EXPORT_REALM_PATH, EXPORT_REALM_FILE_NAME)
        // if backup file already exists, delete it
        exportRealmFile.delete()
        saveFile(result, exportRealmFile)

        val completeZip = File(IOHelper.getBackupDir(activity), "complete")
        IOHelper.unzip(exportRealmFile, completeZip)

        val realmExport = IOHelper.getFirstFileWithExtension(IOHelper.getBackupDir(activity).absoluteFile, "raw")

        val mediaFiles = IOHelper.getFirstFileWithExtension(IOHelper.getBackupDir(activity).absoluteFile, "zip")!!
        val importedFiles = File(IOHelper.getBackupDir(activity), "media-import")
        IOHelper.unzip(mediaFiles, importedFiles)
        // TODO: Move files to specific location and change filepath of file message

        startImportPlainRealmFile(realmExport!!, activity)

        exportRealmFile.delete()
        completeZip.delete()
    }

    /*
    Only used for tests to change between simulated devices.
    No need for setting different passwords and the like.
     */
    fun startImportWithoutLaunchingMainActivity (src: File, activity: Activity)  {
        realmHelper.deleteRealmAndCloseCurrentSession()

        val realRealmFileName = RealmHelper.getFileNameForIdAndRole(
            deviceRole = DeviceRole(DeviceRole.OFFLINE)
        )
        InitializationHelper.setDeviceRole(activity, realRealmFileName)
        InitializationHelper.setInitialized(activity)
        val IMPORT_REALM_FILE_NAME = realRealmFileName
        copyBundledRealmFile(activity, src,  IMPORT_REALM_FILE_NAME)

        (activity.application as AbstractApp).initApp()
        Log.d(javaClass.simpleName, "Data restore is done")
    }

    /**
     * Used to Import dummy files and last step of real imports.
     */
    fun startImportPlainRealmFile (src: File, activity: Activity) {
        startImportWithoutLaunchingMainActivity(src, activity)

        val deleteHistory : (userIntent : Intent) -> Unit = {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        activity.launchActivity<OfflineMainActivity>(init = deleteHistory)
    }

    private fun createEncryptedExportFile (source: File, destination: File, key: String, realmKey: String) {
        val rawBytes = readFile(source)
        var content = String(Base64.encode(rawBytes, Base64.DEFAULT), Charset.forName("UTF-8"))
        content = securityInterfaceHolder.dataConverter.intToString(realmKey.length)  + content + realmKey

        // TODO: Do chunkwise and overall use file encryption with constant keys?
        var encryptedContent = securityInterfaceHolder.symmetricEncryptionHelper.encrypt(content, key)!!
        encryptedContent = securityInterfaceHolder.hardcodedEncryptionHelper.encrypt(encryptedContent)
        destination.writeText(encryptedContent)
    }

    private fun copyBundledRealmFile(activity: Activity, src: File, outFileName: String): String? {
        try {
            val file = File(activity.getApplicationContext().getFilesDir(), outFileName)
            val outputStream = FileOutputStream(file)
            val inputStream = FileInputStream(src)
            val buf = ByteArray(1024)
            var bytesRead: Int
            while (inputStream.read(buf).also { bytesRead = it } > 0) {
                outputStream.write(buf, 0, bytesRead)
            }
            outputStream.close()
            inputStream.close()
            return file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(Exception::class)
    private fun saveFile(fileData: ByteArray, file: File) {
        val bos = BufferedOutputStream(FileOutputStream(file, false))
        bos.write(fileData)
        bos.flush()
        bos.close()
    }

    @Throws(Exception::class)
    private fun readFile(file: File): ByteArray {
        val fileContents = file.readBytes()
        val inputBuffer = BufferedInputStream(
            FileInputStream(file)
        )

        inputBuffer.read(fileContents)
        inputBuffer.close()

        return fileContents
    }
}