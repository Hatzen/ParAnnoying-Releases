package de.hartz.software.parannoying.air.gap.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import de.hartz.software.parannoying.air.gap.R
import de.hartz.software.parannoying.air.gap.fragments.exchange.AirGapFragment
import de.hartz.software.parannoying.air.gap.fragments.exchange.AirGapFragment.Companion.EXTRA_PERSIST_LAUNCH_OPTIONS
import de.hartz.software.parannoying.air.gap.fragments.exchange.DataReceiveFragment
import de.hartz.software.parannoying.air.gap.helpers.DatasetProcessor
import de.hartz.software.parannoying.air.gap.interfaces.di.ExchangeApp
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.helper.onboarding.Introducer
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.core.helper.ui.getOutboxIcon
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ActivityPurpose
import de.hartz.software.parannoying.core.interfaces.di.air.gap.DeviceTarget
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ILaunchOptions

class ReceiveActivity: AirGapActivity<DataReceiveFragment>() {

    override lateinit var dataExchangeChannelsFragment: DataReceiveFragment

    override val layoutResID: Int
        get() = R.layout.activity_exchange

    override fun onCreate(savedInstanceState: Bundle?) {
        dataExchangeChannelsFragment = DataReceiveFragment()
        (app as ExchangeApp).getActivityComponents(this)
                .inject(this)
        super.onCreate(savedInstanceState)

        target = intent.getSerializableExtra(AirGapFragment.EXTRA_TARGET) as DeviceTarget

    }

    override fun initIntroducer() {
        var intention: Introducer.SpecificContentForActivity? = null
        if (purpose == ActivityPurpose.MESSAGE) {
            intention = Introducer.SpecificContentForActivity.ScanMessage
        } else if (purpose == ActivityPurpose.ONLINEID) {
            intention = Introducer.SpecificContentForActivity.ScanNotification
        }  else if (purpose == ActivityPurpose.USERID) {
            intention = Introducer.SpecificContentForActivity.ScanUserId
        }
        intention.let {
            introducer.startIntroduction(it)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_role)?.icon = getOutboxIcon(IconHelper.SMALL_ICON_WHITE)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_role) {
            val text = "Receive data"
            UiHelper.showToastFromBackgroundTask(this, text)
        }
        if (item.itemId == R.id.action_device) {
            val text = "Receive data from " + target.name
            UiHelper.showToastFromBackgroundTask(this, text)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun handleAllDataProcessed() {
        val resultIntent = Intent()

        val launchOptions = intent.getSerializableExtra(EXTRA_PERSIST_LAUNCH_OPTIONS) as? ILaunchOptions
        resultIntent
                .putExtra(EXTRA_PERSIST_LAUNCH_OPTIONS, launchOptions)
                .putExtra(DatasetProcessor.RESULT_EXTRA_FILE_NAME, dataExchangeChannelsFragment.file.absolutePath)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

}
