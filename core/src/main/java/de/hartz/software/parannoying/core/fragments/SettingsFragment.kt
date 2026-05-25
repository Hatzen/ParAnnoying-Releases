package de.hartz.software.parannoying.core.fragments

import android.os.Bundle
import androidx.annotation.XmlRes
import androidx.preference.PreferenceFragmentCompat

// Requires empty constructor for recreation
class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        private const val ARG_PREF_RES_ID = "pref_res_id"

        fun newInstance(@XmlRes preferencesResId: Int): SettingsFragment {
            return SettingsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PREF_RES_ID, preferencesResId)
                }
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val prefResId = arguments?.getInt(ARG_PREF_RES_ID)
            ?: throw IllegalArgumentException("Preference resource ID must be provided")
        setPreferencesFromResource(prefResId, rootKey)
    }
}