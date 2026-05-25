package de.hartz.software.parannoying.online.activities.online

import android.content.DialogInterface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import de.hartz.software.parannoying.core.activities.BaseActivity
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.getTrashIcon
import de.hartz.software.parannoying.online.R
import de.hartz.software.parannoying.online.fragments.LogFragment
import de.hartz.software.parannoying.online.model.OnlineStorage


class OnlineLogActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_log)

        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(android.R.anim.slide_out_right, android.R.anim.slide_in_left)
            .replace(R.id.fragment_container, LogFragment())
            .commit()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Message Log"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(de.hartz.software.parannoying.core.R.menu.delete_all_menu, menu)
        return true
    }
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(de.hartz.software.parannoying.core.R.id.action_delete_all)?.icon = getTrashIcon(IconHelper.SMALL_ICON_WHITE)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == de.hartz.software.parannoying.core.R.id.action_delete_all) {
            DialogHelper.showYesNoAlert(this, "Are you sure to delete all entries? There is no side effect, but logs might be useful.", object: DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        (app.Storage as OnlineStorage).deleteAllLoggedEncryptedMessages()
                    }
                }

            })
        }
        if (item.itemId == android.R.id.home) {
            super.finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
