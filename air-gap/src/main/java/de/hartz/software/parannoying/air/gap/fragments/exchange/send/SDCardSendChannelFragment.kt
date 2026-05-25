package de.hartz.software.parannoying.air.gap.fragments.exchange.send

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.snackbar.Snackbar
import de.hartz.software.parannoying.air.gap.R
import de.hartz.software.parannoying.air.gap.fragments.exchange.AbstractSendChannelFragment
import de.hartz.software.parannoying.air.gap.fragments.exchange.receive.SDCardReceiveChannelFragment
import de.hartz.software.parannoying.air.gap.fragments.exchange.receive.SDCardReceiveChannelFragment.Companion.getExternalFile
import de.hartz.software.parannoying.air.gap.fragments.exchange.receive.SDCardReceiveChannelFragment.Companion.isExternalStorageAvailable
import de.hartz.software.parannoying.air.gap.fragments.exchange.receive.SDCardReceiveChannelFragment.Companion.isExternalStorageReadOnly
import de.hartz.software.parannoying.air.gap.helpers.DatasetProcessor
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.core.helper.ui.getSDCardIcon
import de.hartz.software.parannoying.core.model.domain.settings.Channels
import java.io.FileOutputStream
import java.util.Date


class SDCardSendChannelFragment: AbstractSendChannelFragment() {


    override val channel: Channels
        get() = Channels.SD_CARD

    override var buttonResource = { context: Context -> context.getSDCardIcon(IconHelper.SMALL_ICON_WHITE) }
    private lateinit var mainView: View
    private var fos: FileOutputStream? = null
    private var sdAvailable = false

    // 2MB String size
    override val maxDataSize = 200_000_000

    override fun deinit() {
        fos?.close()
        if (!fragmentResultListener.isAllDataHandled) {
            Log.w(javaClass.simpleName, "Deleted file as send got canceld.")
            val outputFile = getExternalFile(requireActivity())
            outputFile.delete()
        }
    }

    override fun init() {
        super.init()

        sdAvailable = isExternalStorageAvailable(requireActivity()) && !isExternalStorageReadOnly()
    }


    override fun createMainView() { }

    override fun getMainView(): View {
        return mainView
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mainView = inflater.inflate(R.layout.fragment_sd_card, container,false)

        val file = SDCardReceiveChannelFragment.getExternalFile(requireActivity())
        val mountedButton = mainView.findViewById<AppCompatButton>(R.id.mounted)
        val createdAtButton = mainView.findViewById<AppCompatButton>(R.id.createdAt)

        mountedButton.text = if (isExternalStorageAvailable(requireActivity())) "SD-Card available" else "SD-Card unavailable"

        val lastModified = file.lastModified()
        val lastModDate = Date(file.lastModified())
        createdAtButton.text = if (lastModified != 0L) "Messages were added last time at $lastModDate" else "There are currently no messages."

        return mainView
    }

    override fun runAdditionalAction() {
        val alreadyInitialized = fos != null
        if (alreadyInitialized || !sdAvailable) {
            return
        }
        writeFile()
        startTransferDataSet(currentData)
    }

    fun writeFile() {
        val outputFile = getExternalFile(requireActivity())
        if (!outputFile.exists()) {
            outputFile.createNewFile()
        }
        fos = FileOutputStream(outputFile)
    }

    private fun checkTransmissionPossible(): Boolean {
        // activity might be null when leaving before finishing transfer
        if (activity == null || fos == null) {
            return false
        }
        if (!sdAvailable) {
            UiHelper.showToastFromBackgroundTask(requireContext(), "No sd card avaiable please insert")
            return false
        }
        return true
    }

    override fun additionalActionDescription() {
        Snackbar.make(requireView(),  "data from external sd card.", Snackbar.LENGTH_LONG)
                .show()
    }

    override fun isStatusSupported(): Boolean {
        return false
    }

    override fun startTransferDataSet(newData: String) {
        if (!checkTransmissionPossible()) {
            return
        }
        writeLine(newData)
        fragmentResultListener.processNextItem()
    }

    private fun writeLine(message: String) {
        // TODO: Seperator probably not correct for all cases..
        fos!!.write((message + DatasetProcessor.DATASET_SEPERATOR).toByteArray())
    }
}