package de.hartz.software.parannoying.core.fragments.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import de.hartz.software.parannoying.core.R
import de.hartz.software.parannoying.core.activities.insecured.wiki.AboutActivity
import de.hartz.software.parannoying.core.extensions.launchActivity
import de.hartz.software.parannoying.core.fragments.welcome.generic.WelcomeSimpleTextFragment
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.getInfoIcon


open class WelcomeAboutFragment : WelcomeBaseFragment() {

    companion object {
        val KEY_HEADER = "KEY_HEADER"
    }

    private var color: Int = 0
    private lateinit var heading: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        heading = arguments?.getString(KEY_HEADER) ?: ""
        color = arguments?.getInt(WelcomeSimpleTextFragment.KEY_COLOR) ?: 0
        if (savedInstanceState != null) {
            //now do something with savedInstanceState
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.welcome_page_import, container, false)
        setText(view.findViewById<TextView>(R.id.welcomeHeading), heading)
        val importButton = view.findViewById<AppCompatImageButton>(R.id.chat_import)
        importButton.setImageDrawable(requireActivity().getInfoIcon(IconHelper.SMALL_ICON_WHITE))
        importButton.setOnClickListener {
            requireActivity().launchActivity<AboutActivity>()
        }

        // TODO: Get rid of..
        val dummy1 = view.findViewById<Button>(R.id.dummy_import1)
        dummy1.visibility = View.GONE
        val dummy2 = view.findViewById<Button>(R.id.dummy_import2)
        dummy2.visibility = View.GONE
        val dummy3 = view.findViewById<Button>(R.id.dummy_import3)
        dummy3.visibility = View.GONE
        val dummy4 = view.findViewById<Button>(R.id.dummy_import4)
        dummy4.visibility = View.GONE

        view.invalidate()
        return view
    }

    override fun backgroundColor(): Int {
        if (color == 0) {
            return super.backgroundColor()
        }
        return color
    }

}