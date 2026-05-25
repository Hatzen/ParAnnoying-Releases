package de.hartz.software.parannoying.offline.fragments.welcome

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.app.ActivityCompat
import com.developer.filepicker.model.DialogConfigs
import com.developer.filepicker.model.DialogProperties
import com.developer.filepicker.view.FilePickerDialog
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.fragments.welcome.WelcomeBaseFragment
import de.hartz.software.parannoying.core.fragments.welcome.generic.WelcomeSimpleTextFragment
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.getBackupIcon
import de.hartz.software.parannoying.core.model.persistence.realm.RealmHelper
import de.hartz.software.parannoying.offline.R
import de.hartz.software.parannoying.offline.helper.ImportExportHelper
import de.hartz.software.parannoying.offline.interfaces.OfflineApplication
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject


open class WelcomeImportBackupFragment : WelcomeBaseFragment() {

    companion object {
        val KEY_HEADER = "KEY_HEADER"
    }

    @Inject
    lateinit var securityInterfaceHolder: SecurityInterfaceHolder
    @Inject
    lateinit var realmHelper: RealmHelper
    private var color: Int = 0
    private lateinit var heading: String

    private lateinit var importDialog: FilePickerDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (app as OfflineApplication).offlineComponents
            .inject(this)

        heading = arguments?.getString(KEY_HEADER) ?: ""
        color = arguments?.getInt(WelcomeSimpleTextFragment.KEY_COLOR) ?: 0
        if (savedInstanceState != null) {
            //now do something with savedInstanceState
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = inflater.inflate(R.layout.welcome_page_import, container, false)
        setText(contentView.findViewById<TextView>(de.hartz.software.parannoying.offline.R.id.welcomeHeading), heading)
        val importButton = contentView.findViewById<AppCompatImageButton>(de.hartz.software.parannoying.offline.R.id.chat_import)
        importButton.setImageDrawable(requireActivity().getBackupIcon(IconHelper.SMALL_ICON_WHITE))
        importButton.setOnClickListener {
            Log.v("Import Data", "Import button clicked")
            importOfflineData()
        }

        if (Storage.readSettings().hiddenSettings.developerMode) {
            setupDummyImport(contentView)
        }

        contentView.invalidate()
        return contentView
    }

    override fun backgroundColor(): Int {
        if (color == 0) {
            return super.backgroundColor()
        }
        return color
    }

    private fun setupDummyImport(view: View) {

        val dummy1 = view.findViewById<Button>(R.id.dummy_import1)
        dummy1.visibility = View.VISIBLE
        dummy1.setOnClickListener {
            importDummyFile("A")
        }
        val dummy2 = view.findViewById<Button>(R.id.dummy_import2)
        dummy2.visibility = View.VISIBLE
        dummy2.setOnClickListener {
            importDummyFile("B")
        }
        val dummy3 = view.findViewById<Button>(R.id.dummy_import3)
        dummy3.visibility = View.VISIBLE
        dummy3.setOnClickListener {
            importDummyFile("C")
        }
        val dummy4 = view.findViewById<Button>(R.id.dummy_import4)
        dummy4.visibility = View.VISIBLE
        dummy4.setOnClickListener {
            importDummyFile("D")
        }
    }

    fun importDummyFile(deviceName: String) {
        val activity = requireActivity()

        val fileName = "deviceData-offline-"+ deviceName

        val newTmpFile = File(activity.filesDir, fileName + ".txt")

        val inputstream = activity.assets.open("sample-data/"+ fileName)

        val outputStream = FileOutputStream(newTmpFile)
        val inputStream = inputstream
        val buf = ByteArray(1024)
        var bytesRead: Int
        while (inputStream.read(buf).also { bytesRead = it } > 0) {
            outputStream.write(buf, 0, bytesRead)
        }
        outputStream.close()

        ImportExportHelper(securityInterfaceHolder, realmHelper).startImportPlainRealmFile(newTmpFile, activity)
        // Finish activity otherwise it stays in stack even with deleteHistory flag.
        requireActivity().finish()
    }

    private fun importOfflineData() {
        val properties = DialogProperties()
        properties.selection_mode = DialogConfigs.SINGLE_MODE
        properties.selection_type = DialogConfigs.FILE_SELECT
        properties.root = File(DialogConfigs.DEFAULT_DIR)
        properties.error_dir = File(DialogConfigs.DEFAULT_DIR)
        properties.offset = File(DialogConfigs.DEFAULT_DIR)
        properties.extensions = null
        importDialog = FilePickerDialog(context, properties)
        importDialog.setTitle("Select the backup file")
        importDialog.setDialogSelectionListener {
            //files is the array of the paths of files selected by the Application User.
            ImportExportHelper(securityInterfaceHolder, realmHelper).importWithRequestUserPassword(File(it[0]), requireActivity())
        }
        if (ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startImport()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 123)
        }
    }

    //Add this method to show Dialog when the required permission has been granted to the app.
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            FilePickerDialog.EXTERNAL_READ_PERMISSION_GRANT -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startImport()
                } else {
                    //Permission has not been granted. Notify the user.
                    Toast.makeText(requireActivity(), "Permission is Required for getting list of files", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startImport() {
        Log.e("Import Data", "Import button clicked")
        importDialog.show()
    }

}