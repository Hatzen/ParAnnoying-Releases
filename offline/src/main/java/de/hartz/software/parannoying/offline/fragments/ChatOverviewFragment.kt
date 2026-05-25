package de.hartz.software.parannoying.offline.fragments

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.dialogs.DialogsList
import com.stfalcon.chatkit.dialogs.DialogsListAdapter
import de.hartz.software.parannoying.core.extensions.launchActivity
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.offline.R
import de.hartz.software.parannoying.offline.activities.offline.ChatActivity
import de.hartz.software.parannoying.offline.activities.offline.ChatActivity.Companion.EXTRA_USER
import de.hartz.software.parannoying.offline.activities.offline.userinfo.UserInfoActivity
import de.hartz.software.parannoying.offline.adapters.view.ChatOverviewAdapter
import de.hartz.software.parannoying.offline.adapters.view.SearchResultAdapter
import de.hartz.software.parannoying.offline.databinding.FragmentChatOverviewBinding
import de.hartz.software.parannoying.offline.model.domain.Type
import de.hartz.software.parannoying.offline.model.domain.dialogs.BaseDialog
import de.hartz.software.parannoying.offline.model.domain.dialogs.UniqueDialogIdWrapper
import de.hartz.software.parannoying.offline.model.domain.events.BaseEvent
import de.hartz.software.parannoying.offline.model.domain.events.SimpleEvent
import de.hartz.software.parannoying.offline.model.domain.events.UserEvent

class ChatOverviewFragment : AbstractMainFragment(), DialogsListAdapter.OnDialogClickListener<BaseDialog> {

    private var selectedDialogId: UniqueDialogIdWrapper? = null
    private lateinit var dialogsList: DialogsList
    private lateinit var dialogsAdapter: ChatOverviewAdapter
    private lateinit var binding: FragmentChatOverviewBinding
    private lateinit var searchResultsList: RecyclerView
    private lateinit var searchAdapter: SearchResultAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatOverviewBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialogsList = requireView().findViewById(de.hartz.software.parannoying.offline.R.id.dialogsList)
        initAdapter()
        // TODO: Maybe customize menu: https://www.androhub.com/android-popup-menu/
        registerForContextMenu(dialogsList)

        searchResultsList = view.findViewById(R.id.searchResultsList)
        searchResultsList.layoutManager = LinearLayoutManager(requireContext())
        val animator = DefaultItemAnimator().apply {
            addDuration = 250
            removeDuration = 250
            moveDuration = 250
            changeDuration = 250
        }
        searchResultsList.itemAnimator = animator
        searchAdapter = SearchResultAdapter { result ->
            if (result.type == Type.USER) {
                onDialogClick(result.baseDialog)
            } else {
                val passParams : (userIntent: Intent) -> Unit = {
                    it.putExtra(ChatActivity.EXTRA_USER, result.baseDialog.getUniqueDialogId())
                    it.putExtra(ChatActivity.EXTRA_MESSAGE, result.message!!.persistenceId)
                }
                requireActivity().launchActivity<ChatActivity>(init = passParams)
            }
        }
        searchResultsList.adapter = searchAdapter

        setHasOptionsMenu(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        // Revalidate the read messages count on retun of chatactivity.
        dialogsList = requireView().findViewById(R.id.dialogsList)
        initAdapter()

        /*
        // TODO: Considered?
        // Update outstanding sync messages.
        val fabSync = binding.fabSync
        fabSync.count = offlineStorage.readSendMessage().size
         */
    }

    override fun onDialogClick(dialog: BaseDialog) {
        val passParams : (userIntent: Intent) -> Unit = {
            it.putExtra(EXTRA_USER, dialog.getUniqueDialogId())
        }
        requireActivity().launchActivity<ChatActivity>(init = passParams)
    }

    private fun initAdapter() {
        val context = requireContext()
        val imageLoader = ImageLoader { imageView, avatarImageKey, payload ->
            imageView.setImageBitmap(IOHelper.getProfilePictureForUser(avatarImageKey!!, context))
        }
        dialogsAdapter = ChatOverviewAdapter(securityInterfaceHolder, context, imageLoader)
        val dialogs = offlineStorage.getDialogs()
        dialogsAdapter.setItems(dialogs)

        dialogsAdapter.setOnDialogClickListener(this)
        dialogsAdapter.setOnDialogViewLongClickListener { view, dialog ->
            selectedDialogId = dialog
            requireActivity().openContextMenu(view)
        }
        dialogsList.setAdapter(dialogsAdapter)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit_name -> {
                renameUser()
                return true
            }
            R.id.delete -> {
                deleteUser()
                return true
            }
            R.id.info -> {
                showUserInfo()
                return true
            }
            else -> return super.onContextItemSelected(item)
        }
    }

    private fun renameUser () {
        val callback = object: DialogHelper.InputDialogCallback() {
            override fun onFinish(input: String) {
                val user = selectedDialogId!!.getDialog(offlineStorage)
                user.nickname = input
                offlineStorage.updateDialog(user)
                offlineStorage.persistEvent(UserEvent(BaseEvent.EVENT_RENAMED_USER, user))
                initAdapter()
            }
        }
        DialogHelper.showInputDialog(requireContext(), "Enter the new name for the user.", true, callback)
    }

    private fun deleteUser () {
        DialogHelper.showYesNoAlert(requireContext(), "Are you sure to delete this user?", object: DialogInterface.OnClickListener {
            override fun onClick(d: DialogInterface?, which: Int) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    val dialog = selectedDialogId!!.getDialog(offlineStorage)
                    offlineStorage.deleteDialog(dialog)
                    offlineStorage.persistEvent(SimpleEvent(BaseEvent.EVENT_DELETED_USER))
                    initAdapter()
                }
            }
        })
    }

    private fun showUserInfo () {
        val passParams : (userIntent: Intent) -> Unit = {
            it.putExtra(EXTRA_USER, selectedDialogId!!.getUniqueDialogId())
        }
        requireActivity().launchActivity<UserInfoActivity>(init = passParams)
    }

    fun createUserCallback() {
        requireActivity().runOnUiThread {
            initAdapter() // Revalidate all users. After scanning one it does not have a proper persistenceId for some reason..
        }
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chat_overview_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)

        val searchView = searchItem.actionView as SearchView
        val searchText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchText.setTextColor(Color.WHITE)
        // searchText.setHintTextColor(Color.LTGRAY)

        searchView.queryHint = "Search users or messages..."
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    showSearchResults(newText)
                } else {
                    showDialogsList()
                }
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean = false
        })

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean = true

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                showDialogsList()
                return true
            }
        })
    }

    private fun showSearchResults(query: String) {
        dialogsList.visibility = View.GONE
        searchResultsList.visibility = View.VISIBLE

        val results = this.offlineStorage.searchDialogsAndMessages(query)
        searchAdapter.submitList(results) {
            searchResultsList.scrollToPosition(0)
        }
    }

    private fun showDialogsList() {
        dialogsList.visibility = View.VISIBLE
        searchResultsList.visibility = View.GONE
    }

}
