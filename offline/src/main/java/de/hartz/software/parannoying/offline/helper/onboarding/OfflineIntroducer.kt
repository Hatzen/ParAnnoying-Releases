package de.hartz.software.parannoying.offline.helper.onboarding

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.helper.onboarding.Introducer
import de.hartz.software.parannoying.core.model.domain.OnboardingSteps
import de.hartz.software.parannoying.core.model.domain.settings.Channels
import de.hartz.software.parannoying.offline.R
import de.hartz.software.parannoying.offline.activities.offline.ChatActivity
import de.hartz.software.parannoying.offline.activities.offline.OfflineMainActivity
import de.hartz.software.parannoying.offline.model.OfflineStorage
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal

class OfflineIntroducer(activity: Activity): Introducer(activity) {

    val offlineStorage = activity.Storage as OfflineStorage

    override fun init(showForActivity: SpecificContentForActivity?) {
        when (activity) {
            is OfflineMainActivity -> startChatOverview()
            is ChatActivity -> startChat()
            is de.hartz.software.parannoying.air.gap.activities.SendActivity -> {
                when (showForActivity) {
                    SpecificContentForActivity.ScanMessage -> startBarcodeActivityForScanningMessage()
                    SpecificContentForActivity.ScanUserId -> startBarcodeActivityForScanningUserId()
                    SpecificContentForActivity.ScanNotification -> {
                        // triggered when restore notificationid is called.
                    }
                    else -> Log.e(javaClass.simpleName, "Should not be reached..", RuntimeException("test")) // throw RuntimeException("Not Supported content.")
                }
            }
        }
    }

    private fun startChatOverview() {
        if (offlineStorage.onboardingSteps.contains(OnboardingSteps.CHAT_OVERVIEW)) {
            return
        }

        val basicBuilder = MaterialTapTargetPrompt.Builder(activity)
                .setFocalColour(activity.getResources().getColor(de.hartz.software.parannoying.core.R.color.colorPrimaryDark))
                .setBackgroundColour(activity.getResources().getColor(de.hartz.software.parannoying.core.R.color.colorAccent))
                .setCaptureTouchEventOnFocal(true)
                //.setPrimaryTextColour()
                //.setPrimaryTextColour()

        currentMethod = this::startChatOverview
        when (currentStep) {
            0 -> // UserId floating button
                basicBuilder
                    .setTarget(R.id.users)
                    .setPrimaryText("Scan UserId")
                    .setSecondaryText("Tap this button to scan a foreign UserId and add this user to the list.")
                    .setPromptStateChangeListener(listener)
                    // Dont know why but its needed otherwise there is a not covered layer at the bottom.
                    // TODO: this doesnt seem to work all the time.
                    .setClipToView(activity.findViewById(R.id.blurlayout))
                    .show()
            1 -> // Message floating button
                basicBuilder
                    .setTarget(R.id.messages)
                    .setPrimaryText("Scan Message")
                    .setSecondaryText("Tap this button to scan a message from your online device to apply and decrypt it to matching user.")
                    .setPromptStateChangeListener(listener)
                    .show()
            2 ->
                basicBuilder
                    .setTarget(R.id.users)
                    .setPrimaryText("Your UserId")
                    .setSecondaryText("Tap this button to display and share your UserId. This contains your keys and Id share it only with people you want to communicate with.")
                    .setPromptStateChangeListener(listener)
                    .show()
            4 -> {
                basicBuilder
                    .setTarget(R.id.settings)
                    .setPrimaryText("Settings")
                    .setSecondaryText("Here are many more options you can customize, take a closer look at it.")
                    .setPromptStateChangeListener(listener)
                    .show()
            }
            3 -> {
                // TODO: why does this crashes from time to time? Probably wrong fragment?
                val view = activity.findViewById<RecyclerView>(R.id.dialogsList)
                if (view == null) {
                    return
                }
                basicBuilder
                    .setPrimaryText("Start a chat")
                    .setSecondaryText("Now select your username and write a message to yourself")
                    .setFocalColour(activity.getResources().getColor(de.hartz.software.parannoying.core.R.color.transparent))
                    .setPromptStateChangeListener(listener)
                    .setTarget(view.get(0))
                    .setCaptureTouchEventOnFocal(false)
                    .setPromptFocal(RectanglePromptFocal())
                    .show()
                offlineStorage.persistOnboardingSteps(OnboardingSteps.CHAT_OVERVIEW)
            }
            else -> {
                currentStep = 0
                currentMethod = {}
            }

        }
    }


    private fun startChat() {
        if (offlineStorage.onboardingSteps.contains(OnboardingSteps.CHAT)) {
            return
        }
        val basicBuilder = MaterialTapTargetPrompt.Builder(activity)
                .setBackgroundColour(activity.getResources().getColor(de.hartz.software.parannoying.core.R.color.colorAccent))
                .setCaptureTouchEventOnFocal(true)
        //.setPrimaryTextColour()
        //.setPrimaryTextColour()

        currentMethod = this::startChat
        /* TODO: change  ids */
        when (currentStep) {
            0 -> // UserId floating button
                basicBuilder
                        .setTarget(R.id.messagesList)
                        .setPrimaryText("Chat overview")
                        .setSecondaryText("All messages from the conversation will be displayed here.")
                        .setPromptStateChangeListener(listener)
                        .show()
            1 -> // Message floating button
                basicBuilder
                        .setTarget(R.id.input)
                        .setPrimaryText("Enter your message")
                        .setSecondaryText("Tap on this area to display the keyboard an enter a message. If you are ready tap back a single time.")
                        .setPromptStateChangeListener(listener)
                        .setPromptFocal(RectanglePromptFocal())
                        .show()
            2 -> {// Message floating button
                basicBuilder
                        .setTarget(R.id.messageSendButton)
                        .setPrimaryText("Send the message")
                        .setSecondaryText("Now tap this button to show the encrypted message and send the message via your choosen main channel.")
                        .setPromptStateChangeListener(listener)
                        .show()
                offlineStorage.persistOnboardingSteps(OnboardingSteps.CHAT)
            }
            else -> {
                currentStep = 0
                currentMethod = {}
            }
        }

         // */
    }

