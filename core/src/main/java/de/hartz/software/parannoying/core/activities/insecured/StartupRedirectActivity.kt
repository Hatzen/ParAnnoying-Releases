package de.hartz.software.parannoying.core.activities.insecured

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.helper.ShortcutHelper.EXTRA_SHORTCUT_ID
import de.hartz.software.parannoying.core.helper.ShortcutHelper.EXTRA_VALUE_SYNC
import de.hartz.software.parannoying.core.helper.ShortcutHelper.EXTRA_VALUE_USERID
import de.hartz.software.parannoying.core.interfaces.di.ActivityProvider
import javax.inject.Inject


class StartupRedirectActivity : AppCompatActivity() {

    companion object {
        const val ACTION_SYNC = "de.hartz.software.parannoying.core.ACTION_SHORTCUT_SYNC"
        const val ACTION_USERID = "de.hartz.software.parannoying.core.ACTION_SHORTCUT_USERID"
    }

    @Inject
    lateinit var activityProvider: ActivityProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app.coreComponents.inject(this)

        routeToAppropriateActivity()
        finish()
    }

    private fun routeToAppropriateActivity() {
        val welcomeClass = activityProvider.getStartActivityClass()

        var shortcut = when (intent.action) {
            ACTION_SYNC -> {
                EXTRA_VALUE_SYNC
            }
            ACTION_USERID -> {
                EXTRA_VALUE_USERID
            }
            else -> {
                null
            }
        }
        val intent = Intent(this, welcomeClass)
            .putExtra(EXTRA_SHORTCUT_ID, shortcut)
        startActivity(intent)
    }

}