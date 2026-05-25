package de.hartz.software.parannoying.air.gap.test

import android.content.Context
import android.content.Intent
import android.widget.Button
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import de.hartz.software.parannoying.air.gap.activities.dummy.DummyAirGapActivity
import org.junit.*
import org.junit.runner.RunWith

@MediumTest
@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
class AirGapIntegrationTest {

    // Grant all Permissions of
    /*@Rule
    @JvmField var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.NFC,
            Manifest.permission.INTERNET)*/

    /*
    @Rule
    @JvmField var chatOverviewRule: ActivityTestRule<DummyAirGapActivity> = object: ActivityTestRule(DummyAirGapActivity::class.java, false, false) {
        override fun getApplication(): Application {
            return context
        }
    }
     */

    @Rule
    @JvmField var chatOverviewRule: ActivityTestRule<DummyAirGapActivity> = ActivityTestRule(DummyAirGapActivity::class.java, false, false)

    lateinit var context: Context
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<TestApp>()
    }


    /**
     *  TODO: Would be cool to test within this module, but hardly complicated or boilerplate to decouple from App
     * Currently tested in app module see AbstractAirGapIntegrationTest
     */
    @Ignore
    @Test
    fun testTextChannel() {
        chatOverviewRule.launchActivity(Intent())
        chatOverviewRule.activity.findViewById<Button>(R.id.sendSingleDataButton).performClick()
    }
}
