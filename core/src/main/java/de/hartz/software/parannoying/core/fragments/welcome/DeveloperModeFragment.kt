package de.hartz.software.parannoying.core.fragments.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.fragments.welcome.generic.WelcomeCheckBoxFragment

class DeveloperModeFragment : WelcomeCheckBoxFragment() {
    private var developerModeChecked = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mainContent =  super.onCreateView(inflater, container, savedInstanceState) as ViewGroup?
        val devCheckbox = mainContent?.findViewWithTag<CheckBox>("dev")
        devCheckbox?.setOnClickListener {
            val checkbox = (it as CheckBox)
            Storage.updateSettings {
                developerModeChecked = false
                if (checkbox.isChecked) {
                    developerModeChecked = true
                }
                it.hiddenSettings.developerMode = developerModeChecked
            }

        }
        devCheckbox?.isChecked = true
        return mainContent
    }
}