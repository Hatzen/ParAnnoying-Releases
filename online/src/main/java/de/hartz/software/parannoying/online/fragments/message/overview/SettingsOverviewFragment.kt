package de.hartz.software.parannoying.online.fragments.message.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import de.hartz.software.parannoying.air.gap.model.UseCases
import de.hartz.software.parannoying.air.gap.model.UseCases.Online.CRASH_RECEIVE
import de.hartz.software.parannoying.core.activities.insecured.wiki.AboutActivity
import de.hartz.software.parannoying.core.activities.insecured.wiki.SideFactsActivity
import de.hartz.software.parannoying.core.adapter.GenericActionViewItemAdapter
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.extensions.launchActivity
import de.hartz.software.parannoying.core.fragments.ConnectionCheckStatusFragment
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
import de.hartz.software.parannoying.online.activities.online.OnlineLogActivity
import de.hartz.software.parannoying.online.activities.online.OnlineSettingsActivity
import de.hartz.software.parannoying.online.databinding.FragmentSettingsOverviewOnlineBinding
import de.hartz.software.parannoying.online.fragments.AbstractMainFragment

class SettingsOverviewFragment : AbstractMainFragment() {

    private lateinit var binding: FragmentSettingsOverviewOnlineBinding
    private lateinit var listView: ListView
    lateinit var adapter : GenericActionViewItemAdapter

    private val filePicker = FilePicker()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsOverviewOnlineBinding.inflate(inflater)

        listView = binding.actions
        val context =  requireContext()

        val items = listOfNotNull(
            ActionViewItem("Settings",
                {
                    requireActivity().launchActivity<OnlineSettingsActivity>()
                },
                context.getGearIcon(IconHelper.SMALL_ICON_PRIMARY),
                "Some Settings and if you tip on the version often enough you might find a secret."
            ),
            ActionViewItem("About",
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
            ActionViewItem("Show message log",
                {
                    requireActivity().launchActivity<OnlineLogActivity>()
                },
                context.getEventLogIcon(IconHelper.SMALL_ICON_PRIMARY),
                "Shows your inbox and outbox which you confirmed to send and received."
            ),
            ActionViewItem("Show onlineId",
                {
                    val onlineId = DataSecurityHelper(securityInterfaceHolder)
                        .getFullOnlineUserData(this.Storage)
                    airGapAdapter.startSend(UseCases.Online.ONLINEID_SEND.useText(onlineId))
                },
                context.getOnlineIdIcon(IconHelper.SMALL_ICON_PRIMARY),
                "Shows your onlineId to connect to scan on your offline device, usually just needed exactly once, dont give to any other user."
            ),
            ActionViewItem("Forward crashreport",
                {
                    airGapAdapter.startReceive(CRASH_RECEIVE)
                },
                context.getBugIcon(IconHelper.SMALL_ICON_PRIMARY),
                "Receive an offline crashrepor to send it with this online devices."
            ),
            if (onlineStorage.DEVELOPER_MODE) ActionViewItem("Crash me",
                {
                    throw DebuggingPurposeException("Crash for testing purpose!")
                },
                icon = null,
                "Development only to test acra."
            ) else null,
            ActionViewItem("Check connection",
                {
                    ConnectionCheckStatusFragment().show(requireActivity().supportFragmentManager, "ConnectionCheck")
                },
                icon = null,
                "When transfering data to offline device you should airgap the online device as well, otherwise the offline device might be located."
            ),
            ActionViewItem("Send cleartextfile",
                {
                    onSendCleartextFile()
                },
                context.getFileSendIcon(IconHelper.SMALL_ICON_PRIMARY),
                "Send any file to online or offline device, be aware files may contain trojans and may end your privacy."
            ),
            ActionViewItem("Receive cleartextfile",
                {
                    onReceiveCleartextFile()
                },
                context.getFileReceiveIcon(IconHelper.SMALL_ICON_PRIMARY),
                "Receive cleartextfile, be aware files may contain trojans and may end your privacy."
            )
        )

        requireActivity().runOnUiThread {
            adapter = GenericActionViewItemAdapter(
                items, requireContext())
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
