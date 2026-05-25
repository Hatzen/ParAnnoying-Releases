package de.hartz.software.parannoying.app.large.tests.utils

import android.util.Log
import de.hartz.software.parannoying.app.App
import de.hartz.software.parannoying.core.helper.InitializationHelper
import de.hartz.software.parannoying.core.interfaces.di.StorageInterface
import de.hartz.software.parannoying.core.model.domain.DeviceRole
import de.hartz.software.parannoying.core.model.persistence.realm.RealmHelper
import de.hartz.software.parannoying.online.adapters.FirebaseAdapter
import de.hartz.software.parannoying.online.model.OnlineStorage
import io.mockk.mockk
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class DeviceUtil(val app: App) {

    lateinit var storage: StorageInterface<*, *>

    private val devices = HashSet<String>()

    var currentDevice: Pair<String, DeviceRole>? = null
        private set

    fun addDevice(deviceName: String) {
        devices.add(deviceName)
        currentDevice = Pair(deviceName, DeviceRole(DeviceRole.OFFLINE))
        val file = getFileForCurrentDevice()
        file.createNewFile()
        file.deleteOnExit()
        initCurrentDevice()
        currentDevice = Pair(deviceName, DeviceRole(DeviceRole.ONLINE))
        val file2 = getFileForCurrentDevice()
        file2.createNewFile()
        file2.deleteOnExit()
        initCurrentDevice()
    }

    @Synchronized
    fun switchTo(deviceName: String, deviceRole: DeviceRole) {
        if(!devices.contains(deviceName)) {
            throw IllegalArgumentException("Device $deviceName not added so far..")
        }
        if (currentDevice != null) {
            saveData()
        }
        closeCurrentSession()
        currentDevice = Pair(deviceName, deviceRole)
        initCurrentDevice()
    }

    fun saveData() {
        exportData(getFileForCurrentDevice())
    }

    fun clearFirebaseId() {
        /*
        // TODO: We dont get the id like this anymore..
        https://stackoverflow.com/a/68285224/8524651
        if (FirebaseInstanceId.getInstance().instanceId.isComplete) {
            FirebaseInstanceId.getInstance().deleteInstanceId()
        }
        */
        // Delete old instanceId otherwise registering will timeout. Maybe this deletes the original apps notificationId
        FirebaseAdapter(mockk<OnlineStorage>()).resetCurrentNotificationId()
    }

    fun cleanUpAll() {
        // TODO: Clear all files which might be created even only temporary ones..

        // Wont work and crash app directly.. Maybe just because of close activity..
        // StorageInterface.deleteAll(app)

        closeCurrentSession()
        devices.forEach {
                currentDevice = Pair(it, DeviceRole(DeviceRole.OFFLINE))
                initCurrentDevice()
                closeCurrentSession()
                getFileForCurrentDevice().delete()

                currentDevice = Pair(it, DeviceRole(DeviceRole.ONLINE))
                initCurrentDevice()
                closeCurrentSession()
                getFileForCurrentDevice().delete()
            }
        devices.clear()
        currentDevice = null
    }

    fun uploadAllDeviceData() {
        val uploader = DevelopmentFileUploader()
        try {
            // set faster timeout as casually we would wait 60 seconds per test..
            val latch = CountDownLatch(1)

            // TODO: Like this we dont get the exception anymore.. But latch should lead to exception..
            // TODO:
            Thread {
                // Test if the server is running. which might not be the case when running single tests..
                uploader.test()
                latch.countDown()
            }.start()

            latch.await(5, TimeUnit.SECONDS)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        devices.forEach {
                currentDevice = Pair(it, DeviceRole(DeviceRole.OFFLINE))
                uploader.uploadFile(getFileForCurrentDevice(), getRealmFileName())
                currentDevice = Pair(it, DeviceRole(DeviceRole.ONLINE))
                uploader.uploadFile(getFileForCurrentDevice(), getRealmFileName())
            }
    }

    private fun closeCurrentSession() {
        storage.realmHelper.deleteRealmAndCloseCurrentSession()
    }

    private fun getFileForCurrentDevice(): File {
        var deviceRole = "online"
        if (currentDevice!!.second.roleId == DeviceRole.OFFLINE) {
            deviceRole = "offline"
        }
        return File(app.filesDir, "annoying-Data-device" + currentDevice!!.first + "-" + deviceRole + ".txt")
    }

    private fun getRealmFileName(): String {
        return RealmHelper.getFileNameForIdAndRole(currentDevice!!.first, currentDevice!!.second)
    }

    private fun initCurrentDevice() {
        importData(getFileForCurrentDevice())
        val fileName = getRealmFileName()
        InitializationHelper.setDeviceRole(app, fileName)
        Log.e(javaClass.simpleName, "initialized storage for $fileName")
        app.invalidateComponents()
        storage = app.Storage
    }

    // Import / export: https://medium.com/glucosio-project/example-class-to-export-import-a-realm-database-on-android-c429ade2b4ed
    @Throws(IOException::class)
    private fun exportData(dest: File) {
        // if backup file already exists, delete it
        dest.delete()

        // copy current realm to backup file
        val realm = storage.realmHelper.getThreadInstance()
        realm.writeCopyTo(dest)
        realm.close()
    }

    private fun importData(src: File) {
        val IMPORT_REALM_FILE_NAME = getRealmFileName()
        val TAG = "E2E Test"
        copyBundledRealmFile(src,  IMPORT_REALM_FILE_NAME)
        Log.d(TAG, "Data restore is done")
    }

    @Throws(IOException::class)
    private fun copyBundledRealmFile(src: File, outFileName: String): String {
        val file = File(app.filesDir, outFileName)
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
    }

}