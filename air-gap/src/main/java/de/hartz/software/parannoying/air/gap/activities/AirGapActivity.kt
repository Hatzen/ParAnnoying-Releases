package de.hartz.software.parannoying.air.gap.activities

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.ProgressBar
import de.hartz.software.parannoying.air.gap.R
import de.hartz.software.parannoying.air.gap.fragments.exchange.AirGapFragment
import de.hartz.software.parannoying.air.gap.fragments.exchange.AirGapFragment.Companion.EXTRA_PERSIST_LAUNCH_OPTIONS
import de.hartz.software.parannoying.core.activities.BaseActivity
import de.hartz.software.parannoying.core.helper.onboarding.Introducer
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.core.helper.ui.getAddMessageIcon
import de.hartz.software.parannoying.core.helper.ui.getBugIcon
import de.hartz.software.parannoying.core.helper.ui.getEventLogIcon
import de.hartz.software.parannoying.core.helper.ui.getOfflineDeviceIcon
import de.hartz.software.parannoying.core.helper.ui.getOnlineDeviceIcon
import de.hartz.software.parannoying.core.helper.ui.getOnlineIdIcon
import de.hartz.software.parannoying.core.helper.ui.getServerIcon
import de.hartz.software.parannoying.core.helper.ui.getUserIdIcon
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ActivityPurpose
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ActivityPurpose.ANY_DATA
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ActivityPurpose.CRASH
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ActivityPurpose.MESSAGE
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ActivityPurpose.ONLINEID
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ActivityPurpose.SERVER_CONFIG
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ActivityPurpose.USERID
import de.hartz.software.parannoying.core.interfaces.di.air.gap.DeviceTarget
import de.hartz.software.parannoying.core.interfaces.di.air.gap.DeviceTarget.ANY
import de.hartz.software.parannoying.core.interfaces.di.air.gap.DeviceTarget.OFFLINE
import de.hartz.software.parannoying.core.interfaces.di.air.gap.DeviceTarget.ONLINE
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ILaunchOptions
import javax.inject.Inject

abstract class AirGapActivity<FRAGMENT: AirGapFragment>: BaseActivity() {

    @Inject
    lateinit var introducer: Introducer
    @Inject
    lateinit var securityInterfaceHolder: SecurityInterfaceHolder

    abstract var dataExchangeChannelsFragment: FRAGMENT
    abstract val  layoutResID: Int

    lateinit var purpose: ActivityPurpose
    lateinit var target: DeviceTarget


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutResID)

        // Never fall asleep while transferring data, to avoid thread getting killed.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        title = "Air Gap Exchange"
        purpose = intent.getSerializableExtra(AirGapFragment.EXTRA_PURPOSE) as ActivityPurpose
        target = intent.getSerializableExtra(AirGapFragment.EXTRA_TARGET) as DeviceTarget

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, dataExchangeChannelsFragment)
                .commit()

        initIntroducer()
    }

    abstract fun initIntroducer()

    open fun handleAllDataProcessed() {
        val resultIntent = Intent()
        val launchOptions = intent.getSerializableExtra(EXTRA_PERSIST_LAUNCH_OPTIONS) as? ILaunchOptions
        resultIntent
                .putExtra(EXTRA_PERSIST_LAUNCH_OPTIONS, launchOptions)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    fun refreshProgress () {
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        progressBar.max = dataExchangeChannelsFragment.maxProgress

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            progressBar.setProgress(dataExchangeChannelsFragment.currentProgress, true)
        } else {
            // TODO: ObjectAnimator.ofInt(progressBar, "progress", numberOfMessagesHandled) just jumps int steps..
            progressBar.setProgress(dataExchangeChannelsFragment.currentProgress)
        }
    }

    override fun onBackPressed() {
        if (dataExchangeChannelsFragment.maxProgress > 0 && dataExchangeChannelsFragment.currentProgress > 1) {
            DialogHelper.showYesNoAlert(this, "Syncing messages is not completed yet, leaving now will lead to duplicate sending messages. Are you sure you want to leave?",
                    DialogInterface.OnClickListener { dialog, which ->
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            setResult(Activity.RESULT_CANCELED, Intent())
                            finish()
                        }
                    })
        } else {
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.exchange_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_purpose)?.icon = when(purpose) {
            CRASH -> getBugIcon(IconHelper.SMALL_ICON_WHITE)
            ONLINEID -> getOnlineIdIcon(IconHelper.SMALL_ICON_WHITE)
            MESSAGE -> getAddMessageIcon(IconHelper.SMALL_ICON_WHITE)
            USERID -> getUserIdIcon(IconHelper.SMALL_ICON_WHITE)
            ANY_DATA -> getEventLogIcon(IconHelper.SMALL_ICON_WHITE)
            SERVER_CONFIG -> getServerIcon(IconHelper.SMALL_ICON_WHITE)
        }
        menu?.findItem(R.id.action_device)?.icon = when(target) {
            OFFLINE -> getOfflineDeviceIcon(IconHelper.SMALL_ICON_WHITE)
            ONLINE -> getOnlineDeviceIcon(IconHelper.SMALL_ICON_WHITE)
            ANY -> getEventLogIcon(IconHelper.SMALL_ICON_WHITE)
        }
        menu?.findItem(R.id.action_usecase_id)?.icon = getUserIdIcon(IconHelper.SMALL_ICON_WHITE) // TODO: ICON and show text?
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_purpose) {
            val text = "Transferred data is " + purpose.name
            UiHelper.showToastFromBackgroundTask(this, text)
        }
        return super.onOptionsItemSelected(item)
    }

}
