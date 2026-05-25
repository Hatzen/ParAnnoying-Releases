package de.hartz.software.parannoying.online.helper.onboarding

import android.app.Activity
import android.util.Log
import android.view.View
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.helper.onboarding.Introducer
import de.hartz.software.parannoying.core.model.domain.OnboardingSteps
import de.hartz.software.parannoying.online.R
import de.hartz.software.parannoying.online.activities.online.OnlineMainActivity
import de.hartz.software.parannoying.online.model.OnlineStorage
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal


class OnlineIntroducer(activity: Activity): Introducer(activity) {
    val onlineStorage = activity.Storage as OnlineStorage

    override fun init(showForActivity: SpecificContentForActivity?) {
        when (activity) {
            is OnlineMainActivity -> startMessageOverview()
            is de.hartz.software.parannoying.air.gap.activities.SendActivity -> {
                when (showForActivity) {
                    SpecificContentForActivity.ScanNotification -> startBarcodeActivityForScanningTheOnlineNotificationId()
                    else -> Log.e(javaClass.simpleName, "Should not be reached..", RuntimeException("test")) // throw RuntimeException("Not Supported content.")
                }
            }
        }
    }

    private fun startBarcodeActivityForScanningTheOnlineNotificationId() {
        if (onlineStorage.readOnboardingSteps().contains(OnboardingSteps.BARCODE_NOTIFICATIONID)) {
            // Message follows only after firebase id.
            startMessageOverview()
            return
        }

        val basicBuilder = MaterialTapTargetPrompt.Builder(activity)
                .setBackgroundColour(activity.getResources().getColor(de.hartz.software.parannoying.core.R.color.colorAccent))
                .setCaptureTouchEventOnFocal(true)
        //.setPrimaryTextColour()
        //.setPrimaryTextColour()

        currentMethod = this::startBarcodeActivityForScanningTheOnlineNotificationId
        when (currentStep) {
            0 ->
            {
                val mainViewHolder = activity.findViewById<View>(R.id.fab_action_send)
                basicBuilder
                        .setTarget(mainViewHolder)
                        .setPrimaryText("Scan your notificationId")
                        .setSecondaryText("This is your online id to identify the device where all the messages are sent to by google api. It will connect both devices to be more secure.")
                        .setPromptStateChangeListener(listener)
                        .show()
            }
            1 -> { // Message floating button
                /*
                TODO: remove or start initially with yes no?
                basicBuilder
                        .setTarget(R.id.fab_yes)
                        .setPrimaryText("Id scanned")
                        .setSecondaryText("Tap on this button when you have successfully scanned your id.")
                        .setPromptStateChangeListener(listener)
                        .show()*/

                onlineStorage.persistOnboardingSteps(OnboardingSteps.BARCODE_NOTIFICATIONID)
            }
            else -> {
                currentStep = 0
                currentMethod = {}
            }
        }
    }

    // TODO: Guide to wait for scanning first message. Share or sent, BOTH!
    private fun startMessageOverview() {
        if (onlineStorage.readOnboardingSteps().contains(OnboardingSteps.CHAT_OVERVIEW)) {
            return
        }
        val basicBuilder = MaterialTapTargetPrompt.Builder(activity)
                .setBackgroundColour(activity.getResources().getColor(de.hartz.software.parannoying.core.R.color.colorAccent))
                .setCaptureTouchEventOnFocal(true)
        //.setPrimaryTextColour()
        //.setPrimaryTextColour()

        currentMethod = this::startMessageOverview
        when (currentStep) {
            0 ->
                basicBuilder
                    .setPrimaryText("Sync messages")
                    .setSecondaryText("Select this to send all internet messages to offline device and vice versa.")
                    .setFocalColour(activity.getResources().getColor(de.hartz.software.parannoying.core.R.color.transparent))
                    .setPromptStateChangeListener(listener)
                    .setTarget(activity.findViewById<RecyclerView>(R.id.actions).get(0))
                    .setCaptureTouchEventOnFocal(false)
                    .setPromptFocal(RectanglePromptFocal())
                    .show()
            1 -> // Message floating button
                basicBuilder
                        .setTarget(R.id.outbox)
                        .setPrimaryText("Message Outbox")
                        .setSecondaryText("Tap this button to get and see message from your offline device and share it via any application to send it to the target online device.")
                        .setFocalColour(activity.getResources().getColor(de.hartz.software.parannoying.core.R.color.colorPrimaryDark))
                        .setBackgroundColour(activity.getResources().getColor(de.hartz.software.parannoying.core.R.color.colorAccent))
                        .setCaptureTouchEventOnFocal(true)
                        .setPromptStateChangeListener(listener)
                        .show()
            2 -> {
                basicBuilder
                        .setTarget(R.id.settings)
                        .setFocalColour(activity.getResources().getColor(de.hartz.software.parannoying.core.R.color.colorPrimaryDark))
                        .setBackgroundColour(activity.getResources().getColor(de.hartz.software.parannoying.core.R.color.colorAccent))
                        .setCaptureTouchEventOnFocal(true)
                        .setPrimaryText("Settings")
                        .setSecondaryText("When you want to connect this online device to an offline device, see message log, report errors or change some settings click here.")
                        .setPromptStateChangeListener(listener)
                        .show()
                onlineStorage.persistOnboardingSteps(OnboardingSteps.CHAT_OVERVIEW)
            }
            else -> {
                currentStep = 0
                currentMethod = {}
            }

        }
    }
}