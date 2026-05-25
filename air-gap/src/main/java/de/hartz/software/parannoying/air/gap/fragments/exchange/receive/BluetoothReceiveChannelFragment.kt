package de.hartz.software.parannoying.air.gap.fragments.exchange.receive

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.material.snackbar.Snackbar
import de.hartz.software.parannoying.air.gap.fragments.exchange.AbstractReceiveChannelFragment
import de.hartz.software.parannoying.air.gap.fragments.exchange.AirGapFragment
import de.hartz.software.parannoying.air.gap.helpers.channels.bluetooth.BluetoothHelper
import de.hartz.software.parannoying.air.gap.helpers.channels.bluetooth.ConnectedCallbackActivity
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.core.helper.ui.getBluetoothDisconnectedIcon
import de.hartz.software.parannoying.core.helper.ui.getBluetoothIcon
import de.hartz.software.parannoying.core.model.domain.settings.Channels
import java.util.Timer

// TODO: We can send one message successfully so far, but with multiple messages we seem to disconnect after first message for some reason..
class BluetoothReceiveChannelFragment: AbstractReceiveChannelFragment(), ConnectedCallbackActivity {
    override val channel: Channels
        get() = Channels.BLUETOOTH

    override var buttonResource = { context: Context -> context.getBluetoothIcon(IconHelper.SMALL_ICON_WHITE) }

    private lateinit var mainView: ImageView
    private lateinit var bluetoothDrawables: Array<Drawable>
    private lateinit var currentDrawable: Drawable
    private var imageSwitcherTimer: Timer? = null

    override fun getMainView(): View {
        return mainView
    }

    override fun createMainView () {
        mainView = ImageView(activity)
        mainView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
    }

    override fun runAdditionalAction() {
        imageSwitcherTimer = UiHelper.initImageSwitcher(requireActivity(), mainView, bluetoothDrawables)
        BluetoothHelper.initBluetoothService(requireActivity(), this)
        BluetoothHelper.ensureDiscoverable()
        BluetoothHelper.onResume()
    }

    override fun additionalActionDescription() {
        Snackbar.make(requireView() , "Enable bluetooth and enable discovery mode for receiving.", Snackbar.LENGTH_LONG)
                .show()
    }

    override fun isStatusSupported(): Boolean {
        return true
    }

    override fun init() {
        bluetoothDrawables = arrayOf(
                requireContext().getBluetoothIcon(IconHelper.LARGE_ICON_PRIMARY),
                requireContext().getBluetoothIcon(IconHelper.LARGE_ICON_WHITE)
        )
        updateCurrentDrawable()
        // callback.setExchangeStatus(ReceiveChannelsFragment.StatusColor.NO_MATCH)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Do not move to deinit as init launches activity leading to onpause and then calling deinit instantly.
        BluetoothHelper.onDestroy()
    }

    override fun deinit() {
        imageSwitcherTimer?.cancel()
    }

    override fun onBluetoothConnect() {
        updateCurrentDrawable()
        callback!!.setExchangeStatus(AirGapFragment.StatusColor.CONNECTING)
    }

    override fun onBluetoothConnectionLost() {
        if (activity == null) {
            // TODO: clean
            // Activity gets finished when connection gets cut on last message.
            Log.e(javaClass.simpleName, "updateCurrentDrawable with activitiy == null for some reason..")
            return
        }
        updateCurrentDrawable()
        callback!!.setExchangeStatus(AirGapFragment.StatusColor.NO_MATCH)
    }

    override fun onBluetoothMessageReceived(message: String) {
        updateCurrentDrawable()
        callback!!.setExchangeStatus(AirGapFragment.StatusColor.CONNECTED)
        fragmentReceivedSomeData(message)
    }

    private fun updateCurrentDrawable () {
        imageSwitcherTimer?.cancel()
        if (!BluetoothHelper.isBluetoothActive()) {
            currentDrawable = requireActivity().getBluetoothDisconnectedIcon(IconHelper.LARGE_ICON_WHITE)
            mainView.setImageDrawable( currentDrawable )
        } else {
            currentDrawable = requireActivity().getBluetoothIcon(IconHelper.LARGE_ICON_WHITE)
            mainView.setImageDrawable( currentDrawable )
            if (BluetoothHelper.isConnectedToDevice()) {
                UiHelper.initImageSwitcher(requireActivity(), mainView, bluetoothDrawables)
            }
        }
    }
}