package de.hartz.software.parannoying.core.fragments.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.fragments.welcome.generic.WelcomeRadioButtonFragment
import de.hartz.software.parannoying.core.model.domain.settings.Channels

class MainChannelFragment : WelcomeRadioButtonFragment() {

    var validOptions: Set<Channels> = mutableSetOf()
    var selectedChannel: Channels? = null

    fun channelsVisibilityHaveToChange(checked: Set<Channels>) {
        validOptions = checked

        Channels.CHANNEL_LIST.forEach {channel ->
            val isChecked = checked.contains(channel)
            val name = channel.getName(requireContext())

            val radioButton = requireView().findViewWithTag<RadioButton>(name)
            radioButton.isChecked = selectedChannel == channel
            if (isChecked) {
                radioButton.visibility = View.VISIBLE
            } else {
                radioButton.visibility = View.GONE
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mainContent =  super.onCreateView(inflater, container, savedInstanceState) as ViewGroup
        Channels.CHANNEL_LIST.forEach {channel ->
            val radioButton = mainContent.findViewWithTag<RadioButton>(channel.getName(requireContext()))
            radioButton.isChecked = selectedChannel == channel
            radioButton.setOnClickListener {
                if (radioButton.isChecked) {
                    Storage.updateSettings {
                        it.primaryChannel = channel
                        selectedChannel = channel
                    }
                }
            }
        }

        return mainContent
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        channelsVisibilityHaveToChange(validOptions)
    }

    override fun onResume() {
        super.onResume()
        channelsVisibilityHaveToChange(validOptions)
        checkAndSkipFragmentIfPossible()
    }

    private fun checkAndSkipFragmentIfPossible() {
        if (validOptions.size != 1) {
            return
        }
        val item = validOptions.first()
        val name = item.getName(requireContext())
        val button = requireView().findViewWithTag<RadioButton>(name)
        button.isChecked = true
        Storage.updateSettings {
            it.primaryChannel = item
        }
        /*
        // TODO: Skipping fragment doesnt work soo well..
        val activity = requireActivity()
        Handler().postDelayed(
            {
                activity.runOnUiThread {
                    activity.findViewById<AppCompatImageButton>(com.github.appintro.R.id.next)
                            .performClick()
                }
            },
            1000
        )*/
    }

    override fun canMoveFurther() : Boolean {
        val context = requireContext()
        return Channels.CHANNEL_LIST.any {
            view?.findViewWithTag<RadioButton>(it.getName(context))?.isChecked == true
        }
    }

    override fun cantMoveFurtherErrorMessage(): String {
        return "Select at least one channel"
    }
}