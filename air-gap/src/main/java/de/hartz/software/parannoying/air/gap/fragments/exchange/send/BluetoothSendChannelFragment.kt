package de.hartz.software.parannoying.air.gap.fragments.exchange.send

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.material.snackbar.Snackbar
import de.hartz.software.parannoying.air.gap.fragments.exchange.AbstractSendChannelFragment
import de.hartz.software.parannoying.air.gap.fragments.exchange.AirGapFragment
import de.hartz.software.parannoying.air.gap.helpers.channels.bluetooth.BluetoothHelper
import de.hartz.software.parannoying.air.gap.helpers.channels.bluetooth.ConnectedCallbackActivity
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.core.helper.ui.getBluetoothConnectingIcon
import de.hartz.software.parannoying.core.helper.ui.getBluetoothDisconnectedIcon
import de.hartz.software.parannoying.core.helper.ui.getBluetoothIcon
import de.hartz.software.parannoying.core.model.domain.settings.Channels
import java.util.Timer

class BluetoothSendChannelFragment: AbstractSendChannelFragment(), ConnectedCallbackActivity {
    override val channel: Channels
        get() = Channels.BLUETOOTH

    override var buttonResource = { context: Context -> context.getBluetoothIcon(IconHelper.SMALL_ICON_WHITE) }

    private lateinit var mainView: ImageView
    private lateinit var currentDrawable: Drawable
    private lateinit var bluetoothDrawables: Array<Drawable>
    private var imageSwitcherTimer: Timer? = null

    // NDEF record payloads are limited in size to 2^32–1 bytes long
    override val maxDataSize = 0xFF_FF_FF

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
        updateCurrentDrawable()
        if (BluetoothHelper.isBluetoothActive()) {
            BluetoothHelper.startDiscoverActivity()
        } else {
            BluetoothHelper.requestBluetooth()
        }
    }

    override fun additionalActionDescription() {
        Snackbar.make(requireView(), "Enable bluetooth and select the target device.", Snackbar.LENGTH_LONG)
                .show()
    }

    override fun isStatusSupported(): Boolean {
        return true
    }

    override fun init() {
        BluetoothHelper.initBluetoothService(requireActivity(), this)
        bluetoothDrawables = arrayOf(
            requireContext().getBluetoothConnectingIcon(IconHelper.LARGE_ICON_PRIMARY),
            requireContext().getBluetoothConnectingIcon(IconHelper.LARGE_ICON_WHITE)
        )
        updateCurrentDrawable()
        callback!!.setExchangeStatus(AirGapFragment.StatusColor.NO_MATCH)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        updateCurrentDrawable()
        if (BluetoothHelper.onActivityResult(requestCode, resultCode, data)) {
            // BluetoothSelectionActivity returned. Connecting is done in if condition asynchronyously. this.onBluetoothConnect() will be called when ready.
        }
        // Bluetooth getting enabled.
        if (requestCode == BluetoothHelper.REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            // TODO: we have to check if it is a correct device? Use a token system for bluetooth connection.
            if (!BluetoothHelper.isConnectedToDevice()) {
                BluetoothHelper.startDiscoverActivity()
            }
        }
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
        callback!!.setExchangeStatus(AirGapFragment.StatusColor.CONNECTED)
        BluetoothHelper.sendMessage(currentData, this)
    }

    override fun onBluetoothConnectionLost() {
        if (activity == null) {
            return
        }
        updateCurrentDrawable()
        callback!!.setExchangeStatus(AirGapFragment.StatusColor.NO_MATCH)
    }

    // TODO: Propre naming, or just seperate interfaces
    override fun onBluetoothMessageReceived(message: String) {
        fragmentResultListener.processNextItem()
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

    override fun startTransferDataSet(newData: String) {
        BluetoothHelper.sendMessage(newData, this)
    }

}