package de.hartz.software.parannoying.offline.activities.offline.settings


import android.os.Bundle
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import de.hartz.software.parannoying.air.gap.activities.dummy.DummyAirGapActivity
import de.hartz.software.parannoying.core.activities.BaseActivity
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.extensions.launchActivity
import de.hartz.software.parannoying.offline.R
import de.hartz.software.parannoying.offline.model.OfflineStorage


// https://developer.android.com/guide/topics/ui/settings
// https://www.androidhive.info/2017/07/android-implementing-preferences-settings-screen/
class OfflineSettingsHiddenActivity : BaseActivity() { // Cannot be BaseOfflineActivity as SettingsActivity has no BlurLayout, could be wrapped in an own view..

    val INTERNET_KEY = "allow_internet"
    val ROOT_KEY = "root"
    val SCREENSHOT_KEY = "screen_recording"
    val START_AIRGAP_KEY = "airgap"
    val DEBUGGING_KEY = "developer_mode"

    val offlineStorage get() = Storage as OfflineStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(de.hartz.software.parannoying.offline.R.layout.settings_activity)

        val settingsFragment = SettingsFragment()
        supportFragmentManager
                .beginTransaction()
                .replace(de.hartz.software.parannoying.offline.R.id.settings, settingsFragment)
                .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Needed otherwise findPreferences always returns null.
        supportFragmentManager.executePendingTransactions()

        val settings = offlineStorage.readSettings()
        val debuggingPreference = settingsFragment.findPreference<CheckBoxPreference>(DEBUGGING_KEY)
        debuggingPreference?.isChecked = settings.hiddenSettings.developerMode
        debuggingPreference?.setOnPreferenceClickListener {
            settings.hiddenSettings.developerMode = !settings.hiddenSettings.developerMode
            offlineStorage.persistSettings(settings)
            true
        }

        val internetPreference = settingsFragment.findPreference<CheckBoxPreference>(INTERNET_KEY)
        internetPreference?.isChecked = settings.hiddenSettings.allowInternet
        internetPreference?.setOnPreferenceClickListener {
            settings.hiddenSettings.allowInternet = !settings.hiddenSettings.allowInternet
            offlineStorage.persistSettings(settings)
            true
        }

        val screenshotPreference = settingsFragment.findPreference<CheckBoxPreference>(SCREENSHOT_KEY)
        screenshotPreference?.isChecked = settings.hiddenSettings.allowScreenshots
        screenshotPreference?.setOnPreferenceClickListener {
            settings.hiddenSettings.allowScreenshots = !settings.hiddenSettings.allowScreenshots
            offlineStorage.persistSettings(settings)
            true
        }

        val airgapPreference = settingsFragment.findPreference<Preference>(START_AIRGAP_KEY)
        airgapPreference?.setOnPreferenceClickListener {
            launchActivity<DummyAirGapActivity> {  }
            true
        }

    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.offline_hidden_preferences, rootKey)
        }
    }
}