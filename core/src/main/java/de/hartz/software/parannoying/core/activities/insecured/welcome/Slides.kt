package de.hartz.software.parannoying.core.activities.insecured.welcome

import android.os.Bundle
import androidx.fragment.app.Fragment
import de.hartz.software.parannoying.core.R
import de.hartz.software.parannoying.core.fragments.welcome.ChooseChannelFragment
import de.hartz.software.parannoying.core.fragments.welcome.DeveloperModeFragment
import de.hartz.software.parannoying.core.fragments.welcome.MainChannelFragment
import de.hartz.software.parannoying.core.fragments.welcome.WelcomeAboutFragment
import de.hartz.software.parannoying.core.fragments.welcome.WelcomeChannelsExplainedFragment
import de.hartz.software.parannoying.core.fragments.welcome.WelcomePermissionsFragment
import de.hartz.software.parannoying.core.fragments.welcome.generic.WelcomeCheckBoxFragment
import de.hartz.software.parannoying.core.fragments.welcome.generic.WelcomeRadioButtonFragment
import de.hartz.software.parannoying.core.fragments.welcome.generic.WelcomeSimpleTextFragment
import de.hartz.software.parannoying.core.interfaces.AbstractApp
import de.hartz.software.parannoying.core.interfaces.di.StorageInterface
import de.hartz.software.parannoying.core.model.domain.settings.Channels

class Slides(val welcomeActivity: WelcomeActivity, val app: AbstractApp, val Storage: StorageInterface<*, *>) {


    fun addSlide(fragment: Fragment) {
        welcomeActivity.addASlide(fragment)
    }

