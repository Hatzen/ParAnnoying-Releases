package de.hartz.software.parannoying.app.large.tests.e2e

import android.Manifest
import android.app.Activity
import android.app.job.JobScheduler
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.ViewGroup
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import de.hartz.software.parannoying.app.App
import de.hartz.software.parannoying.app.large.tests.utils.DeviceUtil
import de.hartz.software.parannoying.app.large.tests.utils.StrictActivityTestRule
import de.hartz.software.parannoying.app.large.tests.utils.TestFileUtil
import de.hartz.software.parannoying.core.helper.development.DevelopmentUtil
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.helper.security.DataGeneratorHelper
import de.hartz.software.parannoying.core.helper.security.DataSecurityHelper
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.interfaces.di.ApplicationInfoComponent
import de.hartz.software.parannoying.core.interfaces.di.security.RandomHelper
import de.hartz.software.parannoying.core.model.domain.DeviceRole
import de.hartz.software.parannoying.core.model.domain.settings.Channels
import de.hartz.software.parannoying.offline.activities.offline.ChatActivity
import de.hartz.software.parannoying.offline.activities.offline.OfflineMainActivity
import de.hartz.software.parannoying.offline.activities.offline.WelcomeOfflineActivity
import de.hartz.software.parannoying.offline.businesslogic.CreateMessageProcessor
import de.hartz.software.parannoying.offline.businesslogic.ReceiveMessageProcessor
import de.hartz.software.parannoying.offline.helper.guard.internet.NetworkSchedulerService
import de.hartz.software.parannoying.offline.helper.guard.usb.UsbSchedulerService
import de.hartz.software.parannoying.offline.helper.security.DialogCreationHelper
import de.hartz.software.parannoying.offline.helper.security.serializer.EncryptionHandler
import de.hartz.software.parannoying.offline.model.OfflineStorage
import de.hartz.software.parannoying.offline.model.domain.SendMessage
import de.hartz.software.parannoying.offline.model.domain.dialogs.BaseDialog
import de.hartz.software.parannoying.offline.model.domain.settings.OfflineSettings
import de.hartz.software.parannoying.online.activities.online.OnlineMainActivity
import de.hartz.software.parannoying.online.activities.online.WelcomeOnlineActivity
import de.hartz.software.parannoying.online.adapters.FirebaseAdapter
import de.hartz.software.parannoying.online.model.OnlineStorage
import de.hartz.software.parannoying.online.model.domain.InboxEncryptedMessage
import de.hartz.software.parannoying.online.model.domain.SettingsOnline
import org.awaitility.Awaitility
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 *
 * HINT: The tests do not Test Services, Permissions, real UI usage, data exchange
 *
 * Currently the test is running long running tasks in main thread may leading to sigabrt 6, go to develper settings and enable "Show ANRs from background threads"
 * https://stackoverflow.com/a/36694489/8524651
 */
@LargeTest
@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
abstract class AbstractE2ETest {
    // Configurables.

    // May be more messages as for groups.
    val numberOfMostMessages = 5 // TODO: increase to 50


    // Finals.
    lateinit var context: Context

    lateinit var deviceUtil: DeviceUtil
    val DEVICE_A_ID = "A"
    val DEVICE_B_ID = "B"
    val DEVICE_C_ID = "C"
    val DEVICE_D_ID = "D"

    val applicationInfoComponent = object: ApplicationInfoComponent {
        override fun getVersion(): String {
            return "dummy-version"
        }
    }

    val ASYNC_TIMEOUT_SECONDS: Long = 10
    val onlineGroupDeviceIds = arrayListOf(DEVICE_A_ID, DEVICE_B_ID, DEVICE_C_ID)
    val offlineGroupDeviceIds = arrayListOf(DEVICE_A_ID, DEVICE_C_ID) // DEVICE_D_ID // TODO: the created testdata seems as they dont scanned the offlinegroup on device D

    // Helper to get the firebaseId (or pseudoId) by the DeviceId. (Usually should not be the same for multiple devices, but might be)
    val ONLINE_ID_BY_DEVICE_ID = HashMap<String, String>()

