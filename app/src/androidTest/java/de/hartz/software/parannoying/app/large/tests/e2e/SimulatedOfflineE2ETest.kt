package de.hartz.software.parannoying.app.large.tests.e2e

import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import org.junit.Assume
import org.junit.Test
import org.junit.runner.RunWith

// Sparse Test for emulated devices and github action.
@RunWith(AndroidJUnit4ClassRunner::class)
@LargeTest
class SimulatedOfflineE2ETest: AbstractE2ETest() {

    @Test
    fun testTwoUserCommunication() {
        // assumeGithubAction()

        onboardingDevice(DEVICE_A_ID)
        // Test sending message from current user to current user.
        createAndSendMessageToUser(DEVICE_A_ID, ONLINE_ID_BY_DEVICE_ID[DEVICE_A_ID]!!, arrayListOf(DEVICE_A_ID))

        onboardingDevice(DEVICE_B_ID)

        connectDevice(DEVICE_A_ID, DEVICE_B_ID)
        connectDevice(DEVICE_B_ID, DEVICE_A_ID)

        val sendMessageFromAToB = { createAndSendMessageToUser(DEVICE_A_ID, ONLINE_ID_BY_DEVICE_ID[DEVICE_B_ID]!!, arrayListOf(DEVICE_B_ID)) }
        val sendMessageFromBToA = { createAndSendMessageToUser(DEVICE_B_ID, ONLINE_ID_BY_DEVICE_ID[DEVICE_A_ID]!!, arrayListOf(DEVICE_A_ID)) }

        sendMessageFromAToB()
        sendMessageFromAToB()
        sendMessageFromBToA()
        sendMessageFromBToA()
        sendMessageFromAToB()
    }

    private fun assumeGithubAction() {
        Assume.assumeFalse("Using google api can not be tested: ", isOnlinePossible())
    }

    override fun isOfflineTest(): Boolean {
        return true
    }

}
