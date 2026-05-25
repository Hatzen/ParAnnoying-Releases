package de.hartz.software.parannoying.offline.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import de.hartz.software.parannoying.offline.adapters.view.SelectUsersAdapter
import de.hartz.software.parannoying.offline.model.domain.dialogs.DummyUser
import de.hartz.software.parannoying.offline.model.domain.dialogs.User
import de.hartz.software.parannoying.offline.R
import de.hartz.software.parannoying.offline.model.OfflineStorage


class SelectUsersFragment : Fragment() {
    companion object {
        // ArrayList of UsersPersistenceIds which can be used to preselect users from beginning.
        const val EXTRA_USER_IDS:String = "EXTRA_USER_IDS"
        // Indicates that the current user should be displayed as well.
        const val EXTRA_SHOW_CURRENT_USER:String = "EXTRA_SHOW_CURRENT_USER"
        // Indicates that the rows are selectable.
        const val EXTRA_SELECTABLE:String = "EXTRA_SELECTABLE"
        // Display these simple userIds which do not have a reference in the Storage.
        const val EXTRA_ADDITIONAL_USERS_ONLINE_ID:String = "EXTRA_ADDITIONAL_USERS_ONLINE_ID"
    }

    lateinit var selectedUsers: ArrayList<Long>
    var unknownOnlineIds: ArrayList<String>? = null

    private lateinit var dialogsList: ListView
    private lateinit var dialogsAdapter: SelectUsersAdapter

    private var showCurrentUser: Boolean = false
    private var selectable: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showCurrentUser = requireActivity().intent
                .getBooleanExtra(EXTRA_SHOW_CURRENT_USER, false)
        selectable = requireActivity().intent
                .getBooleanExtra(EXTRA_SELECTABLE, true)
        selectedUsers = requireActivity().intent
                .getSerializableExtra(EXTRA_USER_IDS) as ArrayList<Long>
        unknownOnlineIds = requireActivity().intent
                .getSerializableExtra(EXTRA_ADDITIONAL_USERS_ONLINE_ID) as ArrayList<String>?
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_users_selection, null)
        dialogsList = view.findViewById(R.id.userList)

        dialogsAdapter = SelectUsersAdapter(getUsers(), selectedUsers, requireContext(), selectable)

        dialogsList.adapter = dialogsAdapter
        return view
    }

    private fun getUsers(): List<User> {
        val users = ArrayList(OfflineStorage.INSTANCE.users)
        if (!showCurrentUser) {
            users.remove(OfflineStorage.INSTANCE.currentUser)
        }
        unknownOnlineIds?.forEach {
            users.add(DummyUser(it))
        }
        return users
    }
}
