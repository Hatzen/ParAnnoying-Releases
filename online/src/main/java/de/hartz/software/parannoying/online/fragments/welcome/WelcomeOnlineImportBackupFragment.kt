package de.hartz.software.parannoying.online.fragments.welcome

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import de.hartz.software.parannoying.air.gap.model.UseCases
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.fragments.welcome.WelcomeBaseFragment
import de.hartz.software.parannoying.core.fragments.welcome.generic.WelcomeSimpleTextFragment
import de.hartz.software.parannoying.core.helper.security.DataGeneratorHelper
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.getBackupIcon
import de.hartz.software.parannoying.core.interfaces.di.air.gap.AirGapAdapter
import de.hartz.software.parannoying.core.model.domain.DeviceRole
import de.hartz.software.parannoying.online.R
import de.hartz.software.parannoying.online.activities.online.WelcomeOnlineActivity
import de.hartz.software.parannoying.online.activities.online.WelcomeOnlineActivity.Companion.REQUEST_SCAN_BACKUP_ONLINE_ID
import de.hartz.software.parannoying.online.interfaces.OnlineApplication
import de.hartz.software.parannoying.online.model.OnlineStorage
import javax.inject.Inject


open class WelcomeOnlineImportBackupFragment : WelcomeBaseFragment() {

    @Inject
    lateinit var airGapAdapter: AirGapAdapter
    @Inject
    lateinit var securityInterfaceHolder: SecurityInterfaceHolder
    private val onlineStorage get() = Storage as OnlineStorage

    companion object {
        val KEY_HEADER = "KEY_HEADER"
        val KEY_IMPORT = "KEY_IMPORT"
    }

    private var color: Int = 0
    private lateinit var heading: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (app as OnlineApplication).onlineComponents
                .inject(this)

        heading = arguments?.getString(KEY_HEADER) ?: ""
        color = arguments?.getInt(WelcomeSimpleTextFragment.KEY_COLOR) ?: 0
        if (savedInstanceState != null) {
            //now do something with savedInstanceState
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.welcome_page_import, container, false)
        setText(view.findViewById<TextView>(de.hartz.software.parannoying.online.R.id.welcomeHeading), heading)
        val importButton = view.findViewById<AppCompatImageButton>(de.hartz.software.parannoying.online.R.id.chat_import)
        importButton.setImageDrawable(requireActivity().getBackupIcon(IconHelper.SMALL_ICON_WHITE))
        importButton.setOnClickListener {
            Log.v("Import Data", "Import button clicked")
            importOnlineData()
        }

        // TODO: Get rid of..
        val dummy1 = view.findViewById<Button>(de.hartz.software.parannoying.core.R.id.dummy_import1)
        dummy1.visibility = View.GONE
        val dummy2 = view.findViewById<Button>(de.hartz.software.parannoying.core.R.id.dummy_import2)
        dummy2.visibility = View.GONE
        val dummy3 = view.findViewById<Button>(de.hartz.software.parannoying.core.R.id.dummy_import3)
        dummy3.visibility = View.GONE
        val dummy4 = view.findViewById<Button>(de.hartz.software.parannoying.core.R.id.dummy_import4)
        dummy4.visibility = View.GONE

        view.invalidate()
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // TODO: Move back to activtiy doesnt get triggered here.
        super.onActivityResult(requestCode, resultCode, data)
        val result = airGapAdapter.onActivityResult(requestCode, resultCode, data)

        if (result.success && requestCode == REQUEST_SCAN_BACKUP_ONLINE_ID) {
            val onlineId = result.getSingleResult().exchangeData
            importOnline(onlineId)
        }
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun importOnline (result: String) {
        DataGeneratorHelper(securityInterfaceHolder)
            .storeFullOnlineUserDataForOfflineDevice(result, this.Storage)
        onlineStorage.persistDeviceRole(DeviceRole(DeviceRole.ONLINE))

        (requireActivity() as WelcomeOnlineActivity).onFinish()
    }

    override fun backgroundColor(): Int {
        if (color == 0) {
            return super.backgroundColor()
        }
        return color
    }

    private fun importOnlineData() {
        airGapAdapter.startReceive(UseCases.Online.ONLINEID_BACKUP_RECEIVE)

        // TODO: The real import is done in activity result.
    }

}