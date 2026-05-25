package de.hartz.software.parannoying.core.activities.insecured

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle

// https://stackoverflow.com/questions/22166282/close-application-and-remove-from-recent-apps
class ExitActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            finishAndRemoveTask()
        } else {
            finish()
        }
    }

    companion object {
        fun exitApplication(context: Context) {
            val intent = Intent(context, ExitActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    or Intent.FLAG_ACTIVITY_NO_ANIMATION
                    or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            context.startActivity(intent)
        }
    }
}