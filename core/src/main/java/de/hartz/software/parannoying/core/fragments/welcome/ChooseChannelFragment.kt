package de.hartz.software.parannoying.core.fragments.welcome

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.appcompat.widget.AppCompatCheckBox
import de.hartz.software.parannoying.core.R
import de.hartz.software.parannoying.core.activities.insecured.welcome.WelcomeActivity
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.fragments.welcome.generic.WelcomeCheckBoxFragment
import de.hartz.software.parannoying.core.model.domain.settings.Channels

class ChooseChannelFragment : WelcomeCheckBoxFragment() {
    private var checked = mutableSetOf<Channels>()
    var listener: MainChannelFragment? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mainContent =  super.onCreateView(inflater, container, savedInstanceState) as ViewGroup?

        Channels.CHANNEL_LIST.forEach {channel ->
            val checkbox = mainContent!!.findViewWithTag<CheckBox>(channel.getName(requireContext()))
            checkbox.isChecked = checked.contains(channel)
            checkbox.setOnClickListener {
                Storage.updateSettings {
                    checked.remove(channel)

                    it.allowedChannels.remove(channel)
                    if (checkbox.isChecked) {
                        checked.add(channel)
                        it.allowedChannels.add(channel)
                    }
                    notifyListener()
                }
            }
        }
        disableUnsupportedElements(mainContent, this.requireActivity() as WelcomeActivity)

        checked.forEach {channel ->
            val checkbox = mainContent!!.findViewWithTag<CheckBox>(channel.getName(requireContext()))
            // For some reasons we need the delay.. https://github.com/navasmdc/MaterialDesignLibrary/issues/199
            Handler().postDelayed({ checkbox.isChecked = true }, 300)
        }
        return mainContent
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checked.forEach {channel ->
            val checkbox = requireView().findViewWithTag<CheckBox>(channel.getName(requireContext()))
            checkbox.isChecked = true
        }
    }

    private fun notifyListener() {
        listener?.channelsVisibilityHaveToChange(checked)
    }

    override fun canMoveFurther() : Boolean {
        return checked.size > 0
    }

    override fun cantMoveFurtherErrorMessage(): String {
        return "Select at least one channel"
    }


    fun disableUnsupportedElements(view: ViewGroup?, activity: WelcomeActivity) {
        val accentColor = activity.getResources().getColor(R.color.grayAccent)
        checkButtonAvailability(view, "camera", activity.channelSupportChecker.isCameraSupported() , accentColor)
        checkButtonAvailability(view, "video", activity.channelSupportChecker.isCameraSupported() , accentColor)
        checkButtonAvailability(view, "nfc", activity.channelSupportChecker.isNFCSupported(), accentColor)
        checkButtonAvailability(view, "bluetooth", activity.channelSupportChecker.isBluetoothSupported(), accentColor)
        checkButtonAvailability(view, "sd-card", activity.channelSupportChecker.isSDSupported(), accentColor)
        checkButtonAvailability(view, "sound", activity.channelSupportChecker.isSoundSupported(), accentColor)
    }

    @SuppressLint("RestrictedApi")
    fun checkButtonAvailability(parent: ViewGroup?, name: String, isAvailable: Boolean, color: Int) {
        val compoundButton = parent!!.findViewWithTag<AppCompatCheckBox>(name)!!
        if (isAvailable) {
            compoundButton.isEnabled = true
        } else {
            compoundButton.isEnabled = false
            compoundButton.setTextColor(color)
            val states = arrayOf(intArrayOf(android.R.attr.state_enabled), intArrayOf(-android.R.attr.state_enabled), intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_pressed))

            val colors = intArrayOf(color,color,color,color)
            val myList = ColorStateList(states, colors)
            compoundButton.supportButtonTintList = myList
        }
    }

}