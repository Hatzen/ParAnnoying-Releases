package de.hartz.software.parannoying.offline.activities.offline

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.ContextMenu
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.hartz.software.parannoying.air.gap.model.UseCases
import de.hartz.software.parannoying.core.helper.ShortcutHelper.EXTRA_SHORTCUT_ID
import de.hartz.software.parannoying.core.helper.ShortcutHelper.EXTRA_VALUE_SYNC
import de.hartz.software.parannoying.core.helper.ShortcutHelper.EXTRA_VALUE_USERID
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.core.helper.ui.getAddMessageIcon
import de.hartz.software.parannoying.core.helper.ui.getAddUserIcon
import de.hartz.software.parannoying.core.helper.ui.getGearIcon
import de.hartz.software.parannoying.core.helper.ui.getOutboxIcon
import de.hartz.software.parannoying.core.interfaces.di.air.gap.AirGapAdapter
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ResultCallback
import de.hartz.software.parannoying.offline.R
import de.hartz.software.parannoying.offline.databinding.ActivityMainOfflineBinding
import de.hartz.software.parannoying.offline.fragments.AbstractMainFragment
import de.hartz.software.parannoying.offline.fragments.ChatOverviewFragment
import de.hartz.software.parannoying.offline.fragments.SendDataOverviewFragment
import de.hartz.software.parannoying.offline.fragments.SettingsOverviewFragment
import de.hartz.software.parannoying.offline.fragments.UserOverviewFragment
import de.hartz.software.parannoying.offline.helper.CleartextMessageProcessingWorker
import de.hartz.software.parannoying.offline.helper.onboarding.OfflineIntroducer
import de.hartz.software.parannoying.offline.model.domain.DecryptionKeyCloakForUser
import de.hartz.software.parannoying.offline.model.domain.UserSecurity
import de.hartz.software.parannoying.offline.model.domain.events.BaseEvent
import de.hartz.software.parannoying.offline.model.domain.events.SimpleEvent
import javax.inject.Inject


class OfflineMainActivity : BaseOfflineActivity() {

    companion object {
        const val REQUEST_INVALIDATE = -1

        // Avoid getting garbage collected when showing userId.
        private var unconfirmedDecryptionKeyCloakForUser: DecryptionKeyCloakForUser? = null
    }

    private lateinit var binding: ActivityMainOfflineBinding
    private val offlineStorage get() = Storage
    @Inject
    lateinit var airGapAdapter: AirGapAdapter
    @Inject
    lateinit var securityInterfaceHolder: SecurityInterfaceHolder


    val chatFragment = ChatOverviewFragment()
    val messageFragment = SendDataOverviewFragment()
    val userFragment = UserOverviewFragment()
    val settingsFragment = SettingsOverviewFragment()
    // lateinit var currentFragment: AbstractMainFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainOfflineBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        app.offlineComponents.inject(this)

        initActionButtons()
        setCurrentFragment(chatFragment)

