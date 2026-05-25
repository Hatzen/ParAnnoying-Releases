package de.hartz.software.parannoying.app.large.tests.utils

import android.app.Activity
import android.content.Intent
import androidx.test.rule.ActivityTestRule

// https://proandroiddev.com/fix-kotlin-and-new-activitytestrule-the-rule-must-be-public-f0c5c583a865
class StrictActivityTestRule <T: Activity> (activityClass: Class<T>)
    // initialTouchMode  // launchActivity. False to set intent per test);)
    : ActivityTestRule<T>(activityClass, false, false) {

    override fun finishActivity() {
        val activity = this.activity
        super.finishActivity()

        // Both needed otherwise we call onDestroy checking connection
        // and tryin to persist OfflineEventPersistence within OnlineRealm leading to a crash
        while(!activity.isDestroyed) { }
        Thread.sleep(1000)
    }

}