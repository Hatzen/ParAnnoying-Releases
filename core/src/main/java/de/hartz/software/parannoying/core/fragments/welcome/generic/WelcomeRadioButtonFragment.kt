package de.hartz.software.parannoying.core.fragments.welcome.generic

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import de.hartz.software.parannoying.core.R
import de.hartz.software.parannoying.core.fragments.welcome.WelcomeBaseFragment


open class WelcomeRadioButtonFragment : WelcomeBaseFragment() {

    companion object {
        val KEY_HEADER = "KEY_HEADER"
        val KEY_RADIOBUTTONS = "KEY_RADIOBUTTONS"
        val KEY_RADIOBUTTON_TAGS = "KEY_RADIOBUTTON_TAGS"
    }

    private var color: Int = 0
    private lateinit var heading: String
    private lateinit var radioButtonTexts: Array<String>
    private lateinit var radioButtonTags: Array<String>

    private var hasChosen: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        heading = arguments?.getString(KEY_HEADER) ?: ""
        color = arguments?.getInt(WelcomeSimpleTextFragment.KEY_COLOR) ?: 0
        radioButtonTexts = arguments?.getStringArray(KEY_RADIOBUTTONS) ?: arrayOf()
        radioButtonTags = arguments?.getStringArray(KEY_RADIOBUTTON_TAGS) ?: arrayOf()
        if (savedInstanceState != null) {
            //now do something with savedInstanceState
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.welcome_page_radio_button, container, false)
        setText(view.findViewById<TextView>(de.hartz.software.parannoying.core.R.id.welcomeHeading), heading)
        val radioGroup = view.findViewById<RadioGroup>(de.hartz.software.parannoying.core.R.id.radiogroup)
        radioGroup.setOnCheckedChangeListener { radioGroup: RadioGroup, i: Int ->
            hasChosen = true
        }
        for (i in 0 until radioButtonTexts.size) {
            addRadioButton(inflater, radioGroup, radioButtonTexts[i], radioButtonTags[i])
        }
        view.invalidate()
        return view
    }
    
    private fun addRadioButton(inflater: LayoutInflater, parent:ViewGroup, text: String, tag:String) {
        val view = inflater.inflate(de.hartz.software.parannoying.core.R.layout.radiobutton_white, null) as RadioButton
        view.text = text
        view.tag = tag
        view.gravity = Gravity.CENTER
        parent.addView(view)
    }

    override fun canMoveFurther(): Boolean {
        return hasChosen
    }

    override fun cantMoveFurtherErrorMessage(): String {
        return "Select an option before you can go on"
    }

    override fun backgroundColor(): Int {
        if (color == 0) {
            return super.backgroundColor()
        }
        return color
    }

}