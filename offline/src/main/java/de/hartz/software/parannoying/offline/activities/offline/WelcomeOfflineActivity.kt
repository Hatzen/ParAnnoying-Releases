package de.hartz.software.parannoying.offline.activities.offline

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.github.appintro.AppIntro
import com.scottyab.rootbeer.RootBeer
import com.tingyik90.snackprogressbar.SnackProgressBar
import com.tingyik90.snackprogressbar.SnackProgressBarManager
import de.hartz.software.parannoying.core.activities.insecured.welcome.WelcomeActivity
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.extensions.launchActivity
import de.hartz.software.parannoying.core.fragments.ConnectionCheckStatusFragment
import de.hartz.software.parannoying.core.fragments.welcome.WelcomeLoadingFragment
import de.hartz.software.parannoying.core.fragments.welcome.generic.WelcomeInputFragment
import de.hartz.software.parannoying.core.fragments.welcome.generic.WelcomeSimpleTextFragment
import de.hartz.software.parannoying.core.fragments.welcome.generic.WelcomeSimpleTextFragment.Companion.KEY_COLOR
import de.hartz.software.parannoying.core.helper.InitializationHelper
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.model.domain.DeviceRole
import de.hartz.software.parannoying.offline.fragments.welcome.WelcomeImportBackupFragment
import de.hartz.software.parannoying.offline.fragments.welcome.WelcomeReceiveFragment
import de.hartz.software.parannoying.offline.helper.guard.ConnectionGuard
import de.hartz.software.parannoying.offline.helper.security.DialogCreationHelper
import de.hartz.software.parannoying.offline.model.OfflineStorage
import javax.inject.Inject


class WelcomeOfflineActivity : AppIntro(), de.hartz.software.parannoying.air.gap.interfaces.exchange.ReceiveFragmentResultListener {

    lateinit var backupFragment: WelcomeImportBackupFragment // currently only needed to tests
    private var userName: String? = null
    private lateinit var firstFragment: WelcomeInputFragment

    @Inject
    lateinit var securityInterfaceHolder: SecurityInterfaceHolder
    val offlineStorage get() = Storage as OfflineStorage
    var fullOnlineId: String? = null

    val mainContentView: View get() {
        return findViewById<View>(android.R.id.content).getRootView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        initOnboardingActivity()
        setupTexts()
    }

    fun initOnboardingActivity() {
        isWizardMode = true

        // Toggle Indicator Visibility
        isIndicatorEnabled = true
        isColorTransitionsEnabled = true

        // Change Indicator Color
        // TODO: get color from resource
        setIndicatorColor(
            selectedIndicatorColor = de.hartz.software.parannoying.core.R.color.colorPrimaryDark,
            unselectedIndicatorColor = de.hartz.software.parannoying.core.R.color.colorAccent
        )

        // Switch from Dotted Indicator to Progress Indicator
        setProgressIndicator()

        isSystemBackButtonLocked = true
    }

    private fun setupTexts() {
        var bundle: Bundle

        if (!IOHelper.isHardwareEncrypted(this)) {
            val hardwareEncryption = WelcomeSimpleTextFragment()
            bundle = Bundle()
            bundle.putInt(KEY_COLOR, de.hartz.software.parannoying.core.R.color.colorPrimary)
            hardwareEncryption.arguments = bundle
            bundle.putString(WelcomeSimpleTextFragment.KEY_HEADER, "Hardware encryption")
            bundle.putString(WelcomeSimpleTextFragment.KEY_TEXT,
                    "Your device supports hardware encryption but it is not used yet. As long as you are using this app you should definitely consider using it!")
            addSlide(hardwareEncryption)
        }

        val rootBeer = RootBeer(this)
        if (rootBeer.isRooted) {
            val rooted = WelcomeSimpleTextFragment()
            bundle = Bundle()
            bundle.putInt(KEY_COLOR, de.hartz.software.parannoying.core.R.color.colorPrimary)
            rooted.arguments = bundle
            bundle.putString(WelcomeSimpleTextFragment.KEY_HEADER,
                    "Root")
            bundle.putString(WelcomeSimpleTextFragment.KEY_TEXT,
                    "Your device seems to be rooted. This can deactivate security features and endanger the security. It is safer to use stock roms only. ")
            addSlide(rooted)
        }

        val connectionCheckFragment = ConnectionCheckFragment()
        bundle = Bundle()
        connectionCheckFragment.arguments = bundle
        bundle.putInt(KEY_COLOR, de.hartz.software.parannoying.core.R.color.colorPrimary)
        bundle.putString(WelcomeSimpleTextFragment.KEY_HEADER,
                "Go offline")
        bundle.putString(WelcomeSimpleTextFragment.KEY_TEXT,
                "Now turn off every insecure connection. Like Network, USB etc. Keep in mind: As soon as you establish an insecure connection all app data will be wiped!")
        addSlide(connectionCheckFragment)

        backupFragment = WelcomeImportBackupFragment()
        bundle = Bundle()
        backupFragment.arguments = bundle
        bundle.putInt(KEY_COLOR, de.hartz.software.parannoying.core.R.color.colorPrimary)
        bundle.putString(WelcomeImportBackupFragment.KEY_HEADER, "To recover an old backup file click the button otherwise just continue")
        addSlide(backupFragment)

        firstFragment = WelcomeInputFragment()
        bundle = Bundle()
        bundle.putInt(KEY_COLOR, de.hartz.software.parannoying.core.R.color.blue_gray)
        bundle.putString(WelcomeSimpleTextFragment.KEY_TEXT,
                "Enter the <b>pseudonym</b> for your user")
        firstFragment.arguments = bundle
        addSlide(firstFragment)

        val prepareScan = WelcomeSimpleTextFragment()
        bundle = Bundle()
        bundle.putInt(KEY_COLOR, de.hartz.software.parannoying.core.R.color.colorPrimaryDark)
        bundle.putString(WelcomeSimpleTextFragment.KEY_TEXT,
                "In the next step you have to transfer your onlineId from your already created online device to this offline device by a selected channel.")
        prepareScan.arguments = bundle
        addSlide(prepareScan)

        val seventh = WelcomeReceiveFragment()
        bundle = Bundle()
        bundle.putInt(KEY_COLOR, de.hartz.software.parannoying.core.R.color.colorPrimary)
        seventh.arguments = bundle
        addSlide(seventh)

        val fourth = WelcomeLoadingFragment()
        bundle = Bundle()
        bundle.putInt(KEY_COLOR, de.hartz.software.parannoying.core.R.color.colorAccent)
        fourth.arguments = bundle
        addSlide(fourth)

    }

