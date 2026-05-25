package de.hartz.software.parannoying.app.medium

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import de.hartz.software.parannoying.app.utils.DaggerTestComponent
import de.hartz.software.parannoying.core.activities.insecured.welcome.WelcomeActivity
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.helper.InitializationHelper
import de.hartz.software.parannoying.core.helper.security.DataSecurityHelper
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.interfaces.AbstractApp
import de.hartz.software.parannoying.core.interfaces.di.StorageInterface
import de.hartz.software.parannoying.core.model.persistence.realm.RealmHelper
import de.hartz.software.parannoying.offline.helper.ImportExportHelper
import de.hartz.software.parannoying.offline.helper.security.DialogCreationHelper
import de.hartz.software.parannoying.offline.model.OfflineStorage
import de.hartz.software.parannoying.offline.model.RealmOfflinePersistenceConfiguration
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.realm.Realm
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import javax.inject.Inject

@MediumTest
@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
class RealmExportImportTest {
    @Rule
    @JvmField var welcomeActivityRule = ActivityTestRule(WelcomeActivity::class.java)

    @Inject
    lateinit var securityInterfaceHolder: SecurityInterfaceHolder

    companion object {
        const val USER_PASSWORD = "password"
    }

    @Before
    fun setup() {
    }

    @After
    fun cleanup() {

    }

    @Test
    fun export_and_import_encrypted_realm_with_mocked_shared_preferences() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        Realm.init(context)

        val initialRealmPassword = ByteArray(64) { 1 }
        // val newRealmPassword = ByteArray(64) { 1 }

        val testComponent = DaggerTestComponent.create()
        testComponent.inject(this)

        // mockkStatic(InitializationHelper::class)
        // val mockPrefs = mockk<InitializationHelper>(relaxed = true)
        // every { mockPrefs.getRealmFileName(any()) } returns "realm-export-test.txt"

        InitializationHelper.setInitialized(context)
        InitializationHelper.setDeviceRole(context, "dummy-export-file.txt")
        val realmHelper = RealmHelper(securityInterfaceHolder, context, RealmOfflinePersistenceConfiguration())

        val storage = OfflineStorage(realmHelper)

        // TODO: Move string close to "app" extensions
        mockkStatic("de.hartz.software.parannoying.core.extensions.ContextExtensionKt")

        val mockkContext = spyk(context)
        val mockApp = mockk<AbstractApp>()
        every { mockkContext.app } returns mockApp
        every { mockApp.Storage } returns storage

        val onlineId = DataSecurityHelper.NOTIFICATION_ID_PREFIX_INVALID + "randomOnlineId"

        val dialog = DialogCreationHelper(mockkContext, securityInterfaceHolder)
            .createCurrentUserData("dummyUser", onlineId, mockkContext)
        // storage.addDialog(dialog)
        // storage.addDialog(CurrentUser("Test", "TestHash"))
        // TODO: Add filemessage to test if exporting files works properly


        val intent = Intent()
        // When crashing with timeout disable all animations in devices system setting
        // welcomeActivityRule.launchActivity(intent)

        val activity = welcomeActivityRule.activity

        // TODO: Test doesnt work on emulators... as file system function does not exist
        // TODO: But how do we create the files in e2e test then?
        // ImportExportHelper(securityInterfaceHolder, realmHelper)
        //     .startExport(activity, USER_PASSWORD, storage)

        StorageInterface.deleteAll(activity)

        // TODO: Use proper file, maybe testdatafile?
        // TODO: Read export folder
        val file = mockk<File>()

        ImportExportHelper(securityInterfaceHolder, realmHelper)
            .startImportExportedFile( file, USER_PASSWORD, activity)

        // TODO assert user exists


    }
}
