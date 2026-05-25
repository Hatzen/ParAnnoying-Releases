package de.hartz.software.parannoying.offline.activities.offline.settings

import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import de.hartz.software.parannoying.core.activities.SettingsActivity
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.extensions.launchActivity
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.getDownloadApkIcon
import de.hartz.software.parannoying.core.helper.ui.getExportIcon
import de.hartz.software.parannoying.core.helper.ui.getFeedbackIcon
import de.hartz.software.parannoying.core.helper.ui.getSyncMessagesIcon
import de.hartz.software.parannoying.core.model.exceptions.FeedbackExceptionWrapper
import de.hartz.software.parannoying.core.model.persistence.realm.RealmHelper
import de.hartz.software.parannoying.offline.R
import de.hartz.software.parannoying.offline.interfaces.OfflineApplication
import de.hartz.software.parannoying.offline.model.OfflineStorage
import de.hartz.software.parannoying.offline.model.domain.settings.MessageSecurity
import javax.inject.Inject

class OfflineSettingsActivity : SettingsActivity() {

    @Inject
    lateinit var securityInterfaceHolder: SecurityInterfaceHolder
    @Inject
    lateinit var realmHelper: RealmHelper

    val offlineStorage get() = Storage as OfflineStorage

    override val preferencesResource: Int
        get() = R.xml.offline_preferences

    override fun init() {
        (app as OfflineApplication).offlineComponents.inject(this)

        val settings = offlineStorage.readSettings()
        val syncAllMessages = settingsFragment.findPreference<CheckBoxPreference>("sync_all")!!
        syncAllMessages.isChecked = settings.syncAllMessages
        syncAllMessages.setOnPreferenceClickListener {
            settings.syncAllMessages = !settings.syncAllMessages
            offlineStorage.persistSettings(settings)
            true
        }
        syncAllMessages.icon = this.getSyncMessagesIcon(IconHelper.SMALL_ICON_ACCENT)

        val messageSecurityPreference = settingsFragment.findPreference<ListPreference>("messages")
        val value = getResources().getStringArray(R.array.security_options)[settings.messageSecurity.security]
        messageSecurityPreference?.setDefaultValue(value)
        messageSecurityPreference?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            val index = resources.getStringArray(R.array.security_values).indexOf(newValue)
            settings.messageSecurity = MessageSecurity.getBySecurity(index)
            offlineStorage.persistSettings(settings)
            true
        }

        val exportData = settingsFragment.findPreference<Preference>("export")!!
        exportData.setOnPreferenceClickListener {
            launchActivity<ExportActivity>()
            true
        }
        exportData.icon = this.getExportIcon(IconHelper.SMALL_ICON_ACCENT)

        val resetOnboarding = settingsFragment.findPreference<Preference>("resetOnboarding")
        resetOnboarding?.setOnPreferenceClickListener {
            offlineStorage.deleteAllOnboardingSteps()
            true
        }

    }

    override fun initGeneral() {
        val apkPreference = settingsFragment.findPreference<Preference>("apk")!!
        apkPreference.setOnPreferenceClickListener {
            // TODO: When FileSharing activity is implemented use it to receive apk and install it directly.
            DialogHelper.showAlert(this, "Please use your online device to download the new version and share it via bluetooth.")
            true
        }
        apkPreference.icon = this.getDownloadApkIcon(IconHelper.SMALL_ICON_ACCENT)

        val feedbackPreference = settingsFragment.findPreference<Preference>("feedback")!!
        feedbackPreference.setOnPreferenceClickListener {

            val title = "Send app feedback to developer server (NOT ENCRYPTED)"
            val freeText = true
            val callback = object: DialogHelper.InputDialogCallback() {
                override fun onFinish(text: String) {
                    val artificialException = FeedbackExceptionWrapper(text)
                    throw artificialException
                }
            }
            DialogHelper.showInputDialog(this, title, freeText, callback)
            true
        }
        feedbackPreference.icon = this.getFeedbackIcon(IconHelper.SMALL_ICON_ACCENT)
    }

    override fun disableUnsupported() {
    }

    override fun onResume() {
        super.onResume()
        hitmarker = 0
    }
}