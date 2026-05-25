package de.hartz.software.parannoying.air.gap.fragments.exchange.receive

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.snackbar.Snackbar
import de.hartz.software.parannoying.air.gap.R
import de.hartz.software.parannoying.air.gap.fragments.exchange.AbstractReceiveChannelFragment
import de.hartz.software.parannoying.air.gap.helpers.DatasetProcessor
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.core.helper.ui.getSDCardIcon
import de.hartz.software.parannoying.core.model.domain.settings.Channels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date


class SDCardReceiveChannelFragment: AbstractReceiveChannelFragment() {
    companion object {

        // TODO: DO WE NEED TO SWITCH THE FILE CAUSE OF READING AND WRITING WITH 2 DEVICES??
        fun getExternalFile(activity: Activity): File {
            val device = if (activity.Storage.isOfflineDevice()) {
                "received"
            } else {
                "send"
            }
            val removableSdCard = getSdDrives(activity).first()
            return File(removableSdCard, "ParannoyingReceivedMessages-" + device + ".txt")
        }

        fun getSdDrives(context: Context): List<File> {
            val externals = context.getExternalFilesDirs("para-messages")

            // TODO: for multiple sd cards we need to make it selectable..
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return externals.filter {
                    // Shift6MQ : Caused by: java.lang.IllegalArgumentException: Failed to find storage device at null
                    if (it == null) {
                        return@filter false
                    }
                    Environment.isExternalStorageRemovable(it)
                }
            }
            return listOf(externals.get(1))
        }

        fun isExternalStorageReadOnly(): Boolean {
            val extStorageState = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED_READ_ONLY == extStorageState
        }

        fun isExternalStorageAvailable(activity: Activity): Boolean {
            val extStorageState = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == extStorageState && isSDSupportedDevice(activity)
        }

        fun isSDSupportedDevice(activity: Activity): Boolean {
            return try {
                getExternalFile(activity)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    override val channel: Channels
        get() = Channels.SD_CARD

    val scope = CoroutineScope(Job() + Dispatchers.IO)

    override var buttonResource = { context: Context -> context.getSDCardIcon(IconHelper.SMALL_ICON_WHITE) }
    private lateinit var mainView: View

    override fun createMainView() { }

    override fun getMainView(): View {
        return mainView
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainView = inflater.inflate(R.layout.fragment_sd_card, container, false)

        val activity = requireActivity()
        val file = getExternalFile(activity)
        val mountedButton = mainView.findViewById<AppCompatButton>(R.id.mounted)
        val createdAtButton = mainView.findViewById<AppCompatButton>(R.id.createdAt)

        mountedButton.text = if (isExternalStorageAvailable(activity)) "SD-Card available" else "SD-Card unavailable"

        val lastModified = file.lastModified()
        val lastModDate = Date(file.lastModified())
        createdAtButton.text = if (lastModified != 0L) "Messages were added last time at $lastModDate" else "There are currently no messages."

        return mainView
    }


    override fun runAdditionalAction() {
        val activity = requireActivity()
        if (!isExternalStorageAvailable(activity)) {
            UiHelper.showToastFromBackgroundTask(requireContext(), "No sd card avaiable please insert")
            return
        }

        val file = getExternalFile(activity)
        if (!file.exists()) {
            UiHelper.showToastFromBackgroundTask(requireContext(), "There are no messages stored on sd card")
            return
        }
        val TAG = javaClass.simpleName
        scope.launch {
            DatasetProcessor()
                    .readFileChunks(file.absolutePath)
                    .forEach {
                        fragmentReceivedSomeData(it)
                        Log.e(TAG, it)
                        if (fragmentResultListener.isAllDataHandled) {
                            scope.cancel()
                            Log.e(TAG, "finished received receive")
                        }
                    }

            if(!Storage.DEVELOPER_MODE) {
                val deleted = file.delete()
                if (!deleted) {
                    throw RuntimeException("Failed to delete file")
                }
            }
        }

    }

    override fun additionalActionDescription() {
        Snackbar.make(requireView(), "Read data from external sd card.", Snackbar.LENGTH_LONG)
                .show()
    }

    override fun isStatusSupported(): Boolean {
        return false
    }

}