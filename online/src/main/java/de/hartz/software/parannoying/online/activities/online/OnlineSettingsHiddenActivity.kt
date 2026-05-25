package de.hartz.software.parannoying.online.activities.online


import android.os.Bundle
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import de.hartz.software.parannoying.air.gap.activities.dummy.DummyAirGapActivity
import de.hartz.software.parannoying.core.activities.BaseActivity
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.extensions.launchActivity
import de.hartz.software.parannoying.online.R
import de.hartz.software.parannoying.online.model.OnlineStorage


// https://developer.android.com/guide/topics/ui/settings
// https://www.androidhive.info/2017/07/android-implementing-preferences-settings-screen/
class OnlineSettingsHiddenActivity : BaseActivity() {

    val DEBUGGING_KEY = "developer_mode"
    val START_AIRGAP_KEY = "airgap"
    private val onlineStorage get() = Storage as OnlineStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        val settingsFragment = SettingsFragment()
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, settingsFragment)
                .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Needed otherwise findPreferences always returns null.
        supportFragmentManager.executePendingTransactions()

        val debuggingPreference = settingsFragment.findPreference<CheckBoxPreference>(DEBUGGING_KEY)
        val settings = onlineStorage.readSettings()
        debuggingPreference?.isChecked = settings.hiddenSettings.developerMode
        debuggingPreference?.setOnPreferenceClickListener {
            settings.hiddenSettings.developerMode = !settings.hiddenSettings.developerMode
            onlineStorage.persistSettings(settings)
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
            setPreferencesFromResource(R.xml.online_hidden_preferences, rootKey)
        }
    }
}