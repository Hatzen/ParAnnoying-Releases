package de.hartz.software.parannoying.app.medium.utils

import android.app.Activity
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.toBitmap
import androidx.test.espresso.ViewAction
import androidx.test.espresso.core.internal.deps.guava.collect.Iterables
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

object UiTestUtils {

    /**
     * @return a [WaitUntilGoneAction] instance created with the given [timeout] parameter.
     */
    fun waitUntilGone(timeout: Long, activity: Activity? = null): ViewAction {
        return WaitUntilGoneAction(timeout, activity)
    }

    @Throws(Throwable::class)
    fun getCurrentActivity(): Activity? {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val activity = arrayOfNulls<Activity>(1)

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
            activity[0] = Iterables.getOnlyElement(activities) as Activity
        }

        return activity[0]
    }


    fun withDrawable(@DrawableRes id: Int) = object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("ImageView with drawable same as drawable with id $id")
        }

        override fun matchesSafely(view: View): Boolean {
            val context = view.context
            val expectedBitmap = context.getDrawable(id)?.toBitmap()

            return view is ImageView && view.drawable.toBitmap().sameAs(expectedBitmap)
        }
    }
}