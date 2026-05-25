package de.hartz.software.parannoying.app.medium.air.gap

import android.Manifest
import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.EditText
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.MediumTest
import androidx.test.rule.GrantPermissionRule
import de.hartz.software.parannoying.air.gap.activities.dummy.DummyAirGapActivity
import de.hartz.software.parannoying.app.App
import de.hartz.software.parannoying.app.R
import de.hartz.software.parannoying.app.medium.utils.UiTestUtils.getCurrentActivity
import de.hartz.software.parannoying.core.model.domain.settings.Channels
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith


@MediumTest
@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
open class AbstractAirGapIntegrationTest {

    companion object {
        const val WAIT_90_SEC_FOR_RESULT = 90000L
        const val WAIT_FOR_UI_ANIMATIONS = 1000L
    }

    // Grant all Permissions of
    @get:Rule
    var mRuntimePermissionRule =  GrantPermissionRule.grant(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            // https://stackoverflow.com/q/53903976/8524651
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.NFC,
            Manifest.permission.INTERNET)

    @get : Rule
    var mActivityRule = ActivityScenarioRule(DummyAirGapActivity::class.java)

    lateinit var context: Context
    lateinit var receiveActivity: Activity
    lateinit var sendActivity: Activity
    var receiveCalls = 0
    open val maxReceiveCalls = 0

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<App>()

    }

    fun testChannelWithUseCase(
            channel: Channels,
            sendDummyButtonId: Int = R.id.sendMultipleDataButton,
            receiveDummyButtonId: Int = R.id.receiveDataButton,
            send: () -> Unit = this::send,
            receive: () -> Unit = this::receive) {
        val useCaseButton = onView(withId(sendDummyButtonId))
            .perform(scrollTo())
            .perform(closeSoftKeyboard())

        // Notification may hide button..
        // Thread.sleep(4000)

        useCaseButton.perform(click())

        var channelButton = withTagValue(CoreMatchers.equalTo(channel.getName(context)))
        onView(channelButton).perform(scrollTo())
        Thread.sleep(WAIT_FOR_UI_ANIMATIONS)
        onView(channelButton).perform(ViewActions.click())
        /*
        onView(channelButton).perform(scrollTo(), ViewActions.doubleClick())
        Thread.sleep(WAIT_FOR_UI_ANIMATIONS)
        onView(channelButton).perform(scrollTo(), ViewActions.doubleClick())
        // double click is needed for some reason.. And double click leads to rendering twice..
         */
        Thread.sleep(WAIT_FOR_UI_ANIMATIONS)

        sendActivity = getCurrentActivity()!!
        // Wait that textview content is set
        Thread.sleep(WAIT_FOR_UI_ANIMATIONS)
        while(!sendActivity.isFinishing) {
            send()
        }
        Thread.sleep(WAIT_FOR_UI_ANIMATIONS)

        onView(withId(receiveDummyButtonId)).perform(click())

        Thread.sleep(WAIT_FOR_UI_ANIMATIONS)

        channelButton = withTagValue(CoreMatchers.equalTo(channel.getName(context)))
        onView(channelButton).perform(scrollTo(), ViewActions.doubleClick())

        Thread.sleep(WAIT_FOR_UI_ANIMATIONS)

        receiveActivity = getCurrentActivity()!!

        Thread.sleep(WAIT_FOR_UI_ANIMATIONS)

        while(!receiveActivity.isFinishing) {
            if (receiveCalls > maxReceiveCalls) {
               error("Calling this multiple times might lead to problems: $maxReceiveCalls " + receiveCalls)
            }
            receiveCalls++
            receive()
            Thread.sleep(WAIT_FOR_UI_ANIMATIONS) // Needed to wait for finishing activity.
        }

        Thread.sleep(WAIT_FOR_UI_ANIMATIONS)

        onView(withId(R.id.resultTextView)).check(object: ViewAssertion {
            override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
                val text = (view as EditText).text.toString()
                if (!text.contains("has differences: false")) {
                    throw RuntimeException("Data differs: " + text)
                }
            }

        })
    }

    open fun send() {
        throw NotImplementedError()
    }

    open fun receive() {
        throw NotImplementedError()
    }

}