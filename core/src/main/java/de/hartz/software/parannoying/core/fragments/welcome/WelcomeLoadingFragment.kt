package de.hartz.software.parannoying.core.fragments.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.hartz.software.parannoying.core.R
import de.hartz.software.parannoying.core.fragments.welcome.generic.WelcomeSimpleTextFragment


open class WelcomeLoadingFragment : WelcomeBaseFragment() {

    companion object {
        val KEY_HEADER = "KEY_HEADER"
        val KEY_TEXT = "KEY_TEXT"
    }

    private var color: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        color = arguments?.getInt(WelcomeSimpleTextFragment.KEY_COLOR) ?: 0
        if (savedInstanceState != null) {
            //now do something with savedInstanceState
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.welcome_page_loading, container, false)
        return view
    }

    override fun backgroundColor(): Int {
        if (color == 0) {
            return super.backgroundColor()
        }
        return color
    }
}