    private fun startBarcodeActivityForScanningMessage() {
        if (offlineStorage.onboardingSteps.contains(OnboardingSteps.BARCODE_MESSAGE)) {
            return
        }
        val basicBuilder = MaterialTapTargetPrompt.Builder(activity)
                .setBackgroundColour(activity.getResources().getColor(de.hartz.software.parannoying.core.R.color.colorAccent))
                .setCaptureTouchEventOnFocal(true)
        //.setPrimaryTextColour()
        //.setPrimaryTextColour()

        currentMethod = this::startBarcodeActivityForScanningMessage
        when (currentStep) {
            0 -> // UserId floating button
                basicBuilder
                        .setTarget(R.id.fab_no)
                        .setPrimaryText("Fallback")
                        .setSecondaryText("Only if you want to change the message tap this button.")
                        .setPromptStateChangeListener(listener)
                        .show()
            1 -> // Channel bar
            {
                val container = activity.findViewById<LinearLayout>(R.id.bar)
                basicBuilder
                        .setTarget(container)
                        .setPrimaryText("Select a channel")
                        .setSecondaryText("Now select the channel you want to send your message over.")
                        .setPromptStateChangeListener(listener)
                        .setPromptFocal(RectanglePromptFocal())
                        .setFocalColour(activity.getResources().getColor(de.hartz.software.parannoying.core.R.color.colorPrimaryDark))
                        //.setFocalColour(activity.resources.getColor(de.hartz.software.parannoying.core.R.color.transparent)) // For some reason the bar is otherwise just a white bar.
                        .show()
            }
            2 -> // status bar
            {
                val container = activity.findViewById<View>(R.id.seperator)
                basicBuilder
                        .setTarget(container)
                        .setPrimaryText("The status bar")
                        .setSecondaryText("On some channels you get feedback here over the connecting status otherwise it will be gray.")
                        .setPromptStateChangeListener(listener)
                        .setPromptFocal(RectanglePromptFocal())
                        .show()
            }
            3 -> // send button
            {
                basicBuilder
                        .setTarget(R.id.fab_action_send)
                        .setPrimaryText("Start sending with channel")
                        .setSecondaryText("Tap on this button to start the sending process which depends on the channel. In case of camera you probably just tap the qrcode to scan it with your other camera.")
                        .setPromptStateChangeListener(listener)
                        .show()
            }
            4 -> { // accept Message floating button
                basicBuilder
                        .setTarget(R.id.fab_yes)
                        .setPrimaryText("Apply Message")
                        .setSecondaryText("Tap on this button to apply your entered message to the chat and return to the chat overview.")
                        .setPromptStateChangeListener(listener)
                        .show()
                offlineStorage.persistOnboardingSteps(OnboardingSteps.BARCODE_MESSAGE)
            }
            else -> {
                currentStep = 0
                currentMethod = {}
            }
        }
    }

    private fun startBarcodeActivityForScanningUserId() {
        if (offlineStorage.onboardingSteps.contains(OnboardingSteps.BARCODE_USERID)) {
            return
        }
        // TODO: Why the hell does this not work.. :: This doesnt work caused by initaliztion of adapter, the view will not be found...
        val basicBuilder = MaterialTapTargetPrompt.Builder(activity)
                .setBackgroundColour(activity.getResources().getColor(de.hartz.software.parannoying.core.R.color.colorAccent))
                .setCaptureTouchEventOnFocal(true)

        currentMethod = this::startBarcodeActivityForScanningUserId
        when (currentStep) {
            1 -> // UserId floating button
            {
                var heading = "Send your UserId"
                // TODO:  TExt is to long, on full hd there is an end at userid via the
                var text = "Now send your UserId via your prefered channel to the other device, all available channels can be seen via swiping left or right. To start sending your UserId via the displayed channel tap this button."
                if (offlineStorage.readSettings().primaryChannel == Channels.CAMERA) {
                    heading = "Scan your UserId"
                    text = "Now scan your UserId with another offline device to let it communicate with you. Tap on the image to hide buttons."
                }

                val mainViewHolder = activity.findViewById<View>(R.id.fab_action_send)
                basicBuilder
                        .setTarget(mainViewHolder)
                        .setPrimaryText(heading)
                        .setSecondaryText(text)
                        .setPromptStateChangeListener(listener)
                        .show()

                offlineStorage.persistOnboardingSteps(OnboardingSteps.BARCODE_USERID)
            }
            0 -> // Message floating button
                basicBuilder
                        .setTarget(R.id.fab_key)
                        .setPrimaryText("Encrypted UserId")
                        .setSecondaryText("To protect the UserId it is encrypted. After scanning the UserId you must enter a key to decrypt, which will be displayed after pressing the key button.")
                        .setPromptStateChangeListener(listener)
                        .show()
            else -> {
                currentStep = 0
                currentMethod = {}
            }
        }
    }

}