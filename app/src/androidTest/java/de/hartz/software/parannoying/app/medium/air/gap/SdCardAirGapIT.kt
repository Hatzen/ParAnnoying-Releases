package de.hartz.software.parannoying.app.medium.air.gap

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import de.hartz.software.parannoying.app.R
import de.hartz.software.parannoying.app.medium.utils.UiTestUtils
import de.hartz.software.parannoying.core.model.domain.settings.Channels
import org.junit.Ignore
import org.junit.Test


class SdCardAirGapIT: AbstractAirGapIntegrationTest() {
    @Test
    fun testSdCardWithMultiple() {
        testChannelWithUseCase(Channels.SD_CARD, R.id.sendMultipleDataButton)
    }

    // Timeout after 90sec send. Maybe boost performance to write on internalstorage and move afterwards?
    @Ignore
    @Test
    fun testSdCardWithMixedData() {
        testChannelWithUseCase(Channels.SD_CARD, R.id.sendMixedDataButton)
    }


    override fun send() {
        Espresso.onView(ViewMatchers.withId(R.id.fab_action_send)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.progress_bar))
                .perform(UiTestUtils.waitUntilGone(WAIT_90_SEC_FOR_RESULT, sendActivity))
    }

    override fun receive() {
        Espresso.onView(ViewMatchers.withId(R.id.fab_action_send)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.progress_bar))
                .perform(UiTestUtils.waitUntilGone(WAIT_90_SEC_FOR_RESULT, receiveActivity))
    }

}