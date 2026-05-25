package de.hartz.software.parannoying.app.medium.air.gap

import android.view.View
import android.widget.TextView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.MediumTest
import de.hartz.software.parannoying.air.gap.fragments.exchange.DataSendFragment
import de.hartz.software.parannoying.air.gap.fragments.exchange.receive.TextReceiveChannelFragment
import de.hartz.software.parannoying.air.gap.fragments.exchange.send.TextSendChannelFragment
import de.hartz.software.parannoying.app.R
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.core.model.domain.settings.Channels
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith


@MediumTest
@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
class EncryptedTextAirGapIT: AbstractAirGapIntegrationTest() {

    lateinit var key: String
    val messages = ArrayList<String>()

    @Test
    fun testText() {
        testChannelWithUseCase(Channels.TEXT, R.id.sendEncryptedDataButton, R.id.receiveEncryptedDataButton)
    }

    override fun send() {
        Espresso.onView(ViewMatchers.withId(R.id.fab_key)).perform(ViewActions.click())
        // https://stackoverflow.com/a/33245290/8524651
        val snackbar = ViewMatchers.withId(com.google.android.material.R.id.snackbar_text)
        // ViewMatchers.withId(R.id.snackbar_text)
        Thread.sleep(500)

        val snackbartext = getText(snackbar)!!
        key = snackbartext.removePrefix(DataSendFragment.VALIDATION_TOKEN_PREFIX)

        Espresso.onView(ViewMatchers.withId(R.id.fab_action_send)).perform(ViewActions.click())
        Thread.sleep(200)
        val currentElement = TextReceiveChannelFragment.getClipboardContent(sendActivity)
        messages.add(currentElement)
        Espresso.pressBack()
        Thread.sleep(1000L)
    }

    override fun receive() {
        Espresso.onView(ViewMatchers.withId(R.id.fab_key)).perform(ViewActions.click())
        Thread.sleep(750)

        // https://stackoverflow.com/a/63669801/8524651
        // onView(withId(R.id.message)).inRoot(isDialog()).perform(typeTextIntoFocusedView(key));
        val performer = onView(withTagValue(CoreMatchers.equalTo(DialogHelper.INPUT_DIALOG_ID)))
            .inRoot(isDialog())

        performer.perform(click())
        performer.perform(typeTextIntoFocusedView(key));
        onView(withText("OK"))
            .inRoot(isDialog())
            .check(ViewAssertions.matches(isDisplayed()))
            .perform(click());

        messages.forEach {
            TextSendChannelFragment.setClipboardContent(receiveActivity, it)
            Espresso.onView(ViewMatchers.withId(R.id.fab_action_send)).perform(ViewActions.click())
            Thread.sleep(2000L)
        }
    }

    // https://stackoverflow.com/a/23467629/8524651
    fun getText(matcher: Matcher<View?>?): String? {
        val stringHolder = arrayOf<String?>(null)
        onView(matcher).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return isAssignableFrom(TextView::class.java)
            }

            override fun getDescription(): String {
                return "getting text from a TextView"
            }

            override fun perform(uiController: UiController?, view: View) {
                val tv = view as TextView //Save, because of check in getConstraints()
                stringHolder[0] = tv.text.toString()
            }
        })
        return stringHolder[0]
    }

}