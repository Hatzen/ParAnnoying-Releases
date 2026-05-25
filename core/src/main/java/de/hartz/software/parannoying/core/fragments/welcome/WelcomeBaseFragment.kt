package de.hartz.software.parannoying.core.fragments.welcome

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.appintro.SlideBackgroundColorHolder
import com.github.appintro.SlidePolicy
import de.hartz.software.parannoying.core.R
import de.hartz.software.parannoying.core.helper.ui.UiHelper.showSnackbarTop


abstract class WelcomeBaseFragment : Fragment(), SlidePolicy, SlideBackgroundColorHolder {

    abstract override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?

    open fun backgroundColor(): Int {
        return R.color.grayAccent
    }

    open fun buttonsColor(): Int {
        return R.color.colorPrimaryDark
    }

    // If user should be allowed to leave this slide
    override val isPolicyRespected: Boolean
        get() = canMoveFurther() // Your custom logic here.

    override val defaultBackgroundColorRes
        get() = backgroundColor()

    override val defaultBackgroundColor
        get() = backgroundColor()

    override fun setBackgroundColor(backgroundColor: Int) {
        view?.setBackgroundColor(backgroundColor)
    }

    override fun onUserIllegallyRequestedNextPage() {
        val snackbarText =
            cantMoveFurtherErrorMessage()

        showSnackbarTop(snackbarText, requireView())
    }

    open fun canMoveFurther(): Boolean {
        return true
    }

    open fun cantMoveFurtherErrorMessage(): String {
        return "Please fill in the form to continue."
    }

    protected fun setText(textView: TextView, text: String?) {
        // https://stackoverflow.com/a/13206948/8524651
        // val text = "![CDATA[" + htmlcode + "]]>"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textView.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY))
        } else {
            textView.setText(Html.fromHtml(text))
        }
    }

}