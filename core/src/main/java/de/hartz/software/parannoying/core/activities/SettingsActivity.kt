package de.hartz.software.parannoying.core.activities

import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.github.omadahealth.lollipin.lib.managers.AppLock
import com.github.omadahealth.lollipin.lib.managers.LockManager
import de.hartz.software.parannoying.core.R
import de.hartz.software.parannoying.core.activities.insecured.SplashActivity
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.extensions.launchActivity
import de.hartz.software.parannoying.core.fragments.SettingsFragment
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.getBluetoothIcon
import de.hartz.software.parannoying.core.helper.ui.getBugIcon
import de.hartz.software.parannoying.core.helper.ui.getCameraIcon
import de.hartz.software.parannoying.core.helper.ui.getNfcIcon
import de.hartz.software.parannoying.core.helper.ui.getPinIcon
import de.hartz.software.parannoying.core.helper.ui.getSDCardIcon
import de.hartz.software.parannoying.core.helper.ui.getSoundIcon
import de.hartz.software.parannoying.core.helper.ui.getTextIcon
import de.hartz.software.parannoying.core.helper.ui.getVideoIcon
import de.hartz.software.parannoying.core.interfaces.di.ActivityProvider
import de.hartz.software.parannoying.core.interfaces.di.ApplicationInfoComponent
import de.hartz.software.parannoying.core.interfaces.di.StorageInterface
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ChannelSupportAdapter
import de.hartz.software.parannoying.core.model.domain.settings.Channels
import de.hartz.software.parannoying.core.model.domain.settings.QrVideoSpeed
import de.hartz.software.parannoying.core.model.domain.settings.SoundSpectrumAndSpeed
import javax.inject.Inject


// https://developer.android.com/guide/topics/ui/settings
// https://www.androidhive.info/2017/07/android-implementing-preferences-settings-screen/
abstract class SettingsActivity : BaseActivity() {

    lateinit var settingsFragment: SettingsFragment
    abstract val preferencesResource: Int
    var hitmarker: Int = 0

    @Inject
    lateinit var applicationInfoComponent: ApplicationInfoComponent
    @Inject
    lateinit var activityProvider: ActivityProvider
    @Inject
    lateinit var channelSupportChecker: ChannelSupportAdapter


    val settings get() = Storage.readSettings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(de.hartz.software.parannoying.core.R.layout.settings_activity)

        settingsFragment = SettingsFragment.newInstance(preferencesResource)
        supportFragmentManager
                .beginTransaction()
                .replace(de.hartz.software.parannoying.core.R.id.settings, settingsFragment)
                .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Needed otherwise findPreferences always returns null.
        supportFragmentManager.executePendingTransactions()