    // Contains all messages which can not be deliverd by firebase api. Should only contain messages which are not processed yet.
    // users onlineId by all not received messages or filepaths
    val MESSAGES_FOR_OFFLINE_IDS = HashMap<String, MutableList<String>>()
    val FILE_PATH_MESSAGES_FOR_OFFLINE_IDS = HashMap<String, MutableList<String>>()

    val offlineStorage get() = deviceUtil.storage as OfflineStorage
    val onlineStorage get() = deviceUtil.storage as OnlineStorage

    // Grant all Permissions of
    @Rule
    @JvmField var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            // Manifest.permission.WRITE_EXTERNAL_STORAGE, // TODO: Throws on sdk33
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.NFC,
            Manifest.permission.INTERNET)

    @Rule
    @JvmField var chatOverviewRule = StrictActivityTestRule(OfflineMainActivity::class.java)

    @Rule
    @JvmField var chatRule: StrictActivityTestRule<ChatActivity> = StrictActivityTestRule(ChatActivity::class.java)

    @Rule
    @JvmField var welcomeOfflineRule = StrictActivityTestRule(WelcomeOfflineActivity::class.java)

    @Rule
    @JvmField var welcomeOnlineRule = StrictActivityTestRule(WelcomeOnlineActivity::class.java)

    @Rule
    @JvmField var messageOverviewRule = StrictActivityTestRule(OnlineMainActivity::class.java)

    lateinit var dataGeneratorHelper: DataGeneratorHelper
    lateinit var dataSecurityHelper: DataSecurityHelper
    lateinit var dialogCreationHelper: DialogCreationHelper
    lateinit var randomHelper: RandomHelper
    lateinit var securityInterfaceHolder: SecurityInterfaceHolder

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<App>()
        init()

        deviceUtil = DeviceUtil(context as App)

        deviceUtil.clearFirebaseId()
        try {
            deviceUtil.cleanUpAll() // Reset all test data.
        } catch(e: Exception) {
            // This might fail, when the files are not created and properly cleaned before..
            e.printStackTrace()
        }

        deviceUtil.addDevice(DEVICE_A_ID)
        deviceUtil.addDevice(DEVICE_B_ID)
        deviceUtil.addDevice(DEVICE_C_ID)
        deviceUtil.addDevice(DEVICE_D_ID)
    }

    private fun init() {
        val app = context as App
        securityInterfaceHolder = app.securityInterfaceHolder
        dataGeneratorHelper = DataGeneratorHelper(securityInterfaceHolder)
        dataSecurityHelper = DataSecurityHelper(securityInterfaceHolder)
        dialogCreationHelper = DialogCreationHelper(context, securityInterfaceHolder)
        randomHelper = app.securityInterfaceHolder.randomHelper
    }


    @After
    fun tearDown() {
        try {
            deviceUtil.cleanUpAll()
        } catch (exception: Exception) {
            Log.e(javaClass.simpleName, "Could not clean up data", exception)
        }
    }

    protected fun getListWithout(users: List<String>, remo: String): List<String> {
        val copiedList = ArrayList(users)
        copiedList.remove(remo)
        return copiedList
    }

    protected fun createAndSendMessageToUser(senderDeviceId: String, targetUserHash: String, receiverDeviceIds: List<String>) {
        switchTo(chatOverviewRule, senderDeviceId)
        val cleanedOnlineId = dataGeneratorHelper.cleanOnlineUserId(targetUserHash)
        val targetUserOnDifferentDevice = getDialogByOnlineId(cleanedOnlineId)

        val listOfEncryptedMessages = createMessageForUser(targetUserOnDifferentDevice)

        switchTo(messageOverviewRule, senderDeviceId)
        sendEncryptedMessageViaOnlineDevice(listOfEncryptedMessages)

        receiverDeviceIds.forEach {
            switchTo(messageOverviewRule, it)
            val receivedMessage = receiveThisMessageViaOnlineDevice()

            switchTo(chatOverviewRule, it)
            val targetDialog = getDialogByOnlineId(cleanedOnlineId) // Either onlinegroup or currentuser.
            try {
                readMessageForUser(receivedMessage, targetDialog, 1)
            } catch (exception: java.lang.Exception) {
                throw exception
            }

        }
    }

    protected fun createAndSendFileToUser(senderDeviceId: String, targetUserHash: String, receiverDeviceIds: List<String>, largeFile: Boolean = false) {
        switchTo(chatOverviewRule, senderDeviceId)
        val cleanedOnlineId = dataGeneratorHelper.cleanOnlineUserId(targetUserHash)
        val targetUserOnDifferentDevice = getDialogByOnlineId(cleanedOnlineId)

        val listOfEncryptedMessages = createFileMessageForUser(targetUserOnDifferentDevice, largeFile)

        switchTo(messageOverviewRule, senderDeviceId)

        listOfEncryptedMessages.forEach {
            val targetUserPseudoId = dataSecurityHelper.getOnlineIdFromFile(it.encryptedFilePath)
            var listOfMessagesForUser = FILE_PATH_MESSAGES_FOR_OFFLINE_IDS[targetUserPseudoId]
            if (listOfMessagesForUser == null) {
                listOfMessagesForUser = arrayListOf()
                FILE_PATH_MESSAGES_FOR_OFFLINE_IDS[targetUserPseudoId] = listOfMessagesForUser
            }
            listOfMessagesForUser.add(it.encryptedFilePath)
        }

        receiverDeviceIds.forEach {
            switchTo(messageOverviewRule, it)
            val receivedMessage = receiveFileMessageWithOnlineDevice()

            switchTo(chatOverviewRule, it)
            val targetDialog = getDialogByOnlineId(cleanedOnlineId) // Either onlinegroup or currentuser.
            try {
                readFileMessageForUser(receivedMessage, targetDialog)
            } catch (exception: java.lang.Exception) {
                throw exception
            }

        }
    }


    protected fun getDialogByOnlineId(cleanedOnlineId: String): BaseDialog {
        val targetUser = offlineStorage.getDialogs().find { it.hash == cleanedOnlineId }
        if (targetUser == null) {
            Log.e(javaClass.simpleName, "No match for user with onlineId" + cleanedOnlineId + " found on device of " + offlineStorage.currentUser.originalName)
            offlineStorage.getDialogs().forEach() {
                Log.v(javaClass.simpleName, "Possible match with userHash " + it.hash)
            }
        }
        return targetUser!!
    }

    protected fun onboardingDevice(deviceId: String) {
        switchTo(welcomeOnlineRule, deviceId)
        val fullOnlineUserIdResultA = getOnlineId()
        Log.i(javaClass.simpleName, "Created onlineId $fullOnlineUserIdResultA for deviceId $deviceId")

        ONLINE_ID_BY_DEVICE_ID.put(deviceId, fullOnlineUserIdResultA)
        switchTo(welcomeOfflineRule, deviceId)

        createOfflineUserData(fullOnlineUserIdResultA)
    }

    protected fun connectDevice(senderUserId: String, receiverUserId: String) {
        switchTo(chatOverviewRule, senderUserId)
        val offlineUserId = createReturnAndStoreAUserId()
        switchTo(chatOverviewRule, receiverUserId)
        scanOtherUser(offlineUserId)
    }

    protected fun isOnlinePossible(): Boolean {
        return FirebaseAdapter(onlineStorage).isGooglePlayServicesAvailable(context)
    }

    protected open fun isOfflineTest(): Boolean {
        return false
    }

    protected open fun getOnlineId(): String {
        onlineStorage.persistDeviceRole(DeviceRole(DeviceRole.ONLINE))
        onlineStorage.setUseGoogleApi(true)
        val settings = SettingsOnline()
        settings.allowedChannels = arrayListOf(Channels.TEXT)
        settings.primaryChannel = Channels.TEXT
        settings.screenSaver = false
        onlineStorage.persistSettings(settings)
        return if (isOfflineTest()) {
            getFakeOnlineId()
        } else {
            createFullOnlineId()
        }
    }

    protected fun createFullOnlineId() : String {
        // Fragment not attached to activity yet so set context and activity..
        welcomeOnlineRule.activity.generateOnlineIdFragment.mActivity = welcomeOnlineRule.activity
        welcomeOnlineRule.activity.generateOnlineIdFragment.mContext = welcomeOnlineRule.activity

        // val view = welcomeOnlineRule.activity.findViewById<ViewGroup>(R.id.content)
        val view = welcomeOnlineRule.activity.generateOnlineIdFragment.requireView() as ViewGroup
        welcomeOnlineRule.activity.generateOnlineIdFragment.tryCreatingNotificationId(view)

        // VERY IMPORTANT: IF SMARTPHONE DISPLAY IS DISABLED THE ACTIVITIY WONT BE CREATED. SO TIMEOUT WILL HAPPEN: TODO: Refactore code to not depend on Activity Lifecycle....
        // This test needs internet connection. Ensure to turn of flightmode.
        Awaitility.await().atMost(ASYNC_TIMEOUT_SECONDS, TimeUnit.SECONDS).until({ welcomeOnlineRule.activity.generateOnlineIdFragment.finishedRegisteringWithOnline == true },
                CoreMatchers.allOf(CoreMatchers.notNullValue(), CoreMatchers.not(CoreMatchers.equalTo(false)))) // We probably just need the false check.

        val onlineUserId = dataSecurityHelper.getFullOnlineUserData(onlineStorage)

        return onlineUserId
    }

    private fun getFakeOnlineId(): String {
        onlineStorage.setUseGoogleApi(false)
        val fakeOnlineId = dataGeneratorHelper.createFakeOnlineId() + "-fakeNotificationId"
        onlineStorage.onlineUserId = fakeOnlineId
        return fakeOnlineId
    }


    protected fun createOfflineUserData(onlineUserIdResult: String) {
        val activity = welcomeOfflineRule.activity

        // activity.createUserId
        var userName = "Device"
        userName += deviceUtil.currentDevice!!.first

        dialogCreationHelper.createCurrentUserData(userName, onlineUserIdResult, activity)
        offlineStorage.persistDeviceRole(DeviceRole(DeviceRole.OFFLINE))

        val settings = OfflineSettings()
        settings.syncAllMessages = true
        settings.allowedChannels = arrayListOf(Channels.TEXT)
        settings.primaryChannel = Channels.TEXT
        settings.screenSaver = false
        offlineStorage.persistSettings(settings)

        Assert.assertEquals("Name is correct", userName, offlineStorage.currentUser.name) // When name is empty it is a sign for permissions failed on firebase db
        Assert.assertNotSame("Key is generated", null, offlineStorage.currentUser.decryptionKeyCloakForUser)
        Assert.assertNotSame("SignKey is generated", null, offlineStorage.currentUser.signKey)
    }

    protected fun createReturnAndStoreAUserId (): String {
        val activity = currentActivity
        val keyTuple = dialogCreationHelper.getRandomUserId(activity)
        val userId = keyTuple.first
        val decryptionKeyCloakForUser = keyTuple.second
        offlineStorage.persistUnconfirmedKey(decryptionKeyCloakForUser)
        return userId
    }

    protected fun createOfflineGroupAndReturnGroupId(groupName: String): String {
        val activity = currentActivity
        dialogCreationHelper.createAndStoreOfflineGroup(groupName, activity)
        return offlineStorage.offlineGroups.find { it.originalName == groupName }!!.groupId
    }

    protected fun createOnlineGroup(groupName: String, deviceIds: ArrayList<String>): String {
        val firebaseIds = deviceIds.map { ONLINE_ID_BY_DEVICE_ID[it]!! }.map { dataGeneratorHelper.cleanOnlineUserId(it) }
        val activity = currentActivity
        dialogCreationHelper.createOnlineGroup(groupName, firebaseIds, activity)
        return offlineStorage.onlineGroups.find { it.originalName == groupName }!!.groupId
    }

    protected fun scanGroupIdForDevices(unknownUserId: String, deviceIdsGonnaScanTheUser: ArrayList<String>) {
        deviceIdsGonnaScanTheUser.forEach {
            deviceUtil.switchTo(it, DeviceRole(DeviceRole.OFFLINE))
            val activity = currentActivity
            val otherUser = dialogCreationHelper.createABaseDialogFromUserId(unknownUserId, activity)
            offlineStorage.addDialog(otherUser)
        }
    }

    protected fun scanOtherUser(offlineUserIdOtherUser: String): String {
        val activity = currentActivity
        val otherUser = dialogCreationHelper.createABaseDialogFromUserId(offlineUserIdOtherUser, activity)
        offlineStorage.addDialog(otherUser)
        return otherUser.hash
    }

    protected fun createMessageForUser(dialog: BaseDialog): List<String> {
        val intent = Intent()
                .putExtra(ChatActivity.EXTRA_USER, dialog.getUniqueDialogId())
        switchTo(chatRule, intent = intent)

        // Passed user reference is not the Storage one caused by saving and loading.
        var actualUserReference = getActualDialog(dialog)

        val deviceSpecificTestMessage = getTestMessage(dialog, offlineStorage.currentUser, 2)

        val activity = currentActivity as ChatActivity
        val encryptedMessage = EncryptionHandler(securityInterfaceHolder, offlineStorage, context, applicationInfoComponent)
            .getEncryptedMessages(deviceSpecificTestMessage, actualUserReference)


        val mutex = Semaphore(0)
        actualUserReference = getActualDialog(dialog) // Passed user reference is not the Storage one caused by saving and loading.
        activity.runOnUiThread { // Needed as this will add the message to the view as well.
            CreateMessageProcessor(securityInterfaceHolder, currentActivity, applicationInfoComponent)
                // TODO: Why is metadata null?
                // ava.lang.NullPointerException: Parameter specified as non-null is null: method de.hartz.software.parannoying.offline.model.domain.messages.AbstractMessage.setMetaData, parameter <set-?>
                .applySentMessage(deviceSpecificTestMessage, actualUserReference, {})
            mutex.release()
        }
        // Avoid race conditions caused by running on ui thread.
        try {
            mutex.acquire()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        return encryptedMessage
    }

    protected fun createFileMessageForUser(dialog: BaseDialog, largeFile: Boolean): List<SendMessage> {
        val intent = Intent()
                .putExtra(ChatActivity.EXTRA_USER, dialog.getUniqueDialogId())
        switchTo(chatRule, intent = intent)

        val activity = currentActivity as ChatActivity
        val mutex = Semaphore(0)
        val messageList = mutableListOf<SendMessage>()
        activity.runOnUiThread { // Needed as this will add the message to the view as well.
            val testFile = TestFileUtil.getTestFile(activity, largeFile)

            val sendMessages = EncryptionHandler(securityInterfaceHolder, offlineStorage, activity, applicationInfoComponent)
                    .createAndStoreFileMessages(testFile, dialog, {})
            messageList.addAll(sendMessages)

            mutex.release()
        }
        // Avoid race conditions caused by running on ui thread.
        try {
            mutex.acquire()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        return messageList
    }

    // TODO: Check if this error appears somewhere in the real code...
    protected fun getActualDialog(dialog: BaseDialog): BaseDialog {
        // Passed user reference is not the Storage one caused by saving and loading.
        return offlineStorage.getDialogs().find {
            dialog.persistenceId == it.persistenceId
                && dialog.javaClass == it::class.java
        }!!
    }

    // This is not working properly and sometimes crashing cause service wants to write while being online device.
    protected fun deinitOffline () {
        // Disabling only needed when not running in test. See Apps service initialization.
        if (!DevelopmentUtil.isRunningTest()) {
            return
        }
        // Needs specific context Java.lang.SecurityException: Permission Denial: attempt to change component state
        IOHelper.startDisabledService(UsbSchedulerService::class.java, currentActivity, false)
        IOHelper.startDisabledService(NetworkSchedulerService::class.java, currentActivity, false)

        val jobScheduler = currentActivity.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler?
        jobScheduler!!.cancelAll()

        // Await job cancle so usbjob wont crash with writing event on online device
        Thread.sleep(1000)
    }

    protected fun sendEncryptedMessageViaOnlineDevice(encryptedMessages: List<String>) {

        // Online activity has to be launched EVERY time as the login with current data is hidden in onCreate Activity
        val activity = currentActivity

        val sendMessage = activity::class.java.getDeclaredMethod("sendMessage", String::class.java)
        sendMessage.isAccessible = true

        encryptedMessages.forEach{
            val targetUser = dataSecurityHelper.getOnlineIdFromMessage(it)
            if (dataSecurityHelper.isOnlineIdValid(targetUser)) {
                sendMessage.invoke(activity, it)

                // Needed to check if user is able to send this message. if not logged in user got deleted or no internet connection.
                Awaitility.await().atMost(ASYNC_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .until { FirebaseAdapter(onlineStorage).isUserSignedIn() }
            } else {
                // When we want to send an offline message it wont get send as might expect, so handle it
                handleSendingMessageWithoutFirebase(it)
            }
        }
    }

    protected fun handleSendingMessageWithoutFirebase(message: String)  {
        val targetUserPseudoId = dataSecurityHelper.getOnlineIdFromMessage(message)
        if (!dataSecurityHelper.isOnlineIdValid(targetUserPseudoId)) {
            val isOfflineGroup = targetUserPseudoId.contains(DataSecurityHelper.NOTIFICATION_ID_PREFIX_GROUP_OFFLINE)
            if (!isOfflineGroup) {
                var listOfMessagesForUser = MESSAGES_FOR_OFFLINE_IDS[targetUserPseudoId]
                if (listOfMessagesForUser == null) {
                    listOfMessagesForUser = arrayListOf()
                    MESSAGES_FOR_OFFLINE_IDS[targetUserPseudoId] = listOfMessagesForUser
                }
                listOfMessagesForUser.add(message)
            } else {
                offlineGroupDeviceIds.forEach {
                    val oneOfflineGroupMemberOnlineId = dataGeneratorHelper.cleanOnlineUserId(ONLINE_ID_BY_DEVICE_ID[it]!!)
                    var listOfMessagesForUser = MESSAGES_FOR_OFFLINE_IDS[oneOfflineGroupMemberOnlineId]
                    if (listOfMessagesForUser == null) {
                        listOfMessagesForUser = arrayListOf<String>()
                        MESSAGES_FOR_OFFLINE_IDS[oneOfflineGroupMemberOnlineId] = listOfMessagesForUser as ArrayList<String>
                    }
                    listOfMessagesForUser!!.add(message)
                }
            }
        }
    }

    protected fun receiveFileMessageWithOnlineDevice(): String {
        val activity = currentActivity as OnlineMainActivity

        val potentialMessages = FILE_PATH_MESSAGES_FOR_OFFLINE_IDS[onlineStorage.onlineUserId!!]
        potentialMessages?.forEach {
            val sendAt = System.currentTimeMillis()
            val offlineReceivedInboxMessage = InboxEncryptedMessage(it, System.currentTimeMillis(), sendAt, true)
            onlineStorage.persistInboxEncryptedMessages(offlineReceivedInboxMessage)
        }

        val receivedMessages = onlineStorage.readInboxEncryptedMessages()
        val lastReceivedMessage = receivedMessages[0]
        activity.inboxFragment.selectedMessageIds = arrayListOf(lastReceivedMessage.persistenceId)
        activity.inboxFragment.handleSyncSendMessages()

        // TODO: Currently failing here (only sometimes!) as fileMessage is not set properly.
        //   probably race condition with receiving newest file message somehow..
        return lastReceivedMessage.filePath
    }

    protected fun receiveThisMessageViaOnlineDevice(): String {
        // Online activity has to be launched EVERY time as the login with current data is hidden in onCreate Activity
        val activity = currentActivity as OnlineMainActivity


        // Receive messages without firebase API.
        handleMessageWithPseudoOnlineId()

        Awaitility.await().atMost(ASYNC_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .until({ onlineStorage.readInboxEncryptedMessages().isEmpty() || activity.isFinishing }, CoreMatchers.describedAs(
                " online device to receive encrypted message and list of message to not be empty [and activity finished should be false but is " + activity.isFinishing + "] ",
                CoreMatchers.`is`(false)))

        // TODO: Sometimes the message still arrives 2 times..
        // Assert.assertEquals("Message arrives only once", 1, onlineStorage.inboxEncryptedMessages.size)
        val lastReceivedMessage = onlineStorage.readInboxEncryptedMessages()[0]
        activity.inboxFragment.selectedMessageIds = arrayListOf(lastReceivedMessage.persistenceId)
        activity.inboxFragment.handleSyncSendMessages()
        return lastReceivedMessage.message
    }

    // handle message which cannot be deliverd by firebase API.
    protected fun handleMessageWithPseudoOnlineId() {
        val potentialMessages = MESSAGES_FOR_OFFLINE_IDS[onlineStorage.onlineUserId!!]
        potentialMessages?.forEach {
            val sendAt = System.currentTimeMillis()
            val offlineReceivedInboxMessage = InboxEncryptedMessage(it, System.currentTimeMillis(), sendAt)
            onlineStorage.persistInboxEncryptedMessages(offlineReceivedInboxMessage)
        }
    }

    protected fun readFileMessageForUser(inputPath: String, targetDialog: BaseDialog) {
        val activity = currentActivity as OfflineMainActivity
        val user = ReceiveMessageProcessor(currentActivity, securityInterfaceHolder).addReceivedFileMessage(inputPath)
        var deviceSpecificTestMessage = getTestMessage(offlineStorage.currentUser, targetDialog)
    }

    protected fun readMessageForUser(encryptedMessage: String, targetDialog: BaseDialog, expectedMessageCount: Int) {
        val activity = currentActivity as OfflineMainActivity

        val user = ReceiveMessageProcessor(currentActivity, securityInterfaceHolder).addReceivedMessage(encryptedMessage)

        // assertEquals("user is current user", targetDialog.hash, user.hash) // TODO: Should we make an assertion?

        var deviceSpecificTestMessage = getTestMessage(offlineStorage.currentUser, targetDialog)

        // TODO: Do we want to assert anything when there is no exception? We have other e2e tests that checks these and otherwise there should be an exception
        // assertEquals("Message arrives only once", expectedMessageCount, user.messages.size)
        // assertEquals("Message arrives correct", deviceSpecificTestMessage, user.messages[expectedMessageCount - 1].message)
        // assertEquals("user is current user", targetDialog, user)
    }

    protected fun getTestMessage(targetUser: BaseDialog, sourceUser: BaseDialog, lengthOfMessage: Int = 1): String {
        val postFix = randomHelper.computeRandomHashWithSpecificLength(lengthOfMessage * DataSecurityHelper.MAX_MESSAGE_SIZE)
        return "This is my versy secret message. From " + sourceUser.originalName + " to " + targetUser.originalName + "\n" + postFix
    }

    val currentActivity: Activity get() {
        return current!!.activity
    }
    var current: StrictActivityTestRule<*>? = null
    protected inline fun <reified T : Activity> switchTo(targetActivity: StrictActivityTestRule<T>, deviceId: String? = null, intent: Intent? = null) {

        val deviceRole: DeviceRole
        if (isOnlineActivity(T::class.java)) {
            deviceRole = DeviceRole(DeviceRole.ONLINE)
        } else {
            deviceRole = DeviceRole(DeviceRole.OFFLINE)
            deinitOffline()
        }

        if (current != null && deviceId == null) {
            val currentDeviceRole: DeviceRole
            if (isOnlineActivity(current!!.activity::class.java)) {
                currentDeviceRole = DeviceRole(DeviceRole.ONLINE)
            } else {
                currentDeviceRole = DeviceRole(DeviceRole.OFFLINE)
            }

            if (currentDeviceRole != deviceRole) {
                throw RuntimeException("Switching device without replacing storage.")
            }
        }

        // TODO: Remove, just a trial refreshing Storage reference in app
        // if (current != null) {
        //     (current!!.activity.application as App).invalidateComponents()
        // }

        current?.finishActivity()

        Log.e(javaClass.simpleName, "Switching to device $deviceId and activity ${T::class.java.simpleName}")
        if (deviceId != null) {
            deviceUtil.switchTo(deviceId, deviceRole)
        }
        targetActivity.launchActivity(intent)
        current = targetActivity
    }

    protected fun isOnlineActivity(clazz: Class<*>): Boolean {
        val onlineActivities = listOf<Class<*>>(OnlineMainActivity::class.java, WelcomeOnlineActivity::class.java)
        return onlineActivities.contains(clazz)
    }
}