package de.hartz.software.parannoying.offline.activities.offline

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.offline.R
import de.hartz.software.parannoying.offline.databinding.ActivityCreateOnlineGroupBinding
import de.hartz.software.parannoying.offline.fragments.SelectUsersFragment
import de.hartz.software.parannoying.offline.helper.security.DialogCreationHelper
import javax.inject.Inject

// TODO: Rework to use onboarding slider with 3 slides. name, members, overview/finish
//   maybe same for offline group with option to enable token system (needs to be sent to everyone)
class CreateOnlineGroupActivity : BaseOfflineActivity() {

    @Inject
    lateinit var securityInterfaceHolder: SecurityInterfaceHolder

    private lateinit var groupName: String
    private lateinit var fragment: SelectUsersFragment

    private lateinit var binding: ActivityCreateOnlineGroupBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateOnlineGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        DialogHelper.showInputDialog(this, "Enter name for secure group", false, object: DialogHelper.InputDialogCallback() {
            override fun onFinish(input: String) {
                groupName = input
            }

            override fun onCancel() {
                this@CreateOnlineGroupActivity.finish()
            }
        })

        fragment = SelectUsersFragment()
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_create_online_group, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.create) {
            DialogCreationHelper(this, securityInterfaceHolder).createOnlineGroup(groupName, getFirebaseIds(), this)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getFirebaseIds (): ArrayList<String> {
        val persistenceIds = fragment.selectedUsers
        persistenceIds.add(Storage.currentUser.persistenceId) // Currentuser is part of the group.
        return Storage.users.filter { persistenceIds.contains(it.persistenceId) }
                .map { it.hash }.toCollection(ArrayList())
    }

}