        init()
        initForBoth()
        initGeneral()
        disableUnsupported()
    }

    override fun onSupportNavigateUp() : Boolean {
        // Enable Back button.
        finish()
        return true
    }

    private fun initForBoth() {
        initChannelSettings()
        initAdvancedChannelSettings()

        val versionPreference = settingsFragment.findPreference<Preference>("version")
        versionPreference?.summary = applicationInfoComponent.getVersion()
        versionPreference?.setOnPreferenceClickListener(this::handleVersionClick)

        val screenSaverPreference = settingsFragment.findPreference<CheckBoxPreference>("screensaver_active")
        screenSaverPreference?.isChecked = settings.screenSaver
        screenSaverPreference?.setOnPreferenceClickListener {
            Storage.updateSettings { it.screenSaver = !it.screenSaver }
            true
        }


        val hideIconPreference = settingsFragment.findPreference<CheckBoxPreference>("fake_entry_activity_active")!!
        hideIconPreference?.isChecked = settings.useFakeEntryActivity
        hideIconPreference?.setOnPreferenceClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // TODO: Evaluate. Probably there is no other way..
                // https://stackoverflow.com/a/22754642/8524651
                (DialogHelper.showAlert(this, "As of Android 10 / Q this setting will not work an every device."))
            }

            // activity which is first time open in manifiest file which is declare as <category android:name="android.intent.category.LAUNCHER" />
            val p = packageManager
            val fakeEntryActivityClass = activityProvider.getFakeEntryActivityClass()
            val splashComponent = ComponentName(this, SplashActivity::class.java)
            val fakeComponent = ComponentName(this, fakeEntryActivityClass)
            Storage.updateSettings { it.useFakeEntryActivity = !it.useFakeEntryActivity }
            if (settings.useFakeEntryActivity) {
                p.setComponentEnabledSetting(fakeComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
                p.setComponentEnabledSetting(splashComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
            } else {
                p.setComponentEnabledSetting(splashComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
                p.setComponentEnabledSetting(fakeComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
            }
            true
        }
        val iconDrawable = ContextCompat.getDrawable(this, R.mipmap.ic_launcher_fake_round)
        hideIconPreference.icon = iconDrawable

        val cleanPreference = settingsFragment.findPreference<Preference>("clean")
        cleanPreference?.setOnPreferenceClickListener {
            DialogHelper.showYesNoAlert(this, "Are you sure to wipe all app data? Including messages, users and your own userId.", object: DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        StorageInterface.deleteAll(this@SettingsActivity)
                        // TODO: Better wipe device? https://developer.android.com/reference/android/app/admin/DevicePolicyManager#wipeDevice(int)
                    }
                }

            })
            true
        }

        // TODO: This doesnt seem to work.. Pin is set only sometimes and also not recognized by disable Pincode check.
        val pinPreference = settingsFragment.findPreference<Preference>("pin")!!
        pinPreference.setOnPreferenceClickListener {
            val passParams : (userIntent : Intent) -> Unit = {
                it.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK)
            }
            launchActivity<CustomPinActivity>(init = passParams)
            true
        }
        pinPreference.icon = this.getPinIcon(IconHelper.SMALL_ICON_ACCENT)


        val removePinPreference = settingsFragment.findPreference<Preference>("remove_pin")
        removePinPreference?.setOnPreferenceClickListener {
            val passParams : (userIntent : Intent) -> Unit = {
                it.putExtra(AppLock.EXTRA_TYPE, AppLock.DISABLE_PINLOCK)
            }
            launchActivity<CustomPinActivity>(init = passParams)
            true
        }

        val crashlogPreference = settingsFragment.findPreference<Preference>("crashlog")!!
        crashlogPreference.setOnPreferenceClickListener {
            launchActivity<CrashLogOverviewActivity>()
            true
        }
        crashlogPreference.icon = this.getBugIcon(IconHelper.SMALL_ICON_ACCENT)

    }

    private fun handleVersionClick(preference: Preference): Boolean {
        val welcomeClass = activityProvider.getHiddenSettingsActivityClass()
        val classPath = welcomeClass.`package`.name + "." + welcomeClass.simpleName
        val intent = Intent(this, Class.forName(classPath))
        startActivity(intent)
        if (settings.hiddenSettings.developerOptionsActivated) {
            val welcomeClass = activityProvider.getHiddenSettingsActivityClass()
            val classPath = welcomeClass.`package`.name + "." + welcomeClass.simpleName
            val intent = Intent(this, Class.forName(classPath))
            startActivity(intent)
        }
        MediaPlayer.create(this@SettingsActivity, R.raw.coin_sound).start()
        hitmarker++
        when {
            hitmarker in 6..9 -> Toast.makeText(this@SettingsActivity, "" + hitmarker, Toast.LENGTH_SHORT).show()
            hitmarker > 9 -> {
                Toast.makeText(this@SettingsActivity, "Developermode enabled", Toast.LENGTH_LONG).show()
                Storage.updateSettings { it.hiddenSettings.developerOptionsActivated = true }
            }
        }
        return true
    }

    abstract fun init()

    abstract fun initGeneral()

    abstract fun disableUnsupported()
    private fun initChannelSettings () {
        val settings = Storage.readSettings()

        val cameraPreference = settingsFragment.findPreference<SwitchPreferenceCompat>("camera")
        cameraPreference?.icon = getCameraIcon(IconHelper.SMALL_ICON_ACCENT)
        cameraPreference?.isChecked = settings.allowedChannels.contains(Channels.CAMERA)
        cameraPreference?.setOnPreferenceChangeListener { preference: Preference, any: Any ->
            Storage.updateSettings {
                if (cameraPreference.isChecked) {
                    it.allowedChannels.remove(Channels.CAMERA)
                } else {
                    it.allowedChannels.add(Channels.CAMERA)
                }
                cameraPreference.isChecked = !cameraPreference.isChecked
                updatePrimaryChannel()
            }
            true
        }

        val nfcPreference = settingsFragment.findPreference<SwitchPreferenceCompat>("nfc")
        nfcPreference?.icon = getNfcIcon(IconHelper.SMALL_ICON_ACCENT)
        nfcPreference?.isChecked = settings.allowedChannels.contains(Channels.NFC)
        nfcPreference?.setOnPreferenceChangeListener { preference: Preference, any: Any ->
            Storage.updateSettings {
                if (nfcPreference.isChecked) {
                    it.allowedChannels.remove(Channels.NFC)
                } else {
                    it.allowedChannels.add(Channels.NFC)
                }
                nfcPreference.isChecked = !nfcPreference.isChecked
                updatePrimaryChannel()
            }
            true
        }

        if (!channelSupportChecker.isNFCSupported()) {
            nfcPreference?.isEnabled = false
        }

        val bluetoothPreference = settingsFragment.findPreference<SwitchPreferenceCompat>("bluetooth")
        bluetoothPreference?.icon = getBluetoothIcon(IconHelper.SMALL_ICON_ACCENT)
        bluetoothPreference?.isChecked = settings.allowedChannels.contains(Channels.BLUETOOTH)
        bluetoothPreference?.setOnPreferenceChangeListener { preference: Preference, any: Any ->
            Storage.updateSettings {
                if (bluetoothPreference.isChecked) {
                    it.allowedChannels.remove(Channels.BLUETOOTH)
                } else {
                    it.allowedChannels.add(Channels.BLUETOOTH)
                }
                bluetoothPreference.isChecked = !bluetoothPreference.isChecked
                updatePrimaryChannel()
            }
            true
        }
        if (!channelSupportChecker.isBluetoothSupported()) {
            bluetoothPreference?.isEnabled = false
        }

        val textPreference = settingsFragment.findPreference<SwitchPreferenceCompat>("text")
        textPreference?.icon = getTextIcon(IconHelper.SMALL_ICON_ACCENT)
        textPreference?.isChecked = settings.allowedChannels.contains(Channels.TEXT)
        textPreference?.setOnPreferenceChangeListener { preference: Preference, any: Any ->
            Storage.updateSettings {
                if (textPreference.isChecked) {
                    it.allowedChannels.remove(Channels.TEXT)
                } else {
                    it.allowedChannels.add(Channels.TEXT)
                }
                textPreference.isChecked = !textPreference.isChecked
                updatePrimaryChannel()
            }
            true
        }

        val soundPreference = settingsFragment.findPreference<SwitchPreferenceCompat>("sound")
        soundPreference?.icon = getSoundIcon(IconHelper.SMALL_ICON_ACCENT)
        soundPreference?.isChecked = settings.allowedChannels.contains(Channels.SOUND)
        soundPreference?.setOnPreferenceChangeListener { preference: Preference, any: Any ->
            Storage.updateSettings {
                if (soundPreference.isChecked) {
                    it.allowedChannels.remove(Channels.SOUND)
                } else {
                    it.allowedChannels.add(Channels.SOUND)
                }
                soundPreference.isChecked = !soundPreference.isChecked
                updatePrimaryChannel()
            }
            true
        }
        if (!channelSupportChecker.isSoundSupported()) {
            soundPreference?.isEnabled = false
        }

        val videoPreference = settingsFragment.findPreference<SwitchPreferenceCompat>("video")
        videoPreference?.icon = getVideoIcon(IconHelper.SMALL_ICON_ACCENT)
        videoPreference?.isChecked = settings.allowedChannels.contains(Channels.VIDEO)
        videoPreference?.setOnPreferenceChangeListener { preference: Preference, any: Any ->
            Storage.updateSettings {
                if (videoPreference.isChecked) {
                    it.allowedChannels.remove(Channels.VIDEO)
                } else {
                    it.allowedChannels.add(Channels.VIDEO)
                }
                videoPreference.isChecked = !videoPreference.isChecked
                updatePrimaryChannel()
            }
            true
        }
        if (!channelSupportChecker.isCameraSupported()) {
            videoPreference?.isEnabled = false
        }

        val sdPreference = settingsFragment.findPreference<SwitchPreferenceCompat>("sd")
        sdPreference?.icon = getSDCardIcon(IconHelper.SMALL_ICON_ACCENT)
        sdPreference?.isChecked = settings.allowedChannels.contains(Channels.SD_CARD)
        sdPreference?.setOnPreferenceChangeListener { preference: Preference, any: Any ->
            Storage.updateSettings {
                if (sdPreference.isChecked) {
                    it.allowedChannels.remove(Channels.SD_CARD)
                } else {
                    it.allowedChannels.add(Channels.SD_CARD)
                }
                sdPreference.isChecked = !sdPreference.isChecked
                updatePrimaryChannel()
            }
            true
        }
        if (!channelSupportChecker.isSDSupported()) {
            sdPreference?.isEnabled = false
        }

        val primaryChannelPreference = settingsFragment.findPreference<ListPreference>("main_channel")
        updatePrimaryChannel()
        primaryChannelPreference?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            Storage.updateSettings {
                val index = resources.getStringArray(R.array.channel_values).indexOf(newValue)
                it.primaryChannel = Channels.getByChannel(index)
            }
            true
        }
    }

    private fun updatePrimaryChannel() {
        // TODO: Needs more extensive testing. What happens if Primary channel becomes unavailable?

        val primaryChannelPreference = settingsFragment.findPreference<ListPreference>("main_channel")

        val valueList = mutableListOf<CharSequence>()
        val entryList = mutableListOf<CharSequence>()
        val allEntries = resources.getStringArray(R.array.channel_options)
        val allValues = resources.getStringArray(R.array.channel_values)

        val listOfChannelsInOrder = listOf(Channels.CAMERA, Channels.NFC, Channels.BLUETOOTH, Channels.TEXT, Channels.SOUND, Channels.VIDEO,Channels.SD_CARD)
        val expectedListCount = listOfChannelsInOrder.size + 1
        if (expectedListCount == allValues.size && expectedListCount == entryList.size) {
            throw RuntimeException("Channels dont match main channels $expectedListCount, ${allValues.size}, ${entryList.size}")
        }
        listOfChannelsInOrder.forEachIndexed {
            index, item ->
            run {
                if (settings.allowedChannels.contains(item)) {
                    valueList.add(allValues[index])
                    entryList.add(allEntries[index])
                }
            }
        }

        // Work around as empty entryList would lead to crash. TODO: Deny setting this?
        primaryChannelPreference?.isEnabled = true
        val optionNone = allValues.last()
        if (valueList.isEmpty()) {
            // No valid channel available so no choice is possible.
            primaryChannelPreference?.isEnabled = false
            valueList.add(optionNone)
            entryList.add(allEntries.last())
        }
        primaryChannelPreference?.entryValues = valueList.toTypedArray()
        primaryChannelPreference?.entries = entryList.toTypedArray()

        val value = getResources().getStringArray(R.array.channel_values)[settings.primaryChannel.channel]
        // When current mainchannel is not a valid channel anymore remove it from displayed value.
        if (!settings.allowedChannels.contains(settings.primaryChannel)) {
            primaryChannelPreference?.value = null
        }
        primaryChannelPreference?.setDefaultValue(optionNone)
        primaryChannelPreference?.value = value
    }

    private fun refreshOnResume() {
        val removePinPreference = settingsFragment.findPreference<Preference>("remove_pin")
        removePinPreference?.isEnabled = LockManager.getInstance().getAppLock().isPasscodeSet()
    }

    private fun initAdvancedChannelSettings () {
        val settings = Storage.readSettings()

        val autoConfirmPref = settingsFragment.findPreference<CheckBoxPreference>("auto_confirmation")
        autoConfirmPref?.isChecked = settings.useAutomaticConfirmation
        autoConfirmPref?.setOnPreferenceChangeListener { preference: Preference, any: Any ->
            Storage.updateSettings {
                it.useAutomaticConfirmation = !autoConfirmPref.isChecked
                autoConfirmPref.isChecked = !autoConfirmPref.isChecked
            }
            true
        }

        val useBrandingPref = settingsFragment.findPreference<CheckBoxPreference>("useBrandingPref")
        useBrandingPref?.isChecked = settings.useWatermark
        useBrandingPref?.setOnPreferenceChangeListener { preference: Preference, any: Any ->
            Storage.updateSettings {
                it.useWatermark = !useBrandingPref.isChecked
                useBrandingPref.isChecked = !useBrandingPref.isChecked
            }
            true
        }

        val storeBluetoothPref = settingsFragment.findPreference<CheckBoxPreference>("storeBluetooth")
        storeBluetoothPref?.isChecked = settings.storeLastBluetoothMac
        storeBluetoothPref?.summary = if (settings.storedBluetoothMac == null) "" else "Device ${settings.storedBluetoothMac} stored"
        storeBluetoothPref?.setOnPreferenceChangeListener { preference: Preference, any: Any ->
            Storage.updateSettings {
                it.storeLastBluetoothMac = !storeBluetoothPref.isChecked
                storeBluetoothPref.isChecked = !storeBluetoothPref.isChecked
                if (!it.storeLastBluetoothMac) {
                    it.storedBluetoothMac = null
                }
            }
            true
        }


        val sdCardPref = settingsFragment.findPreference<ListPreference>("folder")
        sdCardPref?.entryValues = channelSupportChecker.getSDDrives().toTypedArray()
        sdCardPref?.entries = channelSupportChecker.getSDDrives().toTypedArray()
        sdCardPref?.value = settings.choosenSdCard
        sdCardPref?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            Storage.updateSettings {
                it.choosenSdCard = newValue as String
            }
            true
        }


        val videoSpeed = settingsFragment.findPreference<ListPreference>("speed")
        videoSpeed?.entryValues = QrVideoSpeed.SPEED_LIST.map { it.getName(this) }.toTypedArray()
        videoSpeed?.entries = videoSpeed?.entryValues
        videoSpeed?.value = settings.videoSpeed?.getName(this)
        videoSpeed?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            Storage.updateSettings {
                val test = videoSpeed?.entries!!.indexOf(newValue)
                it.videoSpeed = QrVideoSpeed.getBySpeed(test)
            }
            true
        }

        val soundSpeed = settingsFragment.findPreference<ListPreference>("supersonic")
        soundSpeed?.entryValues = SoundSpectrumAndSpeed.SPEED_LIST.map { it.getName(this) }.toTypedArray()
        soundSpeed?.entries = soundSpeed?.entryValues
        soundSpeed?.value = settings.soundSpectrumAndSpeed.getName(this)
        soundSpeed?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            Storage.updateSettings {
                val test = soundSpeed?.entries!!.indexOf(newValue)
                it.soundSpectrumAndSpeed = SoundSpectrumAndSpeed.getBySpeed(test)
            }
            true
        }
    }


    override fun onResume() {
        super.onResume()
        refreshOnResume()
    }

    override fun onPause() {
        super.onPause()
        try {
            // TODO: do we really need this? should persist on every change
            // TODO: we need to store settings within this activity globally and persist here..
            // storeObject(Storage.settings)
        } catch (e: Exception) {
            // TODO: Data got cleared so no chance to save, handle it more Properly!
        }
    }

}