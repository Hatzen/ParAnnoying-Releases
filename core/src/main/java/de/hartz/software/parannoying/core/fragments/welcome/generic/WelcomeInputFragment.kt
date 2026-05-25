package de.hartz.software.parannoying.core.fragments.welcome.generic

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.hartz.software.parannoying.core.R
import de.hartz.software.parannoying.core.fragments.welcome.WelcomeBaseFragment


class WelcomeInputFragment : WelcomeBaseFragment() {

    companion object {
        val KEY_TEXT = "KEY_TEXT"
    }

    private var color: Int = 0
    private lateinit var text: String

    private var nameEntered: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        text = arguments?.getString(WelcomeSimpleTextFragment.KEY_TEXT) ?: ""
        color = arguments?.getInt(WelcomeSimpleTextFragment.KEY_COLOR) ?: R.color.colorPrimaryDark
        if (savedInstanceState != null) {
            //now do something with savedInstanceState
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.welcome_page_username, container, false)
        val textView = view.findViewById<TextView>(de.hartz.software.parannoying.core.R.id.welcomeText)
        setText(textView, text)

        val editText = view.findViewById<TextView>(de.hartz.software.parannoying.core.R.id.username)
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(enteredText: Editable?) {
                nameEntered = false
                if (enteredText != null) {
                    nameEntered = enteredText.isNotEmpty()
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        })
        return view
    }

    override fun cantMoveFurtherErrorMessage(): String {
        return "Enter a valid username or pseudonym"
    }

    override fun canMoveFurther(): Boolean {
        return nameEntered
    }

    override fun backgroundColor(): Int {
        if (color == 0) {
            return super.backgroundColor()
        }
        return color
    }

}