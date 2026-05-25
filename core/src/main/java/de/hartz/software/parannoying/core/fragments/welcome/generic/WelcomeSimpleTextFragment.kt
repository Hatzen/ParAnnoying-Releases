package de.hartz.software.parannoying.core.fragments.welcome.generic

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.hartz.software.parannoying.core.R
import de.hartz.software.parannoying.core.fragments.welcome.WelcomeBaseFragment


open class WelcomeSimpleTextFragment : WelcomeBaseFragment() {

    companion object {
        val KEY_COLOR = "KEY_COLOR"
        val KEY_HEADER = "KEY_HEADER"
        val KEY_TEXT = "KEY_TEXT"
    }

    private var color: Int = 0
    private lateinit var text: String
    private lateinit var heading: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        heading = arguments?.getString(KEY_HEADER) ?: ""
        text = arguments?.getString(KEY_TEXT) ?: ""
        color = arguments?.getInt(KEY_COLOR) ?: R.color.colorPrimaryDark
        if (savedInstanceState != null) {
            //now do something with savedInstanceState
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(de.hartz.software.parannoying.core.R.layout.welcome_page_simple_text, container, false)
        setText(view.findViewById<TextView>(de.hartz.software.parannoying.core.R.id.welcomeHeading), heading)
        setText(view.findViewById<TextView>(de.hartz.software.parannoying.core.R.id.welcomeText), text)
        view.findViewById<TextView>(de.hartz.software.parannoying.core.R.id.welcomeText).setMovementMethod(LinkMovementMethod.getInstance());
        return view
    }

    override fun backgroundColor(): Int {
        if (color == 0) {
            return super.backgroundColor()
        }
        return color
    }

}