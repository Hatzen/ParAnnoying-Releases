package de.hartz.software.parannoying.offline.fragments

import androidx.fragment.app.Fragment
import de.hartz.software.parannoying.offline.activities.offline.OfflineMainActivity
import de.hartz.software.parannoying.offline.model.OfflineStorage


abstract class AbstractMainFragment : Fragment() {

    val offlineStorage get() = (requireActivity() as OfflineMainActivity).app.Storage as OfflineStorage
    val airGapAdapter get() = (requireActivity() as OfflineMainActivity).airGapAdapter
    val securityInterfaceHolder get() = (requireActivity() as OfflineMainActivity).securityInterfaceHolder

}
