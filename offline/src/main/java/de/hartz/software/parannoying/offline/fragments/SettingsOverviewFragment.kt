package de.hartz.software.parannoying.offline.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import de.hartz.software.parannoying.air.gap.model.UseCases
import de.hartz.software.parannoying.core.activities.insecured.wiki.AboutActivity
import de.hartz.software.parannoying.core.activities.insecured.wiki.SideFactsActivity
import de.hartz.software.parannoying.core.adapter.GenericActionViewItemAdapter
import de.hartz.software.parannoying.core.extensions.launchActivity
import de.hartz.software.parannoying.core.fragments.FileCheckStatusFragment
import de.hartz.software.parannoying.core.helper.io.FilePicker
import de.hartz.software.parannoying.core.helper.security.DataSecurityHelper
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.getBugIcon
import de.hartz.software.parannoying.core.helper.ui.getCardIcon
import de.hartz.software.parannoying.core.helper.ui.getEventLogIcon
import de.hartz.software.parannoying.core.helper.ui.getFileReceiveIcon
import de.hartz.software.parannoying.core.helper.ui.getFileSendIcon
import de.hartz.software.parannoying.core.helper.ui.getGearIcon
import de.hartz.software.parannoying.core.helper.ui.getInfoIcon
import de.hartz.software.parannoying.core.helper.ui.getOnlineIdIcon
import de.hartz.software.parannoying.core.model.exceptions.DebuggingPurposeException
import de.hartz.software.parannoying.core.model.view.ActionViewItem
import de.hartz.software.parannoying.offline.activities.offline.EventOverviewActivity
import de.hartz.software.parannoying.offline.activities.offline.settings.OfflineSettingsActivity
import de.hartz.software.parannoying.offline.databinding.FragmentSettingsOverviewBinding

class SettingsOverviewFragment : AbstractMainFragment() {

    private lateinit var binding: FragmentSettingsOverviewBinding
    private lateinit var listView: ListView
    lateinit var adapter: GenericActionViewItemAdapter
    private val filePicker = FilePicker()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsOverviewBinding.inflate(inflater)

        listView = binding.actions


        val context = requireContext()
        val isOfflineDeviceConnectedToARealOnlineDevice =
            DataSecurityHelper(securityInterfaceHolder).isOnlineIdValid(offlineStorage.currentUser.hash)

        val items = listOfNotNull(
            ActionViewItem(
                "Settings",
                {
                    requireActivity().launchActivity<OfflineSettingsActivity>()
                },
                context.getGearIcon(IconHelper.SMALL_ICON_PRIMARY),
                "Some Settings and if you tip on the version often enough you might find a secret."
            ),
            ActionViewItem(
                "About",
                {
                    requireActivity().launchActivity<AboutActivity>()
                },
                context.getInfoIcon(IconHelper.SMALL_ICON_PRIMARY),
                "Some FAQ to get more informations about different topics"
            ),
            ActionViewItem("Side Facts",
                {
                    requireActivity().launchActivity<SideFactsActivity>()
                },
                context.getCardIcon(IconHelper.SMALL_ICON_PRIMARY),
                "Get some random political, security and app related facts"
            ),
            ActionViewItem(
                "Event Log",
                {
                    requireActivity().launchActivity<EventOverviewActivity>()
                },
                context.getEventLogIcon(IconHelper.SMALL_ICON_PRIMARY),
                "Records of different events so you could track if someone used your phone without your knowledge."
            ),
            if (isOfflineDeviceConnectedToARealOnlineDevice) ActionViewItem(
                "Restore OnlineId",
                {
                    val dataSecurityHelper = DataSecurityHelper(securityInterfaceHolder)
                    val onlineId = dataSecurityHelper.getFullOnlineUserData(offlineStorage)
                    val renewFirebaseUseCase = UseCases.Offline.ONLINEID_SEND.useText(onlineId)
                    airGapAdapter.startSend(renewFirebaseUseCase)
                },
                context.getOnlineIdIcon(IconHelper.SMALL_ICON_PRIMARY),
                "Shows your onlineId which is connected to your UserIds so other people deliver the messages to you properly. Only needed when replacing your online device to scan in onboarding."
            ) else null,
            if (offlineStorage.DEVELOPER_MODE) ActionViewItem(
                "Crash me",
                {
                    throw DebuggingPurposeException("Crash for testing purpose!")
                },
                context.getBugIcon(IconHelper.SMALL_ICON_PRIMARY),
                "Development only to test acra."
            ) else null,
            ActionViewItem(
                "Send cleartextfile",
                {
                    onSendCleartextFile()
                },
                context.getFileSendIcon(IconHelper.SMALL_ICON_PRIMARY),
                "Send any file to online or offline device, be aware files may contain trojans and may end your privacy."
            ),
            ActionViewItem(
                "Receive cleartextfile",
                {
                    onReceiveCleartextFile()
                },
                context.getFileReceiveIcon(IconHelper.SMALL_ICON_PRIMARY),
                "Receive cleartextfile, be aware files may contain trojans and may end your privacy."
            )
        )

        requireActivity().runOnUiThread {
            adapter = GenericActionViewItemAdapter(
                items, requireContext()
            )
            listView.setAdapter(adapter)
            adapter.notifyDataSetChanged()
        }
        return binding.root
    }

    private fun onSendCleartextFile() {
        filePicker.pick(arrayOf("*/*")) { uri ->
            uri?.let {
                val file = filePicker.copyUriToTempFile(requireContext(), it)

                val fragment = FileCheckStatusFragment()
                fragment.file = file
                fragment.callback = {
                    airGapAdapter.startSend(UseCases.CLEARTEXT_FILE_SEND.useFile(file.absolutePath))
                }
                fragment.show(requireActivity().supportFragmentManager, "FileCheck")
            }
        }
    }

    private fun onReceiveCleartextFile() {
        airGapAdapter.startReceive(UseCases.CLEARTEXT_FILE_RECEIVE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        filePicker.register(this)
    }
}
