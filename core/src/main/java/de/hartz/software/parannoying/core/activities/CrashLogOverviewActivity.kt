package de.hartz.software.parannoying.core.activities

import android.content.DialogInterface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import de.hartz.software.parannoying.core.R
import de.hartz.software.parannoying.core.adapter.CrashLogOverviewAdapter
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.getTrashIcon
import de.hartz.software.parannoying.core.interfaces.di.StorageInterface
import de.hartz.software.parannoying.core.interfaces.di.air.gap.AirGapAdapter
import javax.inject.Inject

class CrashLogOverviewActivity : BaseActivity() {
    @Inject
    lateinit var securityInterfaceHolder: SecurityInterfaceHolder
    @Inject
    lateinit var airGapAdapter: AirGapAdapter
    @Inject
    lateinit var storage: StorageInterface<*, *>
    lateinit var eventViewAdapter : CrashLogOverviewAdapter
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crashlog_overview)

        listView = findViewById(R.id.crashlog_list)
        listView.emptyView = findViewById(R.id.emptyElement)

        app.coreComponents.inject(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Crashlogs"

        updateInbox()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.delete_all_menu, menu)
        return true
    }
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_delete_all)?.icon = getTrashIcon(IconHelper.SMALL_ICON_WHITE)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_delete_all) {
            DialogHelper.showYesNoAlert(this, "Are you sure to delete all entries?", object: DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        storage.deleteAllCrashlogs()
                        updateInbox()
                    }
                }

            })
        }
        if (item.itemId == android.R.id.home) {
            super.finish()
        }
        return super.onOptionsItemSelected(item)
    }

    fun updateInbox() {
        val eventsNewestToLast = Storage.crashLog.reversed()
        runOnUiThread(Runnable {
            eventViewAdapter = CrashLogOverviewAdapter(eventsNewestToLast, this, securityInterfaceHolder,  airGapAdapter)
            listView.setAdapter(eventViewAdapter)
            eventViewAdapter.notifyDataSetChanged()
        })
    }

}
