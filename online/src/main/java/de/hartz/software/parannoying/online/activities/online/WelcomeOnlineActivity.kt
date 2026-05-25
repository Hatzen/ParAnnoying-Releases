package de.hartz.software.parannoying.online.activities.online

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import de.hartz.software.parannoying.core.activities.insecured.welcome.WelcomeActivity
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.extensions.launchActivity
import de.hartz.software.parannoying.core.fragments.welcome.WelcomePermissionsFragment
import de.hartz.software.parannoying.core.fragments.welcome.generic.WelcomeCheckBoxFragment
import de.hartz.software.parannoying.core.fragments.welcome.generic.WelcomeSimpleTextFragment
import de.hartz.software.parannoying.core.fragments.welcome.generic.WelcomeSimpleTextFragment.Companion.KEY_COLOR
import de.hartz.software.parannoying.core.helper.InitializationHelper
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.interfaces.di.air.gap.AirGapAdapter
import de.hartz.software.parannoying.online.adapters.FirebaseAdapter
import de.hartz.software.parannoying.online.fragments.welcome.GenerateOnlineIdFragment
import de.hartz.software.parannoying.online.fragments.welcome.WelcomeOnlineImportBackupFragment
import de.hartz.software.parannoying.online.interfaces.OnlineApplication
import de.hartz.software.parannoying.online.model.OnlineStorage
import javax.inject.Inject

class WelcomeOnlineActivity : AppIntro() {

    companion object {
        const val REQUEST_SCAN_BACKUP_ONLINE_ID = 1941
    }

    // TODO: This is needed as member for test. Maybe bad design?
    lateinit var generateOnlineIdFragment: GenerateOnlineIdFragment
    lateinit var importOnlineFragment: WelcomeOnlineImportBackupFragment

    private val onlineStorage get() = Storage as OnlineStorage
    @Inject
    lateinit var securityInterfaceHolder: SecurityInterfaceHolder
    @Inject
    lateinit var airGapAdapter: AirGapAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (app as OnlineApplication).onlineComponents.inject(this)

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
        var bundle : Bundle

        val hardwareEncryption = WelcomeSimpleTextFragment()
        bundle = Bundle()
        bundle.putInt(KEY_COLOR, de.hartz.software.parannoying.core.R.color.colorPrimary)
        hardwareEncryption.arguments = bundle
        bundle.putString(WelcomeSimpleTextFragment.KEY_TEXT,
                "For even more anonymisation between the both online devices it is highly recommended to use <b><a href='https://briarproject.org'>Briar</a></b> and its way to use the Tor network!")
        addSlide(hardwareEncryption)

        importOnlineFragment = WelcomeOnlineImportBackupFragment()
        bundle = Bundle()
        importOnlineFragment.arguments = bundle
        bundle.putInt(KEY_COLOR, de.hartz.software.parannoying.core.R.color.colorPrimary)
        bundle.putString(WelcomeOnlineImportBackupFragment.KEY_HEADER, "To connect an existing offline device click the button and scan the online id otherwise just continue")
        addSlide(importOnlineFragment)

        // Last slide. defined before last_slide-1 for reference reasons
        generateOnlineIdFragment = GenerateOnlineIdFragment()
        bundle = Bundle()
        bundle.putInt(KEY_COLOR, de.hartz.software.parannoying.core.R.color.colorPrimary)
        generateOnlineIdFragment.arguments = bundle

        // Offline or online Id for online device
        val useGoogleApi = FirebaseAdapter(onlineStorage).isGooglePlayServicesAvailable(this)
        onlineStorage.setUseGoogleApi(useGoogleApi)
        if (!onlineStorage.useGoogleApi) {
            val googleApiNotAvailable = WelcomeSimpleTextFragment()
            bundle = Bundle()
            bundle.putInt(KEY_COLOR, de.hartz.software.parannoying.core.R.color.colorAccent)
            googleApiNotAvailable.arguments = bundle
            bundle.putString(WelcomeSimpleTextFragment.KEY_TEXT,
                    "As your device dont support googles realtime api you have to share all scanned offline messages with any other app. Please consider using Briar as most other messengers will connect your identity to every message exchange!")
            addSlide(googleApiNotAvailable)
        } else {
            val useGoogleApiFragment = UseGoogleApiFragment()
            bundle = Bundle()
            bundle.putInt(KEY_COLOR, de.hartz.software.parannoying.core.R.color.colorPrimaryDark)
            bundle.putStringArray(WelcomeCheckBoxFragment.KEY_CHECKBOXTEXTS, arrayOf("Use google API"))
            bundle.putStringArray(WelcomeCheckBoxFragment.KEY_CHECKBOXTAGS, arrayOf("useAPI"))
            bundle.putString(WelcomeSimpleTextFragment.KEY_HEADER, "Deliver the messages with this app via google api or a foreign messenger via share")
            useGoogleApiFragment.arguments = bundle
            addSlide(useGoogleApiFragment)
            useGoogleApiFragment.generateOnlineIdFragment = generateOnlineIdFragment
            useGoogleApiFragment.storage = onlineStorage
        }

        addSlide(generateOnlineIdFragment)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        launchActivity<WelcomeActivity> {  }
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        // Decide what to do when the user clicks on "Skip"
        onFinish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        // Decide what to do when the user clicks on "Done"
        onFinish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        importOnlineFragment.onActivityResult(requestCode, resultCode, data)
    }

    fun onFinish() {
        // TODO: Delete history properly at least back button still works for some reason leading to this acitivty..
        val deleteHistory : (userIntent : Intent) -> Unit = {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
        }
        InitializationHelper.setInitialized(this)
        launchActivity<OnlineMainActivity>(init = deleteHistory)
        finish()
    }

    override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
        if (newFragment is GenerateOnlineIdFragment) {
            if (generateOnlineIdFragment?.initStarted == false) {
                generateOnlineIdFragment?.initStarted = true
                generateOnlineIdFragment?.tryCreatingNotificationId(generateOnlineIdFragment?.view as ViewGroup)
            }
        } else if (newFragment is WelcomePermissionsFragment) {
            // TODO: this doesnt seem to work or triggers at a wrong time..
            newFragment.requestOptionalPermissions()
        }
    }

}

// Anonymous and inner class crashes..
class UseGoogleApiFragment : WelcomeCheckBoxFragment() {

    var generateOnlineIdFragment: GenerateOnlineIdFragment? = null
    lateinit var storage: OnlineStorage

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mainContent =  super.onCreateView(inflater, container, savedInstanceState) as ViewGroup?
        val useGoogleApiCheckbox = mainContent?.findViewWithTag<CheckBox>("useAPI")!!
        useGoogleApiCheckbox.isChecked = storage.useGoogleApi
        useGoogleApiCheckbox.setOnClickListener {
            storage.setUseGoogleApi(useGoogleApiCheckbox.isChecked)
        }

        val additionalButton = mainContent.findViewById<Button>(de.hartz.software.parannoying.core.R.id.additional_button)
        additionalButton.visibility = View.VISIBLE
        additionalButton.text = "Or configure custom server"
        additionalButton.setOnClickListener {
            requireActivity().launchActivity<ServerConfigActivity> {  }
        }

        return mainContent
    }

    override fun canMoveFurther() : Boolean {
        return true
    }
}

