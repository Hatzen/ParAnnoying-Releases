package de.hartz.software.parannoying.online.fragments

import androidx.fragment.app.Fragment
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.online.activities.online.OnlineMainActivity
import de.hartz.software.parannoying.online.model.OnlineStorage


abstract class AbstractMainFragment : Fragment() {

    val onlineStorage get() = (requireActivity() as OnlineMainActivity).app.Storage as OnlineStorage
    val airGapAdapter get() = (requireActivity() as OnlineMainActivity).airGapAdapter
    val securityInterfaceHolder get() = (requireActivity() as OnlineMainActivity).securityInterfaceHolder

}
