package de.hartz.software.parannoying.air.gap.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import de.hartz.software.parannoying.air.gap.R
import de.hartz.software.parannoying.air.gap.fragments.exchange.DataSendFragment
import de.hartz.software.parannoying.air.gap.interfaces.di.ExchangeApp
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.helper.onboarding.Introducer
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.core.helper.ui.getInboxIcon
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ActivityPurpose

// TODO: This doesnt start on the firephone for crashes.. :: Maybe caused by blur layout?
open class SendActivity: AirGapActivity<DataSendFragment>() {

    override val layoutResID: Int
        get() = R.layout.activity_exchange
    override lateinit var dataExchangeChannelsFragment: DataSendFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        dataExchangeChannelsFragment = DataSendFragment()
        (app as ExchangeApp).getActivityComponents(this)
                .inject(this)

        super.onCreate(savedInstanceState)

        title = "Send $purpose to $target"
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
        menu?.findItem(R.id.action_role)?.icon = getInboxIcon(IconHelper.SMALL_ICON_WHITE)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_role) {
            val text = "Send data"
            UiHelper.showToastFromBackgroundTask(this, text)
        }
        if (item.itemId == R.id.action_device) {
            val text = "Send data to " + target.name
            UiHelper.showToastFromBackgroundTask(this, text)
        }

        return super.onOptionsItemSelected(item)
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        throw UnsupportedOperationException("Sending should not depend on receiving new items via intent")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Needed to be called as BluetoothHelper#startDiscoverActivity relies on the activity.
        // TODO: Refactore..
        dataExchangeChannelsFragment.currentChannelFragment?.onActivityResult(requestCode, resultCode, data)

    }
}
