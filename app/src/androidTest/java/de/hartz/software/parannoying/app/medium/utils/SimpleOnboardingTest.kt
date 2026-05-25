package de.hartz.software.parannoying.app.medium.utils

import android.content.Intent
import androidx.appcompat.widget.AppCompatImageButton
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import de.hartz.software.parannoying.core.activities.insecured.welcome.WelcomeActivity
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class SimpleOnboardingTest {

    @Rule
    @JvmField var welcomeActivityRule: ActivityTestRule<WelcomeActivity> = ActivityTestRule(WelcomeActivity::class.java)

    @Test
    fun testSlidingThroughOnboarding() {
        val intent = Intent()

        // TODO: probably can be removed, just a fix for crashing
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        // When crashing with timeout disable all animations in devices system setting
        welcomeActivityRule.launchActivity(intent)

        nextSlide()
        nextSlide()
        nextSlide()
        nextSlide()
        nextSlide()
        nextSlide()
        nextSlide()
        nextSlide()

        welcomeActivityRule.finishActivity()
    }

    private fun nextSlide() {
        val button = welcomeActivityRule.activity.findViewById<AppCompatImageButton>(com.github.appintro.R.id.next)

        welcomeActivityRule.activity.runOnUiThread {
            button.performClick()
        }
        Thread.sleep(1500)
    }
}
