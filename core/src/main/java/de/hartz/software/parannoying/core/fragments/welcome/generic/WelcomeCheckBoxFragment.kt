package de.hartz.software.parannoying.core.fragments.welcome.generic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import de.hartz.software.parannoying.core.R
import de.hartz.software.parannoying.core.fragments.welcome.WelcomeBaseFragment


open class WelcomeCheckBoxFragment : WelcomeBaseFragment() {

    companion object {
        val KEY_HEADER = "KEY_HEADER"
        val KEY_CHECKBOXTEXTS = "KEY_CHECKBOXTEXTS"
        val KEY_CHECKBOXTAGS = "KEY_CHECKBOXTAGS"
    }

    private var color: Int = 0
    private lateinit var heading: String
    private lateinit var checkBoxTexts: Array<String>
    private lateinit var checkBoxTags: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        heading = arguments?.getString(WelcomeRadioButtonFragment.KEY_HEADER) ?: ""
        color = arguments?.getInt(WelcomeSimpleTextFragment.KEY_COLOR) ?: 0
        checkBoxTexts = arguments?.getStringArray(KEY_CHECKBOXTEXTS) ?: arrayOf()
        checkBoxTags = arguments?.getStringArray(KEY_CHECKBOXTAGS) ?: arrayOf()
        if (savedInstanceState != null) {
            //now do something with savedInstanceState
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.welcome_page_check_box, container, false)
        setText(view.findViewById<TextView>(de.hartz.software.parannoying.core.R.id.welcomeHeading), heading)
        val checkBoxGroup = view.findViewById<LinearLayout>(de.hartz.software.parannoying.core.R.id.check_group)
        for (i in 0 until checkBoxTexts.size) {
            addCheckbox(inflater, checkBoxGroup, checkBoxTexts[i], checkBoxTags[i])
        }
        return view
    }

    private fun addCheckbox(inflater: LayoutInflater, parent:ViewGroup, text: String, tag:String) {
        val view = inflater.inflate(de.hartz.software.parannoying.core.R.layout.checkbox_white, null) as CheckBox
        view.setText(text)
        view.tag = tag
        parent.addView(view)
    }

    override fun backgroundColor(): Int {
        if (color == 0) {
            return super.backgroundColor()
        }
        return color
    }

}