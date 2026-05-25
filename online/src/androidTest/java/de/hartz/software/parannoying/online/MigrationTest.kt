package de.hartz.software.parannoying.online

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import de.hartz.software.parannoying.core.helper.InitializationHelper
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.model.persistence.realm.RealmHelper
import de.hartz.software.parannoying.online.activities.online.WelcomeOnlineActivity
import de.hartz.software.parannoying.online.model.RealmOnlinePersistenceConfiguration
import io.realm.Realm
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@MediumTest
@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
class MigrationTest {
    @Rule
    @JvmField var welcomeActivityRule = ActivityTestRule(WelcomeOnlineActivity::class.java)

    @Inject
    lateinit var securityInterfaceHolder: SecurityInterfaceHolder

    @Test
    fun export_and_import_encrypted_realm_with_mocked_shared_preferences() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        Realm.init(context)

        // TODO Create file to import
        // val testComponent = DaggerTestComponent.create()
        // testComponent.inject(this)

        InitializationHelper.setInitialized(context)
        InitializationHelper.setDeviceRole(context, "dummy-export-file.txt")
        val realmHelper = RealmHelper(securityInterfaceHolder, context, RealmOnlinePersistenceConfiguration())

        val activity = welcomeActivityRule.activity

        realmHelper.deleteRealmAndCloseCurrentSession()

        // activity.backupFragment.importDummyFile("A")

        // Doesnt crash so works properly
    }
}
