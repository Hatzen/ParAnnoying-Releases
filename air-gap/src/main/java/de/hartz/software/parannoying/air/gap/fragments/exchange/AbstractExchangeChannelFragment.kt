package de.hartz.software.parannoying.air.gap.fragments.exchange

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import de.hartz.software.parannoying.air.gap.activities.AirGapActivity
import de.hartz.software.parannoying.air.gap.interfaces.exchange.ExchangeFragmentListener
import de.hartz.software.parannoying.core.R
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.core.model.domain.settings.Channels

abstract class AbstractExchangeChannelFragment: Fragment() {
    companion object {
        lateinit var separatorView: View
        var callback: ExchangeFragmentListener? = null
    }

    abstract val channel: Channels
    private lateinit var showViewButton: ImageButton

    protected open lateinit var buttonResource: (Context) -> Drawable

    private val useAutomaticConfirmation: Boolean by lazy {
        requireContext().app.Storage.readSettings().useAutomaticConfirmation
    }

    protected val securityInterfaceHolder: SecurityInterfaceHolder by lazy {
        getAppCompatActivity().securityInterfaceHolder
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        createMainView()
    }

    override fun onPause() {
        super.onPause()
        deinit()
    }

    abstract fun getMainView(): View

    abstract fun createMainView ()

    abstract fun runAdditionalAction ()

    // TODO unify showing snackbar etc..
    abstract fun additionalActionDescription ()

    abstract fun isStatusSupported() : Boolean

    fun useConfirmation(): Boolean {
        return isConfirmationSupported() &&
                useAutomaticConfirmation
    }

    protected open fun isConfirmationSupported(): Boolean {
        return false
    }
    /**
     * To be called only once until deinit is called.
     */
    open fun init() {}


    open fun deinit() {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return getMainView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        callback!!.readyForTransmitting()
    }

    fun createShowViewButton (context: Context, rootView: ViewGroup?) {
        val imageButton = LayoutInflater.from(context).inflate(R.layout.image_button_channel, rootView, false) as ImageButton
        imageButton.setImageDrawable(buttonResource(context))
        val text = channel.getName(context)
        imageButton.setOnLongClickListener {
            UiHelper.showToastFromBackgroundTask(context, "" + text)
            true
        }
        imageButton.tag = text
        showViewButton = imageButton
    }

    open fun getShowViewButtonWrapper () : ImageButton {
        return showViewButton
    }

    protected fun getAppCompatActivity(): AirGapActivity<*> {
        return requireActivity() as AirGapActivity<*>
    }

}