        handleDataFromIntent(intent)
    }


    private fun setCurrentFragment(fragment: AbstractMainFragment) {
        // currentFragment = fragment
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            .replace(de.hartz.software.parannoying.offline.R.id.fragmentFrame, fragment)
            .commit()
    }

    fun initActionButtons() {

        val bottomNavigationView = findViewById<BottomNavigationView>(de.hartz.software.parannoying.offline.R.id.bottomNavigationView)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.getItemId()) {
                R.id.chats -> setCurrentFragment(chatFragment)
                R.id.messages -> {
                    setCurrentFragment(messageFragment)
                }
                R.id.users -> {
                    setCurrentFragment(userFragment)
                }
                R.id.settings -> setCurrentFragment(settingsFragment)
            }
            true
        }
        val menu = bottomNavigationView.getMenu()

        menu.findItem(R.id.chats).setIcon(getAddMessageIcon(IconHelper.SMALL_ICON_WHITE))
        menu.findItem(R.id.messages).setIcon(getOutboxIcon(IconHelper.SMALL_ICON_WHITE))
        menu.findItem(R.id.users).setIcon(getAddUserIcon(IconHelper.SMALL_ICON_WHITE))
        menu.findItem(R.id.settings).setIcon(getGearIcon(IconHelper.SMALL_ICON_WHITE))

        // TODO:  https://stackoverflow.com/a/61667887/8524651
        /* val newMessageCount = Storage.readSendMessage().size
        val badge = bottomNavigationView.getOrCreateBadge(R.id.messages);
        if (newMessageCount > 0) {
            badge.isVisible = true;
            badge.number = newMessageCount;
        } else {
            badge.isVisible = false;
        }*/
    }

    override fun onResume() {
        super.onResume()
        // Revalidate security
        offlineStorage.currentUser.userSecurityIssues.addAll(UserSecurity.getAllByContext(this, offlineStorage.readSettings()))
        offlineStorage.updateCurrentUser()

        OfflineIntroducer(this).startIntroduction()
    }

    private fun handleDataFromIntent(intent: Intent) {
        val shortcut = intent.extras?.getString(EXTRA_SHORTCUT_ID)
        if (shortcut != null) {
            if (shortcut == EXTRA_VALUE_SYNC) {
                setCurrentFragment(messageFragment)
                supportFragmentManager.executePendingTransactions()
                // TODO: when onboarding is not finished now this leads to weird ui introducer
                messageFragment.startSync()
            }
            if (shortcut == EXTRA_VALUE_USERID) {
                setCurrentFragment(userFragment)
                supportFragmentManager.executePendingTransactions()
                // TODO: Get a better callback
                Handler().postDelayed( {userFragment.showUserId()}, 1000)
            }
            return
        }

        airGapAdapter.handleDataFromIntent(intent, object : ResultCallback {
            override fun onSuccess(resultData: String) {

                offlineStorage.persistEvent(SimpleEvent(BaseEvent.EVENT_IMPORT_MESSAGE))
                CleartextMessageProcessingWorker.enqueueReceivedMessage(applicationContext, resultData)

                /*
                // TODO: remove as we dont update it anywhere else, do we?
                val isUpdated = dialogsAdapter.updateDialogWithMessage(user.persistenceId.toString(), user.lastMessage)
                if (!isUpdated) {
                    //Dialog with this ID doesn't exist, so you can create new Dialog or update all dialogs list
                }
                */
                UiHelper.showToastFromBackgroundTask(this@OfflineMainActivity, "Message added")
            }

            override fun onError(errorMsg: String, exception: java.lang.Exception) {
                Log.e(javaClass.simpleName , "Storing Nfc Message" + exception.localizedMessage)
                UiHelper.showToastFromBackgroundTask(this@OfflineMainActivity, "" + exception.localizedMessage)
            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        unconfirmedDecryptionKeyCloakForUser = null
        val result = airGapAdapter.onActivityResult(requestCode, resultCode, data)

        if (!result.success) {
            return
        }
        val useCase = result.getUseCase()
        when (useCase) {
            UseCases.Offline.USERID_SEND -> userFragment.storeUserId()
            UseCases.Offline.USERID_RECEIVE -> {
                val baseDialog = result.getSingleResult().exchangeData
                CleartextMessageProcessingWorker
                    .enqueueCreateUser(applicationContext, baseDialog, chatFragment::createUserCallback)
            }
            UseCases.Offline.MESSAGES_SYNC.sendLaunchOptions -> {
                offlineStorage.deleteAllEncryptedMessages()
                // Start receive of sync messages
            }
            UseCases.Offline.MESSAGES_SYNC.receiveLaunchOptions -> {
                val messageList = result.result!!
                messageList.forEach {
                    if (it.isFile) {
                        CleartextMessageProcessingWorker.enqueueReceivedFile(applicationContext, it.filePath)
                    } else {
                        CleartextMessageProcessingWorker.enqueueReceivedMessage(applicationContext, it.exchangeData)
                    }
                }
                offlineStorage.persistEvent(SimpleEvent(BaseEvent.EVENT_SYNCED_MESSAGES))
            }
            UseCases.Offline.MESSAGE_RECEIVE -> {
                val messageList = result.result!!
                messageList.forEach {
                    if (it.isFile) {
                        CleartextMessageProcessingWorker.enqueueReceivedFile(applicationContext, it.filePath)
                    } else {
                        CleartextMessageProcessingWorker.enqueueReceivedMessage(applicationContext, it.exchangeData)
                    }
                }
            }

        }

    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        // send messages
        if (v?.id == R.id.message_list) {
            val inflater = menuInflater
            inflater.inflate(R.menu.context_menu_send_data_overview, menu)
        }
        // chats
        if (v?.id == R.id.dialogsList) {
            val inflater = menuInflater
            inflater.inflate(R.menu.context_menu_chat_overview, menu)
        }
    }
}