    fun initSlides() {
        var fragmentCount = 1

        val first = WelcomeSimpleTextFragment()
        var bundle = Bundle()
        bundle.putInt(WelcomeSimpleTextFragment.KEY_COLOR, de.hartz.software.parannoying.core.R.color.colorRed)
        bundle.putString(WelcomeSimpleTextFragment.KEY_HEADER, "Disclaimer")
        bundle.putString(WelcomeSimpleTextFragment.KEY_TEXT,
                "This apps purpose is <b> only security not usability</b> <br> So to fully understand the concept of this messenger please take at least 5 to 10 Minutes.")
        first.arguments = bundle
        addSlide(first)
        fragmentCount++

        Storage.readSettings().hiddenSettings.developerMode = app.isDebugMode()
        if (app.isDebugMode()) {
            val devModeFragment = DeveloperModeFragment()
            bundle = Bundle()
            bundle.putInt(WelcomeSimpleTextFragment.KEY_COLOR, R.color.colorBlack)
            bundle.putString(WelcomeSimpleTextFragment.KEY_HEADER, "<b>This is not a released Version</b> do you want to keep debugging the app?")
            bundle.putStringArray(WelcomeCheckBoxFragment.KEY_CHECKBOXTEXTS, arrayOf("Developer Mode active"))
            bundle.putStringArray(WelcomeCheckBoxFragment.KEY_CHECKBOXTAGS, arrayOf("dev"))
            devModeFragment.arguments = bundle
            addSlide(devModeFragment)
            fragmentCount++
        }

        val firstPointTwo = WelcomeAboutFragment()
        bundle = Bundle()
        bundle.putInt(WelcomeSimpleTextFragment.KEY_COLOR, R.color.colorAccent)
        bundle.putString(WelcomeSimpleTextFragment.KEY_HEADER, "Get informed before starting")
        bundle.putString(WelcomeSimpleTextFragment.KEY_TEXT,
                "For all necessary informations in detail please have a look into the about section")
        firstPointTwo.arguments = bundle
        addSlide(firstPointTwo)
        fragmentCount++

        val second = WelcomeSimpleTextFragment()
        bundle = Bundle()
        bundle.putInt(WelcomeSimpleTextFragment.KEY_COLOR, de.hartz.software.parannoying.core.R.color.colorYellow)
        bundle.putString(WelcomeSimpleTextFragment.KEY_HEADER, "Prequisites")
        bundle.putString(WelcomeSimpleTextFragment.KEY_TEXT,
                "You need <b>two</b> android devices (Version 4.4 and above) with Camera, NFC or Bluetooth. Where <b>one must not have internet</b> connection forever you want to this messenger.")
        second.arguments = bundle
        addSlide(second)
        fragmentCount++

        val third = WelcomeSimpleTextFragment()
        bundle = Bundle()
        bundle.putInt(WelcomeSimpleTextFragment.KEY_COLOR, de.hartz.software.parannoying.core.R.color.colorAccent)
        bundle.putString(WelcomeSimpleTextFragment.KEY_HEADER, "Offline Device")
        bundle.putString(WelcomeSimpleTextFragment.KEY_TEXT,
                "This device is responsible to encrypt and decrypt messages. You will write your messages therefore you have to cut off the internet connection.")
        third.arguments = bundle
        addSlide(third)
        fragmentCount++

        val fourth = WelcomeSimpleTextFragment()
        bundle = Bundle()
        bundle.putInt(WelcomeSimpleTextFragment.KEY_COLOR, de.hartz.software.parannoying.core.R.color.colorPrimary)
        bundle.putString(WelcomeSimpleTextFragment.KEY_HEADER, "NOTICE")
        bundle.putString(WelcomeSimpleTextFragment.KEY_TEXT,
                "When a offline devices is configured and get internet connection at anytime all app data will be wiped automatically")
        fourth.arguments = bundle
        addSlide(fourth)
        fragmentCount++

        val fifth = WelcomeSimpleTextFragment()
        bundle = Bundle()
        bundle.putInt(WelcomeSimpleTextFragment.KEY_COLOR, de.hartz.software.parannoying.core.R.color.colorPrimaryDark)
        bundle.putString(WelcomeSimpleTextFragment.KEY_HEADER, "Online Device")
        bundle.putString(WelcomeSimpleTextFragment.KEY_TEXT,
                "This device is responsible to deliver and receive all encrypted messages via the internet by a choosen channel. You can send the message via integrated google api or share it via any other messenger app.")
        fifth.arguments = bundle
        addSlide(fifth)
        fragmentCount++

        val fifthPointOne = WelcomeSimpleTextFragment()
        bundle = Bundle()
        bundle.putInt(WelcomeSimpleTextFragment.KEY_COLOR, de.hartz.software.parannoying.core.R.color.colorAccent)
        bundle.putString(WelcomeSimpleTextFragment.KEY_HEADER, "Notice")
        bundle.putString(WelcomeSimpleTextFragment.KEY_TEXT,
                "Before setting up the offline device you need to have an online device settup.")
        fifthPointOne.arguments = bundle
        addSlide(fifthPointOne)
        fragmentCount++

        val fifthPointTwo = WelcomeSimpleTextFragment()
        bundle = Bundle()
        bundle.putInt(WelcomeSimpleTextFragment.KEY_COLOR, R.color.colorPrimary)
        bundle.putString(WelcomeSimpleTextFragment.KEY_HEADER, "Channels")
        bundle.putString(WelcomeSimpleTextFragment.KEY_TEXT,
                "Channels are the way you communicate between the online and offline device. There are multiple ways which may be restricted by your device. In the next step you have to choose them.")
        fifthPointTwo.arguments = bundle
        addSlide(fifthPointTwo)
        fragmentCount++

        val fifthPointThree = WelcomeChannelsExplainedFragment()
        addSlide(fifthPointThree)
        fragmentCount++

        val choosenChannel = ChooseChannelFragment()
        bundle = Bundle()
        bundle.putInt(WelcomeSimpleTextFragment.KEY_COLOR, de.hartz.software.parannoying.core.R.color.colorAccent)
        bundle.putString(WelcomeSimpleTextFragment.KEY_HEADER, "<b>Select</b> all <b>channels</b> you want to use between your both devices")
        // Same as arrays.xml values
        val texts = arrayOf("Camera", "NFC", "Bluetooth", "Text", "Sound", "Video", "SD-Card")
        val tags = arrayOf("camera", "nfc", "bluetooth", "text", "sound", "video", "sd-card")
        bundle.putStringArray(WelcomeCheckBoxFragment.KEY_CHECKBOXTEXTS, texts)
        bundle.putStringArray(WelcomeCheckBoxFragment.KEY_CHECKBOXTAGS, tags)

        bundle.putString(WelcomeSimpleTextFragment.KEY_HEADER,
                "Select all usable channels")
        choosenChannel.arguments = bundle
        addSlide(choosenChannel)
        fragmentCount++

        val mainChannel = MainChannelFragment()
        bundle = Bundle()
        bundle.putInt(WelcomeSimpleTextFragment.KEY_COLOR, de.hartz.software.parannoying.core.R.color.grayAccent)
        bundle.putString(WelcomeSimpleTextFragment.KEY_HEADER, "Select the <b>main channel</b> to use")

        val channel = Channels.CHANNEL_LIST.map { it.getName(welcomeActivity) }.toTypedArray()
        bundle.putStringArray(WelcomeRadioButtonFragment.KEY_RADIOBUTTONS, channel)
        bundle.putStringArray(WelcomeRadioButtonFragment.KEY_RADIOBUTTON_TAGS, channel)

        mainChannel.arguments = bundle
        addSlide(mainChannel)
        fragmentCount++

        choosenChannel.listener = mainChannel

        val sixth = WelcomeSimpleTextFragment()
        bundle = Bundle()
        bundle.putInt(WelcomeSimpleTextFragment.KEY_COLOR, de.hartz.software.parannoying.core.R.color.colorPrimary)
        bundle.putString(WelcomeSimpleTextFragment.KEY_TEXT,
                "Now you can pick the role and choose the channel both devices will communicate with")
        sixth.arguments = bundle
        addSlide(sixth)
        fragmentCount++

        val seventh = WelcomeRadioButtonFragment()
        bundle = Bundle()
        bundle.putInt(WelcomeSimpleTextFragment.KEY_COLOR, de.hartz.software.parannoying.core.R.color.grayAccent)
        bundle.putString(WelcomeSimpleTextFragment.KEY_HEADER, "Select the device role")
        bundle.putStringArray(WelcomeRadioButtonFragment.KEY_RADIOBUTTONS, arrayOf("Online", "Offline"))
        bundle.putStringArray(WelcomeRadioButtonFragment.KEY_RADIOBUTTON_TAGS, arrayOf("online", "offline"))
        seventh.arguments = bundle
        addSlide(seventh)
        welcomeActivity.deviceRoleFragment = seventh
        fragmentCount++

        val eight = WelcomePermissionsFragment()
        bundle = Bundle()
        bundle.putInt(WelcomeSimpleTextFragment.KEY_COLOR, R.color.grayAccent)
        bundle.putString(WelcomeSimpleTextFragment.KEY_HEADER, "Please grant now all mandatory permissions (green buttons)")
        eight.arguments = bundle
        addSlide(eight)

        fun setAskForPermissions() {
            val permissions = eight.getRequiredPermissions().toTypedArray()
            if (permissions.size > 0) {
                // Ask for required permission.
                welcomeActivity.askPermissions(fragmentCount, permissions)
            }

            // TODO: We cannot ask for required and optional permissions at the same time..
            //askForPermissions(
            // permissions = eight.getOptionalPermissions().toTypedArray(),
            //    slideNumber = fragmentCount,
            //    required = false)
        }
        eight.callback = ::setAskForPermissions
    }

}