package de.hartz.software.parannoying.air.gap.fragments.exchange

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.hartz.software.parannoying.air.gap.R
import de.hartz.software.parannoying.air.gap.activities.AirGapActivity
import de.hartz.software.parannoying.air.gap.fragments.exchange.DataSendFragment.Companion.EXTRA_YES_NO
import de.hartz.software.parannoying.air.gap.helpers.DatasetProcessor
import de.hartz.software.parannoying.air.gap.helpers.ExchangeHelper
import de.hartz.software.parannoying.air.gap.interfaces.exchange.ExchangeFragmentListener
import de.hartz.software.parannoying.air.gap.model.FragmentNotReadyException
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.core.helper.ui.getKeyIcon
import de.hartz.software.parannoying.core.helper.ui.getNoIcon
import de.hartz.software.parannoying.core.helper.ui.getSendIcon
import de.hartz.software.parannoying.core.helper.ui.getYesIcon
import de.hartz.software.parannoying.core.model.domain.settings.Channels


abstract class AirGapFragment: Fragment(), ExchangeFragmentListener {

    enum class StatusColor {
        NO_MATCH, CONNECTING, CONNECTED
    }

    companion object {
        const val EXTRA_TEXT:String = "EXTRA_TEXT"
        const val EXTRA_PURPOSE:String = "EXTRA_PURPOSE"
        const val EXTRA_TARGET:String = "EXTRA_TARGET"
        const val EXTRA_PERSIST_LAUNCH_OPTIONS = "EXTRA_PERSIST_LAUNCH_OPTIONS"
        // Needed when exchanging messages for extra security (no man in the middle or spoofing)
        //  BUT: Will destroy foreign device sync (like userid, forwarding data etc.)
        const val EXTRA_TOKEN:String = "EXTRA_TOKEN"

        const val SINGLE_DATA_COUNT = 1
    }

    // General vars.
    protected lateinit var channelFragments: MutableList<AbstractExchangeChannelFragment>
    private lateinit var channelFragmentButtons: MutableList<ImageButton>
    private var changeFragmentLockActive = false
    private var waitForUnlock = false

    var useAdditionalEncryption: Boolean = false
    var currentChannelFragment: AbstractExchangeChannelFragment? = null
    var token = ExchangeHelper.DEFAULT_TOKEN

    var showYesNoButtons:Boolean = true

    open val isAllDataHandled get() = run { maxProgress <= currentProgress }
    var maxProgress = SINGLE_DATA_COUNT
    var currentProgress = 0
    val isSingleData get() = run {
        maxProgress == SINGLE_DATA_COUNT
    }


    val airGapActivity: AirGapActivity<*>? get() = activity as? AirGapActivity<*>

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        channelFragments = mutableListOf()
        channelFragmentButtons = mutableListOf()

        val intent = requireActivity().intent
        showYesNoButtons = intent.getBooleanExtra(EXTRA_YES_NO, false)
        token = intent.getStringExtra(AirGapFragment.EXTRA_TOKEN) ?: ExchangeHelper.DEFAULT_TOKEN
        useAdditionalEncryption = intent.getBooleanExtra(DataSendFragment.EXTRA_USE_ADDITIONAL_ENCRYPTION, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButtons()
        initView()
    }

    @CallSuper
    protected open fun processItem(item: String) {
        var count = DatasetProcessor.CASUAL_SEPERATOR_LIST.sumOf { item.split(it).size - 1 }
        if (count == 0) {
            Log.w(javaClass.simpleName, "No seperator found probably encrypted single send.")
            count = 1
            // throw RuntimeException("there should be at least one seperator")
        }
        // TODO: Receiving text leads to 469 of 500 for some reason..
        currentProgress += count

        requireActivity().runOnUiThread {
            airGapActivity?.refreshProgress()
            if (isAllDataHandled) {
                airGapActivity?.handleAllDataProcessed()
            }
        }
    }

