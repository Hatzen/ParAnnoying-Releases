package de.hartz.software.parannoying.offline.activities.offline

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import de.hartz.software.parannoying.air.gap.model.UseCases
import de.hartz.software.parannoying.air.gap.model.UseCases.FORWARD_RECEIVE_DATA
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.core.helper.ui.getPlusIcon
import de.hartz.software.parannoying.core.helper.ui.getTrashIcon
import de.hartz.software.parannoying.core.interfaces.di.air.gap.AirGapAdapter
import de.hartz.software.parannoying.offline.R
import de.hartz.software.parannoying.offline.adapters.view.ForwardOverviewAdapter
import de.hartz.software.parannoying.offline.databinding.ActivityForwardOverviewBinding
import de.hartz.software.parannoying.offline.model.domain.events.ForwardDataset
import javax.inject.Inject

class ForwardOverviewActivity : BaseOfflineActivity() {

    @Inject
    lateinit var airGapAdapter: AirGapAdapter
    @Inject
    lateinit var securityInterfaceHolder: SecurityInterfaceHolder
    lateinit var forwardOverviewAdapter : ForwardOverviewAdapter

    private lateinit var listView: ListView
    private var selectedPersistenceId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forward_overview)
        val binding = ActivityForwardOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app.offlineComponents.inject(this)

        val fabAddDataset = binding.addForwardDataset
        fabAddDataset.setImageDrawable(getPlusIcon(IconHelper.MEDIUM_ICON_WHITE))
        fabAddDataset.setOnClickListener { view ->
            airGapAdapter.startReceive(FORWARD_RECEIVE_DATA)
        }
        fabAddDataset.setOnLongClickListener {
            Toast.makeText(this, "Add message or userId to store and forward", Toast.LENGTH_LONG).show()
            true
        }

        listView = findViewById<ListView>(R.id.forward_items_list)
        listView.emptyView = findViewById(R.id.emptyElement)

        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val forwardDataset = Storage.readForwardDatasets().reversed()[position]

            selectedPersistenceId = forwardDataset.persistenceId

            airGapAdapter.startSend(UseCases.FORWARD_SEND_DATA.useText(forwardDataset.data))

        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Forward data"

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
                        Storage.deleteAllForwardDataset()
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val exchangeResult = airGapAdapter.onActivityResult(requestCode, resultCode, data)
        val useCase = exchangeResult.getUseCase()

        when (useCase) {
            UseCases.FORWARD_RECEIVE_DATA -> {
                if (exchangeResult.result?.count() ?: 0 == 0) {
                    UiHelper.showToastFromBackgroundTask(this, "Not a valid message to forward.")
                    return
                }
                val dataset = ForwardDataset(exchangeResult.getSingleResult().exchangeData, null)
                DialogHelper.showInputDialog(
                    this,
                    "Enter a note to show besides the data. DO NOT enter validationtoken, real names or any sensitiv data.",
                    true,
                    object: DialogHelper.InputDialogCallback() {
                        override fun onCancel() {
                            Storage.addForwardDatasets(dataset)
                            updateInbox()
                        }
                        override fun onFinish(input: String) {
                            dataset.note = input
                            Storage.addForwardDatasets(dataset)
                            updateInbox()
                        }
                    }
                )
            }
            UseCases.FORWARD_SEND_DATA -> {
                val item = Storage.readForwardDatasets()
                    .find { it.persistenceId == selectedPersistenceId }!!
                Storage.deleteForwardDataset(item)
            }
        }

        updateInbox()
        return super.onActivityResult(requestCode, resultCode, data);
    }

    fun updateInbox() {
        val eventsNewestToLast = Storage.readForwardDatasets().reversed()
        runOnUiThread(Runnable {
            forwardOverviewAdapter = ForwardOverviewAdapter(eventsNewestToLast, securityInterfaceHolder, this)
            listView.adapter = forwardOverviewAdapter
            forwardOverviewAdapter.notifyDataSetChanged()
        })
    }

}
