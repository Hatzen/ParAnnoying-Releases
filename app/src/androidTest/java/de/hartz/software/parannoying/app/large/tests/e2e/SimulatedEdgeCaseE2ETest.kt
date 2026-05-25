package de.hartz.software.parannoying.app.large.tests.e2e

import android.util.Log
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import de.hartz.software.parannoying.app.large.tests.utils.TestFileUtil
import de.hartz.software.parannoying.core.model.domain.DeviceRole
import de.hartz.software.parannoying.core.model.persistence.realm.RealmHelper
import de.hartz.software.parannoying.offline.helper.ImportExportHelper
import de.hartz.software.parannoying.offline.model.RealmOfflinePersistenceConfiguration
import de.hartz.software.parannoying.online.adapters.FirebaseAdapter
import org.junit.Assume
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@LargeTest
@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
class SimulatedEdgeCaseE2ETest : AbstractE2ETest() {

    lateinit var onlineGroupOnlineId: String
    lateinit var offlineGroupOnlineId: String

    var isExportServerRunning = false

    @Ignore
    @Test
    fun generateOnboardingData() {
        doOnboarding()
        deviceUtil.uploadAllDeviceData()
    }

    // Successful 12.03.2025 within 15 min
    @Test
    // Realistic test for local hardware devices.
    fun testMultipleDevicesWithGroupsAndManyMessages() {
        Assume.assumeTrue("Using google api can be tested: ", FirebaseAdapter(onlineStorage).isGooglePlayServicesAvailable(context))
        isExportServerRunning = InstrumentationRegistry.getArguments().getBoolean("uploadTestFiles")

        // Overall test time 2,37 Min
        doOnboarding()
        // 1,36 Min // TODO: Failing since moving firebase to dev environment as users dont exist in dev database.
        // doOnboardingByFileForAllDevices()

        // Test sending messages

        val sendMessageFromAToB = { createAndSendMessageToUser(DEVICE_A_ID, ONLINE_ID_BY_DEVICE_ID[DEVICE_B_ID]!!, arrayListOf(DEVICE_B_ID)) }
        val sendMessageFromBToA = { createAndSendMessageToUser(DEVICE_B_ID, ONLINE_ID_BY_DEVICE_ID[DEVICE_A_ID]!!, arrayListOf(DEVICE_A_ID)) }

        sendMessageFromAToB() // TODO: User not found on device
        sendMessageFromBToA() // TODO: User not found?? => happend another time....
        sendMessageFromAToB() // TODO: User not found?? => happend one time.... same occured after 23 random messages when A wants to write with A. Most probably the token system is flacky.
        sendMessageFromAToB()
        sendMessageFromBToA()

        // Test File
        createAndSendFileToUser(DEVICE_B_ID, onlineGroupOnlineId, getListWithout(onlineGroupDeviceIds, DEVICE_B_ID))
        createAndSendFileToUser(DEVICE_C_ID, offlineGroupOnlineId, getListWithout(offlineGroupDeviceIds, DEVICE_B_ID))
        createAndSendFileToUser(DEVICE_A_ID, ONLINE_ID_BY_DEVICE_ID[DEVICE_B_ID]!!, arrayListOf(DEVICE_B_ID))
        // TODO: Serializing this 30mb file takes 10 mins in test leading to device dying usually as screen goes off..
        // createAndSendFileToUser(DEVICE_A_ID, ONLINE_ID_BY_DEVICE_ID[DEVICE_B_ID]!!, arrayListOf(DEVICE_B_ID), true)

        // Test sending online group messages
        createAndSendMessageToUser(DEVICE_B_ID, onlineGroupOnlineId, getListWithout(onlineGroupDeviceIds, DEVICE_B_ID))
        createAndSendMessageToUser(DEVICE_B_ID, onlineGroupOnlineId, getListWithout(onlineGroupDeviceIds, DEVICE_B_ID))
        createAndSendMessageToUser(DEVICE_C_ID, onlineGroupOnlineId, getListWithout(onlineGroupDeviceIds, DEVICE_C_ID))
        createAndSendMessageToUser(DEVICE_B_ID, onlineGroupOnlineId, getListWithout(onlineGroupDeviceIds, DEVICE_B_ID))
        createAndSendMessageToUser(DEVICE_A_ID, onlineGroupOnlineId, getListWithout(onlineGroupDeviceIds, DEVICE_A_ID))
        createAndSendMessageToUser(DEVICE_A_ID, onlineGroupOnlineId, getListWithout(onlineGroupDeviceIds, DEVICE_A_ID))

        // Test sending offline group messages
        createAndSendMessageToUser(DEVICE_C_ID, offlineGroupOnlineId, getListWithout(offlineGroupDeviceIds, DEVICE_B_ID))
        createAndSendMessageToUser(DEVICE_A_ID, offlineGroupOnlineId, getListWithout(offlineGroupDeviceIds, DEVICE_A_ID))

        writeRandomMessages()

        // TODO: Do some "irregular" stuff
        // 1. send message which never got received/ confirmed inbetween
        // 2. Receive some messages with online device (general issue we dont import the file for online device)
        // 3. Send some messages with offline device but keep them for sync (for testdata purpose)
        // 4. Device which has got messages but online device didnt go online. So messages will received by notification?

        // Manually enable this when server is running as the test doesnt work currently.
        if (isExportServerRunning) {
            deviceUtil.uploadAllDeviceData()
            Log.e(javaClass.simpleName, "Uploaded test data")
        } else {
            Log.e(javaClass.simpleName, "Testserver not running NOT Uploading test data")
        }
    }