    private fun initView() {
        AbstractExchangeChannelFragment.callback = this
        val buttonViewGroup = view?.findViewById<ViewGroup>(R.id.bar)
        val settings = Storage.readSettings()
        settings.allowedChannels.forEach {
            val fragment = getFragmentForChannel(it)
            fragment.createShowViewButton(requireContext(), buttonViewGroup)
            val fragmentButton = fragment.getShowViewButtonWrapper()
            // Set channel select listener.
            fragmentButton.setOnClickListener {
                // Remove active marker from previous.
                currentChannelFragment!!.getShowViewButtonWrapper().background = null
                try {
                    showFragment(fragment)
                } catch (e: FragmentNotReadyException) {
                    Log.i("initView", "" + e.message)
                } finally {
                    // Set active marker either for the current or previous.
                    currentChannelFragment!!.getShowViewButtonWrapper().setBackgroundResource(R.drawable.button_border_top) // Set button as selected.
                }
            }
            // Set primary channel.
            if (settings.primaryChannel == it) {
                changeFragmentLockActive = false // Prevent cached states.
                showFragment(fragment)
                fragmentButton.setBackgroundResource(R.drawable.button_border_top) // Set button as selected.
                // In send activity we need to refresh yes no buttons for specific channels
                initButtons()
            }
            channelFragmentButtons.add(fragmentButton)
            channelFragments.add(fragment)
        }
        val viewGroup = requireView().findViewById<LinearLayout>(R.id.bar)!!
        channelFragmentButtons.forEach {
            viewGroup.addView(it)
        }

        // Catch crashes in onboarding where realm is not setup
        if (currentChannelFragment == null) {
            val fragment = getFragmentForChannel(Channels.TEXT)!!
            showFragment(fragment)
        }
    }

    protected open fun initButtons() {
        val sendButton = requireView().findViewById<FloatingActionButton>(R.id.fab_action_send)
        sendButton.setImageDrawable(activity?.getSendIcon(IconHelper.MEDIUM_ICON_WHITE))
        sendButton.setOnClickListener{
            currentChannelFragment?.runAdditionalAction()
        }
        sendButton.setOnLongClickListener{
            currentChannelFragment?.additionalActionDescription()
            true
        }
        initKeyButton()
        initYesNoButton()
    }

    private fun initKeyButton () {
        val keyButton = requireView().findViewById<FloatingActionButton>(R.id.fab_key)
        keyButton?.setImageDrawable(activity?.getKeyIcon(IconHelper.MEDIUM_ICON_WHITE))
        if (!useAdditionalEncryption) {
            keyButton.visibility = View.GONE
        } else {
            keyButton.show()
            keyButton.setOnClickListener {
                onKeyButtonClicked()
            }
        }
    }

    abstract fun onKeyButtonClicked()


    fun initYesNoButton () {
        val context = requireContext()
        val rootView = requireView()
        val yesButton = rootView.findViewById<FloatingActionButton>(R.id.fab_yes)
        val noButton = rootView.findViewById<FloatingActionButton>(R.id.fab_no)

        yesNoButtonsVisible(showYesNoButtons)
        noButton?.setImageDrawable(context.getNoIcon(IconHelper.MEDIUM_ICON_WHITE))
        yesButton?.setImageDrawable(context.getYesIcon(IconHelper.MEDIUM_ICON_WHITE))
        yesButton.setOnClickListener {
            onYesClick()
        }
        // No.
        noButton.setOnClickListener {
            // TODO: It woudl be more proper to just go a message backwards? maybe longpress for cancle and single press for forward? Needs to consider userId scans.
            requireActivity().onBackPressed()
        }

        if (currentChannelFragment != null) {
            showYesNoButtonsForFragment(currentChannelFragment!!)
        }
    }

    private fun yesNoButtonsVisible(visibility: Boolean) {
        val yesButton = requireView().findViewById<FloatingActionButton>(R.id.fab_yes)
        val noButton = requireView().findViewById<FloatingActionButton>(R.id.fab_no)
        if (!visibility) {
            yesButton.visibility = View.GONE
            noButton.visibility = View.GONE
        } else {
            yesButton.show()
            noButton.show()
        }
    }

    private fun showYesNoButtonsForFragment(fragment: AbstractExchangeChannelFragment) {
        val buttonsNeeded = currentFragmentSupportsYesNo(fragment)

        val buttonsVisible = (hasMultipleData() || showYesNoButtons)
                && buttonsNeeded
        yesNoButtonsVisible(buttonsVisible)
    }
    abstract fun currentFragmentSupportsYesNo (fragment: AbstractExchangeChannelFragment): Boolean
    abstract fun onYesClick ()

    fun hasMultipleData (): Boolean {
        return maxProgress > 1
    }

    private fun canChannelBeSwitchedWithoutErrors() {
        // With use of animations monkeytest will crash with: java.lang.IllegalStateException: The specified child already has a parent. You must call removeView() on the child's parent first.
        if (!waitForUnlock) {
            Handler().postDelayed({
                changeFragmentLockActive = false
                waitForUnlock = false
            }, 1001) // Needs to take longer than the animation which take 500 + 500
        }
        waitForUnlock = true
        if (changeFragmentLockActive) {
            throw FragmentNotReadyException()
        }
        changeFragmentLockActive = true
    }

