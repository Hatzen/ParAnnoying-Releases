package de.hartz.software.parannoying.app.large.tests

import android.content.Intent
import android.widget.Button
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import de.hartz.software.parannoying.core.activities.insecured.welcome.WelcomeActivity
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class SimpleOnboardingTest {

    @Rule
    @JvmField var welcomeActivityRule: ActivityTestRule<WelcomeActivity> = ActivityTestRule(WelcomeActivity::class.java)

    @Ignore("Crashes as App is not AbstractApp within this module..")
    @Test
    fun testSlidingThroughOnboarding() {
        welcomeActivityRule.launchActivity(Intent())
        val button = welcomeActivityRule.activity.findViewById<Button>(com.github.appintro.R.id.next)
        button.performClick()
        button.performClick()
        button.performClick()
        button.performClick()
        button.performClick()
        button.performClick()
        button.performClick()
        button.performClick()
        button.performClick()
    }

}
