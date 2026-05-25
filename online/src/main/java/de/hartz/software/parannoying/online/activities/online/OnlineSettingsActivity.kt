package de.hartz.software.parannoying.online.activities.online

import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import de.hartz.software.parannoying.core.activities.SettingsActivity
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.extensions.launchActivity
import de.hartz.software.parannoying.core.helper.io.IOHelper.startDisabledService
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.getDownloadApkIcon
import de.hartz.software.parannoying.core.helper.ui.getFeedbackIcon
import de.hartz.software.parannoying.core.model.exceptions.FeedbackExceptionWrapper
import de.hartz.software.parannoying.online.R
import de.hartz.software.parannoying.online.adapters.FirebaseAdapter
import de.hartz.software.parannoying.online.helper.network.CustomDownloadListener
import de.hartz.software.parannoying.online.helper.network.NotificationReceiver
import de.hartz.software.parannoying.online.interfaces.OnlineApplication
import de.hartz.software.parannoying.online.model.OnlineStorage
import org.acra.ACRA

class OnlineSettingsActivity : SettingsActivity() {
    override val preferencesResource: Int
        get() = R.xml.online_preferences

    private val onlineStorage get() = Storage as OnlineStorage

    override fun init() {
        (app as OnlineApplication).onlineComponents.inject(this)

        val settings = onlineStorage.readSettings()
        val useGoogleApiPreference = settingsFragment.findPreference<CheckBoxPreference>("google")
        useGoogleApiPreference?.isChecked = onlineStorage.useGoogleApi
        useGoogleApiPreference?.isEnabled = FirebaseAdapter(onlineStorage).isGooglePlayServicesAvailable(this)
        useGoogleApiPreference?.setOnPreferenceClickListener {
            onlineStorage.setUseGoogleApi(!onlineStorage.useGoogleApi)
            // Sign in occours in MessageOverview.
            if (!onlineStorage.useGoogleApi) {
                FirebaseAdapter(onlineStorage).signOut()
            }
            startDisabledService(NotificationReceiver::class.java, this, onlineStorage.useGoogleApi)
            true
        }


        val serverConfig = settingsFragment.findPreference<Preference>("server_config")
        serverConfig?.setOnPreferenceClickListener {
            launchActivity<ServerConfigActivity> {  }
            true
        }

        val reportErrorsOnline = settingsFragment.findPreference<CheckBoxPreference>("report_online")
        reportErrorsOnline?.isChecked = settings.reportErrorOnline
        reportErrorsOnline?.setOnPreferenceClickListener {
            settings.reportErrorOnline = !settings.reportErrorOnline
            onlineStorage.persistSettings(settings)
            true
        }

        val useMessageLog = settingsFragment.findPreference<CheckBoxPreference>("log")
        useMessageLog?.isChecked = settings.logEncryptedMessages
        useMessageLog?.setOnPreferenceClickListener {
            settings.logEncryptedMessages = !settings.logEncryptedMessages
            // Sign in occours in MessageOverview.
            if (!settings.logEncryptedMessages) {
                onlineStorage.deleteAllLoggedEncryptedMessages()
            }
            onlineStorage.persistSettings(settings)
            true
        }
    }

    override fun initGeneral() {

        val apkPreference = settingsFragment.findPreference<Preference>("apk")!!
        apkPreference?.setOnPreferenceClickListener {
            val downloadUrl = "https://github.com/Hatzen/ParAnnoying-Releases/releases/download/latest-master/ParAnnoying.apk"
            CustomDownloadListener(this).startDownload(downloadUrl)
            true
        }
        apkPreference.icon = this.getDownloadApkIcon(IconHelper.SMALL_ICON_ACCENT)

        val feedbackPreference = settingsFragment.findPreference<Preference>("feedback")!!
        feedbackPreference?.setOnPreferenceClickListener {

            val title = "Send app feedback to developer server (NOT ENCRYPTED)"
            val freeText = true
            val callback = object: DialogHelper.InputDialogCallback() {
                override fun onFinish(text: String) {
                    val artificialException = FeedbackExceptionWrapper(text)
                    ACRA.errorReporter.handleSilentException(artificialException)
                }
            }
            DialogHelper.showInputDialog(this, title, freeText, callback)
            true
        }
        feedbackPreference.icon = this.getFeedbackIcon(IconHelper.SMALL_ICON_ACCENT)
    }

    override fun disableUnsupported() {
    }

}