    @Throws(FragmentNotReadyException::class)
    private fun showFragment(fragment: AbstractExchangeChannelFragment) {
        canChannelBeSwitchedWithoutErrors()

        if (fragment.isStatusSupported()) {
            colorFrom = resources.getColor(R.color.colorRed)
            colorTo = resources.getColor(R.color.colorRed)
            AbstractExchangeChannelFragment.separatorView.setBackgroundColor(resources.getColor(R.color.colorRed))
            initStatusSwitcher()
        } else {
            AbstractExchangeChannelFragment.separatorView.setBackgroundColor(resources.getColor(R.color.grayAccent))
            cancleStatusSwitcher()
        }

        currentChannelFragment = fragment
        val transaction = requireFragmentManager().beginTransaction()
                .addToBackStack(null) // Avoid crashes .. https://stackoverflow.com/a/57790465/8524651
                .replace(R.id.container, currentChannelFragment!!)
        if(UiHelper.areSystemAnimationsEnabled(requireContext())) {
            // This can cause strange errors when executed again before animation finished.
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        transaction.commitAllowingStateLoss()

        // requireActivity().runOnUiThread {
        //     // running before change will crash for requireContext
        //     // Running after might lead to not proper updates
        //     beforeFragmentChange()
        // }

        // In send activity we need to refresh yes no buttons for specific channels
        initButtons()

        // running before change will crash for requireContext
        // Running after might lead to not proper updates
        beforeFragmentChange()
    }

    open fun beforeFragmentChange() {}

    private fun getFragmentForChannel(channel: Channels) : AbstractExchangeChannelFragment {
        val result = getFragment(channel)
        if (result != null) {
            return result
        }
        throw IllegalArgumentException("Channel $channel is not supported.")
    }

    abstract fun getFragment(channel: Channels) : AbstractExchangeChannelFragment?

    override fun onResume() {
        super.onResume()
        // Catch when no primary channel is selected. Maybe this is not a proper workflow.
        try {
            showFragment(currentChannelFragment!!)
        } catch (e: FragmentNotReadyException) {
            Log.d("onResume", "Test", e)
        }
    }


    override fun readyForTransmitting() {
    }

    override fun onDestroy() {
        super.onDestroy()
        // AbstractExchangeChannelFragment.callback = null // TODO: OnDestroy is called after next oncreate. Get rid of static usage..
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = inflater.inflate(R.layout.fragment_exchange, null, false)
        AbstractExchangeChannelFragment.separatorView = contentView.findViewById<View>(R.id.seperator)
        return contentView
    }

    private var colorAnimation: ValueAnimator? = null
    private val statusColorEvaluator = ArgbEvaluator()

    private var colorFrom : Int = 0
    private var colorTo : Int = 0

    public override fun setExchangeStatus(status: StatusColor) {
        if (!currentChannelFragment!!.isStatusSupported()) {
            colorAnimation?.cancel()
            colorAnimation = null
            return
        }
        colorFrom = (AbstractExchangeChannelFragment.separatorView.background as ColorDrawable).color

        colorTo = when (status) {
            StatusColor.NO_MATCH -> {
                resources.getColor(R.color.colorRed)
            }
            StatusColor.CONNECTING -> {
                resources.getColor(R.color.colorYellow)
            }
            StatusColor.CONNECTED -> {
                resources.getColor(R.color.colorPrimaryDark)
            }
        }

        colorAnimation?.cancel()
        colorAnimation?.setIntValues(colorFrom, colorTo)
        colorAnimation?.start()
    }

    @Synchronized
    private fun initStatusSwitcher() {
        AbstractExchangeChannelFragment.separatorView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        colorAnimation?.cancel()
        colorAnimation = ValueAnimator.ofObject(statusColorEvaluator, colorFrom, colorTo)
        colorAnimation!!.duration = 1000
        colorAnimation!!.addUpdateListener { animator ->
            run {
                AbstractExchangeChannelFragment.separatorView.setBackgroundColor(animator.animatedValue as Int)
            }
        }
        colorAnimation!!.start()
        colorAnimation!!.repeatCount = ValueAnimator.INFINITE
    }

    private fun cancleStatusSwitcher() {
        colorAnimation?.cancel()
        AbstractExchangeChannelFragment.separatorView.setBackgroundColor(resources.getColor(R.color.colorAccent))
    }

}