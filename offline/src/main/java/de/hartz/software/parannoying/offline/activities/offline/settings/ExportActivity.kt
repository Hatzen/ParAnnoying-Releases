package de.hartz.software.parannoying.offline.activities.offline.settings

import android.Manifest
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.hartz.software.parannoying.air.gap.model.UseCases
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.getAddIcon
import de.hartz.software.parannoying.core.helper.ui.getTrashIcon
import de.hartz.software.parannoying.core.interfaces.di.air.gap.AirGapAdapter
import de.hartz.software.parannoying.core.model.persistence.realm.RealmHelper
import de.hartz.software.parannoying.offline.R
import de.hartz.software.parannoying.offline.activities.offline.BaseOfflineActivity
import de.hartz.software.parannoying.offline.adapters.view.settings.FileListAdapter
import de.hartz.software.parannoying.offline.helper.ImportExportHelper
import java.io.File
import javax.inject.Inject


class ExportActivity : BaseOfflineActivity() {

    @Inject
    lateinit var securityInterfaceHolder: SecurityInterfaceHolder
    @Inject
    lateinit var airGapAdapter: AirGapAdapter
    @Inject
    lateinit var realmHelper: RealmHelper

    private lateinit var adapter: FileListAdapter
    private lateinit var directory: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app.offlineComponents.inject(this)
        setContentView(R.layout.activity_export)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // show back button
        supportActionBar?.title = "Backup overview"

        directory =  IOHelper.getBackupDir(this).apply { mkdirs() }

        val activity = this
        adapter = FileListAdapter(
            { file -> airGapAdapter.startSend(UseCases.CLEARTEXT_FILE_SEND.useFile(file.absolutePath)) },
            { file ->
                val intent = IOHelper.initShareFile(file, activity)
                if (intent == null) {
                    ActivityCompat.requestPermissions(activity,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), IOHelper.STORAGE_REQUEST_CODE)
                    return@FileListAdapter
                }
                activity.startActivity(intent)
            }
        )

        findViewById<RecyclerView>(R.id.file_list).apply {
            layoutManager = LinearLayoutManager(this@ExportActivity)
            adapter = this@ExportActivity.adapter
        }

        refreshFileList()
    }
    override fun onSupportNavigateUp() : Boolean {
        // Enable Back button.
        finish()
        return true
    }

    private fun refreshFileList() {
        val files = directory.listFiles()?.sortedBy { it.lastModified() } ?: emptyList()
        adapter.submitList(files)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.export_menu, menu)

        menu.findItem(R.id.action_create).icon = getAddIcon(IconHelper.SMALL_ICON_WHITE)
        menu.findItem(R.id.action_delete).icon = getTrashIcon(IconHelper.SMALL_ICON_WHITE)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_create -> {
            createExportFile()
            true
        }
        R.id.action_delete -> {
            if (adapter.getSelectedFiles().isNotEmpty()) {
                deleteSelectedFiles()
            } else {
                DialogHelper.showYesNoAlert(this, "Delete all backup files?", object: DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            directory.deleteRecursively()
                            refreshFileList()
                        }
                    }
                })
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun createExportFile() {
        val progressBar = findViewById<FrameLayout>(R.id.progress_overlay)
        progressBar.visibility = View.VISIBLE
        Handler(Looper.getMainLooper()).post {
            ImportExportHelper(securityInterfaceHolder, realmHelper)
                .exportWithRequestUserPassword(this, {
                    runOnUiThread {
                        refreshFileList()
                        progressBar.visibility = View.GONE
                    }
                })

        }
    }

    private fun deleteSelectedFiles() {
        adapter.getSelectedFiles().forEach { it.delete() }
        adapter.clearSelection()
        refreshFileList()
    }
}