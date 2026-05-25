package de.hartz.software.parannoying.app.medium.air.gap

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.filters.MediumTest
import de.hartz.software.parannoying.air.gap.fragments.exchange.receive.TextReceiveChannelFragment
import de.hartz.software.parannoying.air.gap.fragments.exchange.send.TextSendChannelFragment
import de.hartz.software.parannoying.app.R
import de.hartz.software.parannoying.core.model.domain.settings.Channels
import org.junit.Test
import org.junit.runner.RunWith


@MediumTest
@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
class TextAirGapIT: AbstractAirGapIntegrationTest() {

    val messages = ArrayList<String>()
    @Test // TODO: Multiple data fails as entry 88 gets duplicated and cropped a bit..
    fun tesMultiple() {
        testChannelWithUseCase(Channels.TEXT, R.id.sendMultipleDataButton)
    }
    @Test
    fun testSingle() {
        testChannelWithUseCase(Channels.TEXT, R.id.sendSingleDataButton, send = this::singleSend)
    }

    fun singleSend() {
        Espresso.onView(ViewMatchers.withId(R.id.fab_action_send)).perform(ViewActions.click())

        val currentElement = TextReceiveChannelFragment.getClipboardContent(sendActivity)
        messages.add(currentElement)
        Espresso.pressBack()
        Thread.sleep(1000L)
    }
    override fun send() {
        Espresso.onView(ViewMatchers.withId(R.id.fab_action_send)).perform(ViewActions.click())

        val currentElement = TextReceiveChannelFragment.getClipboardContent(sendActivity)
        messages.add(currentElement)
        Espresso.onView(ViewMatchers.withId(R.id.fab_yes)).perform(ViewActions.click())
        Thread.sleep(1000L)
    }

    override fun receive() {
        messages.forEach {
            TextSendChannelFragment.setClipboardContent(receiveActivity, it)
            Espresso.onView(ViewMatchers.withId(R.id.fab_action_send)).perform(ViewActions.click())
            Thread.sleep(2000L)
        }
    }

}