    private fun writeRandomMessages() {
        for (i in 1..numberOfMostMessages) {
            val deviceSenderId = onlineGroupDeviceIds.random()
            val onlineIdReceiver: String
            val listOfDeviceIds: List<String>
            when(Random.nextInt(1, 4)) {
                1, 2 -> {
                    val deviceReceiverId = onlineGroupDeviceIds.random()
                    onlineIdReceiver = ONLINE_ID_BY_DEVICE_ID[deviceReceiverId]!!
                    listOfDeviceIds = arrayListOf(deviceReceiverId)
                }
                3 -> {
                    onlineIdReceiver = onlineGroupOnlineId
                    listOfDeviceIds = getListWithout(onlineGroupDeviceIds, deviceSenderId)
                }
                else -> {
                    onlineIdReceiver = offlineGroupOnlineId
                    listOfDeviceIds = getListWithout(offlineGroupDeviceIds, deviceSenderId)
                }
            }
            Log.e(this::class.java.simpleName, "Test $i creating message from deviceSenderId $deviceSenderId to $onlineIdReceiver for devices $listOfDeviceIds")
            createAndSendMessageToUser(deviceSenderId, onlineIdReceiver, listOfDeviceIds)
        }
    }

    private fun doOnboarding() {
        onboardingDevice(DEVICE_A_ID)
        onboardingDevice(DEVICE_B_ID)
        onboardingDevice(DEVICE_C_ID)
        onboardingDevice(DEVICE_D_ID)

        // Connect and share the devices and groups.
        connectDevice(DEVICE_A_ID, DEVICE_B_ID)
        connectDevice(DEVICE_A_ID, DEVICE_C_ID)

        connectDevice(DEVICE_B_ID, DEVICE_A_ID)
        connectDevice(DEVICE_B_ID, DEVICE_C_ID)

        connectDevice(DEVICE_C_ID, DEVICE_A_ID)
        connectDevice(DEVICE_C_ID, DEVICE_B_ID)
        connectDevice(DEVICE_C_ID, DEVICE_D_ID)

        connectDevice(DEVICE_D_ID, DEVICE_C_ID)
        connectDevice(DEVICE_D_ID, DEVICE_B_ID) // This device can only communicate D => B

        switchTo(chatOverviewRule, DEVICE_A_ID)
        val onlineGroupId = createOnlineGroup("ONLINE_GROUP_NAME", onlineGroupDeviceIds)
        onlineGroupOnlineId = dialogCreationHelper.getOnlineIdFromUserId(onlineGroupId)
        // getListWithout(onlineGroupDeviceIds, DEVICE_A_ID) A already has this
        scanGroupIdForDevices(onlineGroupId, arrayListOf(DEVICE_B_ID, DEVICE_C_ID)) // TODO: 2x There was a message: "Unknown Members", "There are $unknownMembersCount unknown members in this online group. These members will not receive any message.")


        switchTo(chatOverviewRule, DEVICE_B_ID)
        // TODO: We create it on DeviceB but offlineGroupDeviceIds does not contain this Id (good for scanning bad for writing messages to?) But offlinegroups wont change so probably no effect for the test..
        val offlineGroupId = createOfflineGroupAndReturnGroupId("OFFLINE_GROUP_NAME")
        offlineGroupOnlineId = dialogCreationHelper.getOnlineIdFromUserId(offlineGroupId)
        scanGroupIdForDevices(offlineGroupId, offlineGroupDeviceIds)

    }


    private fun doOnboardingByFileForAllDevices() {
        // Needed otherwise currentActivity is null for deinitOffline
        switchTo(welcomeOnlineRule, DEVICE_A_ID)
        doOnboardingByFile(DEVICE_A_ID)
        doOnboardingByFile(DEVICE_B_ID)
        doOnboardingByFile(DEVICE_C_ID)
        doOnboardingByFile(DEVICE_D_ID)
    }


    private fun doOnboardingByFile(deviceId: String) {
        switchTo(welcomeOfflineRule, deviceId)
        var file = TestFileUtil.getFile(deviceId, DeviceRole(DeviceRole.OFFLINE), welcomeOfflineRule.activity)
        val realmHelper = RealmHelper(securityInterfaceHolder, context, RealmOfflinePersistenceConfiguration())
        ImportExportHelper(securityInterfaceHolder, realmHelper)
            .startImportWithoutLaunchingMainActivity(file, welcomeOfflineRule.activity)
        switchTo(chatOverviewRule, deviceId) // Needed so the storage is refreshed with proper values.
        val importId = dataSecurityHelper.getFullOnlineUserData(offlineStorage)

        if (onlineGroupDeviceIds.contains(deviceId)) {
            onlineGroupOnlineId = offlineStorage.onlineGroups.get(0).hash
        }
        if (offlineGroupDeviceIds.contains(deviceId)) {
            offlineGroupOnlineId = offlineStorage.offlineGroups.get(0).hash
        }

        switchTo(welcomeOnlineRule, deviceId)
        dataGeneratorHelper.storeFullOnlineUserDataForOfflineDevice(importId, onlineStorage)
        ONLINE_ID_BY_DEVICE_ID.put(deviceId, importId)
        onlineStorage.persistDeviceRole(DeviceRole(DeviceRole.ONLINE))
    }
}