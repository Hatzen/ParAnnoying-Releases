package de.hartz.software.parannoying.core.activities.insecured.welcome

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import de.hartz.software.parannoying.core.R
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.helper.InitializationHelper
import de.hartz.software.parannoying.core.interfaces.di.ActivityProvider
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ChannelSupportAdapter
import de.hartz.software.parannoying.core.model.domain.DeviceRole
import de.hartz.software.parannoying.core.model.domain.settings.Channels
import de.hartz.software.parannoying.core.model.persistence.realm.RealmHelper
import javax.inject.Inject

class WelcomeActivity : AppIntro() {

    lateinit var deviceRoleFragment: Fragment

    @Inject
    lateinit var activityProvider: ActivityProvider
    @Inject
    lateinit var channelSupportChecker: ChannelSupportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // See manifest.
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        app.coreComponents.inject(this)

        initOnboardingActivity()

        Slides(this, app, Storage).initSlides()
    }

    fun addASlide(fragment: Fragment) {
        super.addSlide(fragment)
    }

    fun askPermissions(fragmentCount: Int, permissions: Array<String>) {
        askForPermissions(
                permissions = permissions,
                slideNumber = fragmentCount,
                required = true)
    }

    fun initOnboardingActivity() {

        // Toggle Indicator Visibility
        isIndicatorEnabled = true
        isColorTransitionsEnabled = true

        // Change Indicator Color
        setIndicatorColor(
            selectedIndicatorColor = R.color.colorPrimaryDark,
            unselectedIndicatorColor = R.color.colorAccent
        )

        // Switch from Dotted Indicator to Progress Indicator
        // TODO: This does not work in tests https://stackoverflow.com/a/33843737/8524651
        setProgressIndicator()
        // indicatorController = null

        // You can customize your parallax parameters in the constructors.
        /*
        // TODO: Nullpointer for title
        page.findViewById<TextView>(R.id.title).translationX = computeParallax(page, position, titlePF)
        page.findViewById<ImageView>(R.id.image).translationX = computeParallax(page, position, imagePF)

        setTransformer(
            AppIntroPageTransformerType.Parallax(
                titleParallaxFactor = 1.0,
                imageParallaxFactor = -1.0,
                descriptionParallaxFactor = 2.0
            ))
         */

        isSystemBackButtonLocked = true
    }

    override fun onStop() {
        super.onStop()
        // TODO: Make a more proper way of going back from online/ offline activity. But also keep this activity working if it is in background for more than 5 mins.
        // After rejoining the activity its mostly dump. looks like current slide and block are not restored..
        finish()
    }

    override fun onUserDeniedPermission(permissionName: String) {
        // User pressed "Deny" on the permission dialog
    }
    override fun onUserDisabledPermission(permissionName: String) {
        // TODO: Say user to go to settings.
        // User pressed "Deny" + "Don't ask again" on the permission dialog
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        // Decide what to do when the user clicks on "Skip"

        Storage.updateSettings {
            it.allowedChannels.remove(Channels.TEXT)
            it.allowedChannels.add(Channels.TEXT)
            it.primaryChannel = Channels.TEXT
        }
        Storage.deviceRole = (DeviceRole(DeviceRole.OFFLINE))
        val realRealmFileName = RealmHelper.getFileNameForIdAndRole(deviceRole = DeviceRole(DeviceRole.OFFLINE))
        InitializationHelper.setDeviceRole(this, realRealmFileName)

        onFinish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        // Decide what to do when the user clicks on "Done"
        onFinish()
    }

    fun onFinish() {

        if (isOfflineDevice()) {
            val realRealmFileName = RealmHelper.getFileNameForIdAndRole(deviceRole = DeviceRole(DeviceRole.OFFLINE))
            InitializationHelper.setDeviceRole(this, realRealmFileName)
            Storage.deviceRole = (DeviceRole(DeviceRole.OFFLINE))
        } else if (isOnlineDevice()) {
            val realRealmFileName = RealmHelper.getFileNameForIdAndRole(deviceRole = DeviceRole(DeviceRole.ONLINE))
            InitializationHelper.setDeviceRole(this, realRealmFileName)
            Storage.deviceRole = (DeviceRole(DeviceRole.ONLINE))
        }

        // TransferData from VolatileStorage to Offline or OnlineStorage
        val volatileStorage = Storage
        val settings = volatileStorage.readSettings()
        // Force recreation of WelcomeActivityProvider based on the device role.
        app.invalidateComponents()
        app.coreComponents.inject(this)

        Storage.deviceRole = (volatileStorage.deviceRole)
        Storage.updateSettings {
            it.allowedChannels = settings.allowedChannels
            it.primaryChannel = settings.primaryChannel
            it.screenSaver = settings.screenSaver
            it.hiddenSettings.developerMode = settings.hiddenSettings.developerMode
        }

        val welcomeClass = activityProvider.getWelcomeActivityClass()
        val classPath = welcomeClass.`package`.name + "." + welcomeClass.simpleName
        val intent = Intent(this, Class.forName(classPath)).apply {
            // Unable to go back again.
            // Dont do this as results from airgap wont call onactivity result.
            // flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
        }
        startActivity(intent)
        finish()
    }

    private fun isOnlineDevice() : Boolean {
        if (deviceRoleFragment.view == null) {
            return false
        }
        return deviceRoleFragment.requireView().findViewWithTag<RadioButton>("online")!!.isChecked
    }

    private fun isOfflineDevice() : Boolean {
        if (deviceRoleFragment.view == null) {
            return false
        }
        return deviceRoleFragment.requireView().findViewWithTag<RadioButton>("offline")!!.isChecked
    }
}