    override fun passResult(result: String, caller: de.hartz.software.parannoying.air.gap.fragments.exchange.AbstractExchangeChannelFragment) {
        fullOnlineId = result
        initUser(fullOnlineId!!)
    }

    fun hasResult(): Boolean {
        return fullOnlineId != null
    }
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        launchActivity<WelcomeActivity> {  }
    }

    private fun initUser(fullOnlineId: String) {
        val snackProgressBarManager by lazy { SnackProgressBarManager(mainContentView, lifecycleOwner = this) }
        snackProgressBarManager.setOverlayLayoutColor(de.hartz.software.parannoying.offline.R.color.colorPrimary)
        userName = firstFragment.requireView().findViewById<TextView>(de.hartz.software.parannoying.offline.R.id.username).text.toString()

        val context = this

        // TODO: Must be moved to static/ companion class.
        @SuppressLint("StaticFieldLeak")
        val task = object: AsyncTask<Void, Void, Exception>() {
            override fun doInBackground(vararg params: Void?): Exception? {
                try {
                    val snackProgressBar =
                            SnackProgressBar(SnackProgressBar.TYPE_HORIZONTAL, "Creating user data..")
                                    .setShowProgressPercentage(true)

                    runOnUiThread {
                        snackProgressBarManager.show(snackProgressBar, SnackProgressBarManager.LENGTH_INDEFINITE)
                    }
                    var currentProgress  = 0
                    DialogCreationHelper(context, securityInterfaceHolder).createCurrentUserData(userName!!, fullOnlineId, this@WelcomeOfflineActivity) {
                        currentProgress += it
                        runOnUiThread {
                            snackProgressBarManager.setProgress(currentProgress)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(javaClass.simpleName, "Error evaluating image", e)
                    return e
                }
                return null
            }

            override fun onPostExecute(result: Exception?) {
                super.onPostExecute(result)
                snackProgressBarManager.dismissAll()

                if (result != null) {
                    if (result is IllegalArgumentException) {
                        Toast.makeText(this@WelcomeOfflineActivity, "The OnlineId seems not to be valid", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@WelcomeOfflineActivity, "Error evaluated image: please try again. Might be a unaccurate scanned code", Toast.LENGTH_LONG).show()
                    }
                    return
                }
                //finished show:
                storeUserData()
                val deleteHistory : (userIntent: Intent) -> Unit = {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                InitializationHelper.setInitialized(context)
                launchActivity<OfflineMainActivity>(init = deleteHistory)
            }
        }.execute()

        CreateUserTask().execute()
    }

    private fun storeUserData() {
        offlineStorage.persistDeviceRole(DeviceRole(DeviceRole.OFFLINE))
        // TODO: do we need to persist currentuser at this point? probably not but we did before convertion of storage to repo
    }

    companion object {
        class CreateUserTask: AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                return null
            }
        }
    }
}


class ConnectionCheckFragment : WelcomeSimpleTextFragment() {

    override fun canMoveFurther() : Boolean {
        return (Storage as OfflineStorage).readSettings().hiddenSettings.developerMode || !ConnectionGuard.isConnected(requireContext())
        // return !ConnectionGuard.isConnected(requireContext())
    }

    override fun cantMoveFurtherErrorMessage(): String {
        ConnectionCheckStatusFragment().show(requireActivity().supportFragmentManager, "ConnectionCheck")
        return "Cut off internet and usb connection to go on."
    }
}
