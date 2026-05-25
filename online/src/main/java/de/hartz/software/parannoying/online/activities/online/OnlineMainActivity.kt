package de.hartz.software.parannoying.online.activities.online

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.hartz.software.parannoying.air.gap.helpers.channels.QrCodeHelper
import de.hartz.software.parannoying.air.gap.model.UseCases
import de.hartz.software.parannoying.air.gap.model.UseCases.Online.ONLINEID_FIRST_SEND
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.helper.ShortcutHelper.EXTRA_SHORTCUT_ID
import de.hartz.software.parannoying.core.helper.ShortcutHelper.EXTRA_VALUE_SYNC
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.helper.io.IOHelper.initShareImage
import de.hartz.software.parannoying.core.helper.io.IOHelper.startDisabledService
import de.hartz.software.parannoying.core.helper.security.DataSecurityHelper
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.core.helper.ui.getGearIcon
import de.hartz.software.parannoying.core.helper.ui.getInboxIcon
import de.hartz.software.parannoying.core.helper.ui.getOutboxIcon
import de.hartz.software.parannoying.core.interfaces.di.air.gap.AirGapAdapter
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ResultCallback
import de.hartz.software.parannoying.core.model.domain.DeviceRole
import de.hartz.software.parannoying.core.model.exceptions.ReportExceptionWrapper
import de.hartz.software.parannoying.online.R
import de.hartz.software.parannoying.online.adapters.FirebaseAdapter
import de.hartz.software.parannoying.online.databinding.ActivityMainOnlineBinding
import de.hartz.software.parannoying.online.fragments.AbstractMainFragment
import de.hartz.software.parannoying.online.fragments.message.overview.InboxFragment
import de.hartz.software.parannoying.online.fragments.message.overview.OutboxFragment
import de.hartz.software.parannoying.online.fragments.message.overview.SettingsOverviewFragment
import de.hartz.software.parannoying.online.helper.FirebaseAdapterCallbackImpl
import de.hartz.software.parannoying.online.helper.network.NotificationReceiver
import de.hartz.software.parannoying.online.interfaces.OnlineApplication
import de.hartz.software.parannoying.online.model.OnlineStorage
import de.hartz.software.parannoying.online.model.domain.OutboxEncryptedMessage
import org.acra.ACRA
import javax.inject.Inject


/**
 * Activity for the online device. It shows all Incoming messages and can scan messages from
 * offline device.
 */
class OnlineMainActivity : AppCompatActivity() {

    @Inject
    lateinit var airGapAdapter: AirGapAdapter
    @Inject
    lateinit var securityInterfaceHolder: SecurityInterfaceHolder

    var onlineInterface: FirebaseAdapter? = null
    val onlineStorage get() = Storage as OnlineStorage

    val inboxFragment = InboxFragment()
    val outboxFragment = OutboxFragment()
    private val settingsOverviewFragment = SettingsOverviewFragment()

    private lateinit var binding: ActivityMainOnlineBinding
    private var tmpStoreResult: String? = null
    private var firebaseAdapterCallbackImpl: FirebaseAdapterCallbackImpl? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainOnlineBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        (app as OnlineApplication).onlineComponents
            .inject(this)

        handleDataFromIntent(intent)
        doOnboardingIfNeeded()

