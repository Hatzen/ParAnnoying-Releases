package de.hartz.software.parannoying.offline.activities.offline

import android.content.DialogInterface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ExpandableListView
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.getTrashIcon
import de.hartz.software.parannoying.core.interfaces.di.air.gap.AirGapAdapter
import de.hartz.software.parannoying.offline.R
import de.hartz.software.parannoying.offline.adapters.view.EventOverviewAdapter
import javax.inject.Inject

class EventOverviewActivity : BaseOfflineActivity() {
    @Inject
    lateinit var airGapAdapter: AirGapAdapter
    lateinit var eventViewAdapter : EventOverviewAdapter
    private lateinit var listView: ExpandableListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_overview)

        listView = findViewById<ExpandableListView>(R.id.event_list)
        listView.emptyView = findViewById(R.id.emptyElement)

        app.offlineComponents.inject(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "App Events"
        // TODO: Mappign currently wont work for specific events, so listenerst wont work..

        updateInbox()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(de.hartz.software.parannoying.core.R.menu.delete_all_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(de.hartz.software.parannoying.core.R.id.action_delete_all)?.icon = getTrashIcon(IconHelper.SMALL_ICON_WHITE)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == de.hartz.software.parannoying.core.R.id.action_delete_all) {
            DialogHelper.showYesNoAlert(this, "Are you sure to delete all entries?", object: DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        Storage.deleteAllEvents()
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
        val eventsNewestToLast = Storage.readEvents().reversed()
        runOnUiThread(Runnable {
            eventViewAdapter = EventOverviewAdapter(eventsNewestToLast, this, airGapAdapter)
            listView.setAdapter(eventViewAdapter)
            eventViewAdapter.notifyDataSetChanged()
        })
    }

}
