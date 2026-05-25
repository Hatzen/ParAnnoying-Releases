package de.hartz.software.parannoying.app.large.tests.creators

import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import de.hartz.software.parannoying.app.large.tests.e2e.AbstractE2ETest
import org.junit.*
import org.junit.runner.RunWith

// Sparse Test for emulated devices and github action.
@RunWith(AndroidJUnit4ClassRunner::class)
@LargeTest
class CreatePushNotification: AbstractE2ETest() {

    @Ignore
    @Test
    fun testTwoUserCommunication() {
        // TODO: Import dummy data and send a MEssage from A => B Without receiving with A

    }

}