        initActionButtons()
        setCurrentFragment(inboxFragment)
    }


    private fun setCurrentFragment(fragment: AbstractMainFragment) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            // .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.slide_out_right)
            // .addToBackStack(null)
            // .setReorderingAllowed(true)
            .replace(R.id.fragmentFrame, fragment)
            .commit()
    }

    fun initActionButtons() {

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.getItemId()) {
                R.id.inbox -> setCurrentFragment(inboxFragment)
                R.id.outbox -> {
                    setCurrentFragment(outboxFragment)
                }
                R.id.settings -> setCurrentFragment(settingsOverviewFragment)
            }
            true
        }
        val menu = bottomNavigationView.getMenu()

        menu.findItem(R.id.inbox).setIcon(getInboxIcon(IconHelper.SMALL_ICON_WHITE))
        menu.findItem(R.id.outbox).setIcon(getOutboxIcon(IconHelper.SMALL_ICON_WHITE))
        menu.findItem(R.id.settings).setIcon(getGearIcon(IconHelper.SMALL_ICON_WHITE))


        /*
         */
        // TODO:  https://stackoverflow.com/a/61667887/8524651
        /* val newMessageCount = onlineStorage.readOutboxEncryptedMessages().size
        val badge = bottomNavigationView.getOrCreateBadge(R.id.outbox);
        if (newMessageCount > 0) {
            badge.isVisible = true;
            badge.number = newMessageCount;
        } else {
            badge.isVisible = false;
        }*/
    }

    override fun onResume() {
        super.onResume()
        if (onlineStorage.useGoogleApi) {
            // Login if needed.
            onlineInterface = FirebaseAdapter(onlineStorage)
            initOnlineStatus()
        }
    }

    private fun initOnlineStatus() {
        firebaseAdapterCallbackImpl = FirebaseAdapterCallbackImpl(this)
        colorFrom = resources.getColor(R.color.colorRed)
        colorTo = resources.getColor(R.color.colorRed)
        initStatusSwitcher()
        setExchangeStatus(de.hartz.software.parannoying.air.gap.fragments.exchange.AirGapFragment.StatusColor.CONNECTING)
        onlineInterface?.signIn(this, firebaseAdapterCallbackImpl!!)
    }

    override fun onPause() {
        super.onPause()
        cancleStatusSwitcher()

        onlineInterface?.removeAllListeners()
        firebaseAdapterCallbackImpl?.cancleRetries()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IOHelper.RESULT_CODE_SHARE_IMAGE && resultCode == Activity.RESULT_OK) {
            if (outboxFragment.hasTriggeredShare()) {
                outboxFragment.removeSharedMessage()
            }
        }

        val result = airGapAdapter.onActivityResult(requestCode, resultCode, data)

        if (!result.success) {
            return
        }
        val useCase = result.getUseCase()
        when (useCase) {
            // Onboarding activity accepted.
            UseCases.Online.ONLINEID_FIRST_SEND -> if (resultCode == Activity.RESULT_OK) {
                onlineStorage.persistDeviceRole(DeviceRole( DeviceRole.ONLINE ))
            } else {
                // Onboarding were denied.
                finish()
                return
            }
            UseCases.Online.MESSAGE_SEND -> {
                inboxFragment.handleSyncSendMessages()
            }
            UseCases.Online.MESSAGES_SYNC.sendLaunchOptions -> {
                inboxFragment.handleSyncSendMessages()
            }
            UseCases.Online.MESSAGES_SYNC.receiveLaunchOptions -> {
                // TODO: throw exception when file message is received?
                val resultList = result.result!!
                handleSyncReceivedMessages(resultList.map { it.exchangeData })
            }
            UseCases.Online.CRASH_RECEIVE -> {
                val exceptionText = result.getSingleResult().exchangeData
                val artificialException = ReportExceptionWrapper(exceptionText)
                ACRA.errorReporter.handleSilentException(artificialException)
            }
            UseCases.Online.MESSAGE_SHARE_RECEIVE -> {
                // TODO: Could be anything probably sharing whole exchange file would be useful,
                //  on the other hand not all messages belong to one receiver..
                val singleMessage = result.getSingleResult().exchangeData
                sendViaShare(singleMessage)
            }
        }
    }

    private fun handleSyncReceivedMessages(resultList: Sequence<String>) {
        val dataSecurityHelper =  DataSecurityHelper(securityInterfaceHolder)
        resultList
            .filter {
                val firebaseId = dataSecurityHelper.getOnlineIdFromMessage(it)
                !dataSecurityHelper.isOnlineIdValid(firebaseId)
            }
            .forEach {
                val targetUserHash = dataSecurityHelper.getOnlineIdFromMessage(it)
                val outboxEncryptedMessage = OutboxEncryptedMessage(it,
                    IOHelper.getCurrentDateAsUnixTimestamp() * 1000,
                    targetUserHash
                )
                onlineStorage.persistOutboxEncryptedMessages(outboxEncryptedMessage)
            }
        outboxFragment.updateOutbox()

        resultList
            .filter {
                val firebaseId = dataSecurityHelper.getOnlineIdFromMessage(it)
                dataSecurityHelper.isOnlineIdValid(firebaseId)
            }
            .forEach { sendMessage(it) }

    }

    private fun handleDataFromIntent(intent: Intent) {
        val shortcut = intent.extras?.getString(EXTRA_SHORTCUT_ID)
        if (shortcut != null) {
            if (shortcut == EXTRA_VALUE_SYNC) {
                inboxFragment.startSync()
            }
            return
        }

        airGapAdapter.handleDataFromIntent(intent, object : ResultCallback {
            override fun onSuccess(resultData: String) {
                onlineStorage.addInboxEncryptedMessage(resultData)
            }

            override fun onError(errorMsg: String, exception: java.lang.Exception) {
                Log.e(javaClass.simpleName , "Storing Nfc Message" + exception.localizedMessage)
                UiHelper.showToastFromBackgroundTask(this@OnlineMainActivity, "" + exception.localizedMessage)
            }

        })
    }

    private fun sendMessage(message: String) {
        val dataSecurityHelper =  DataSecurityHelper(securityInterfaceHolder)
        val targetUser = dataSecurityHelper.getOnlineIdFromMessage(message)

        if (dataSecurityHelper.isOnlineIdValid(targetUser)) {
            val targetUserHash = dataSecurityHelper.getOnlineIdFromMessage(message)
            val outboxEncryptedMessage = OutboxEncryptedMessage(message, IOHelper.getCurrentDateAsUnixTimestamp() * 1000, targetUserHash)
            onlineStorage.persistOutboxEncryptedMessages(outboxEncryptedMessage)
            firebaseAdapterCallbackImpl!!.onMessageStateChanged() // Delivering

            sendOnlineMessage(targetUser, message, firebaseAdapterCallbackImpl!!)
        } else {
            DialogHelper.showYesNoAlert(this,  "The user is not accessible via google api. " +
                    "You can only contact him via an other messenger. Do you want to share the message with an other messenger now?",
                    DialogInterface.OnClickListener { dialog, which ->
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            sendViaShare(message)
                        }
                    })
        }
    }

    fun sendOnlineMessage (targetUser: String, message: String, callback: FirebaseAdapter.MessageCallback) {
        val dataSecurityHelper =  DataSecurityHelper(securityInterfaceHolder)
        var realOnlineId = targetUser // Handle single user.
        if (dataSecurityHelper.isOnlineIdForOnlineGroup(targetUser)) {
            val targetMemberOnlineId = dataSecurityHelper.getOnlineIdForTargetOfOnlineGroup(targetUser)
            if (!dataSecurityHelper.isOnlineIdValid(targetMemberOnlineId)) {
                throw RuntimeException("member has invalid onlineId " + targetMemberOnlineId + " for group "+ targetUser + ". Can not send message via firebase. ")
            }
            realOnlineId = targetMemberOnlineId
        }
        onlineInterface?.sentMessage(realOnlineId, message, callback)
    }

    private fun doOnboardingIfNeeded() {
        val dataSecurityHelper =  DataSecurityHelper(securityInterfaceHolder)
        // First app start.
        if (!Storage.isOnlineDevice()) {
            startDisabledService(NotificationReceiver::class.java, this, onlineStorage.useGoogleApi)
            val onlineId = dataSecurityHelper.getFullOnlineUserData(this.Storage)
            airGapAdapter.startSend(ONLINEID_FIRST_SEND.useText(onlineId))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (IOHelper.isStoragePermissionGranted(this)) {
            val bitmap = QrCodeHelper.dataToQrCode(tmpStoreResult!!, this)!!
            initShareImage(bitmap, this)
        }
    }

    fun sendViaShare (message: String) {
        // TODO: There can be a difference between sharing via qrcode and text.
        if (!IOHelper.isStoragePermissionGranted(this)) {
            tmpStoreResult = message
        }
        val bitmap = QrCodeHelper.dataToQrCode(message, this)!!
        initShareImage(bitmap, this)
    }


    // TODO: Unify with DataExchangeChannelsFragment animation.
    private var colorAnimation: ValueAnimator? = null
    private val statusColorEvaluator = ArgbEvaluator()
    private var separatorView: View? = null

    private var colorFrom : Int = 0
    private var colorTo : Int = 0

    fun setExchangeStatus(status: de.hartz.software.parannoying.air.gap.fragments.exchange.AirGapFragment.StatusColor) {
        colorFrom = (separatorView!!.background as ColorDrawable).color

        colorTo = when (status) {
            de.hartz.software.parannoying.air.gap.fragments.exchange.AirGapFragment.StatusColor.NO_MATCH -> {
                resources.getColor(R.color.colorRed)
            }
            de.hartz.software.parannoying.air.gap.fragments.exchange.AirGapFragment.StatusColor.CONNECTING -> {
                resources.getColor(R.color.colorYellow)
            }
            de.hartz.software.parannoying.air.gap.fragments.exchange.AirGapFragment.StatusColor.CONNECTED -> {
                resources.getColor(R.color.colorPrimaryDark)
            }
        }

        colorAnimation?.cancel()
        colorAnimation?.setIntValues(colorFrom, colorTo)
        colorAnimation?.start()
    }

    private fun initStatusSwitcher() {
        separatorView = findViewById<View>(R.id.online_status_bar)
        separatorView!!.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        colorAnimation?.cancel()
        colorAnimation = ValueAnimator.ofObject(statusColorEvaluator, colorFrom, colorTo)
        colorAnimation!!.duration = 1000
        colorAnimation!!.addUpdateListener { animator ->
            run {
                separatorView!!.setBackgroundColor(animator.animatedValue as Int)
            }
        }
        colorAnimation!!.start()
        colorAnimation!!.repeatCount = 0
    }

    private fun cancleStatusSwitcher() {
        colorAnimation?.cancel()
        separatorView?.setBackgroundColor(resources.getColor(R.color.colorAccent))
    }
}
