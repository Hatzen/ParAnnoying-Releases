package de.hartz.software.parannoying.app.medium

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import de.hartz.software.parannoying.app.utils.DaggerTestComponent
import de.hartz.software.parannoying.core.helper.InitializationHelper
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.model.persistence.realm.RealmHelper
import de.hartz.software.parannoying.offline.activities.offline.WelcomeOfflineActivity
import de.hartz.software.parannoying.offline.model.RealmOfflinePersistenceConfiguration
import io.realm.Realm
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@MediumTest
@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
class MigrationTest {
    @Rule
    @JvmField var welcomeActivityRule = ActivityTestRule(WelcomeOfflineActivity::class.java)

    @Inject
    lateinit var securityInterfaceHolder: SecurityInterfaceHolder

    @Test
    fun importOldDataToTestAllMigrationsWithoutDataMigration() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        Realm.init(context)

        val testComponent = DaggerTestComponent.create()
        testComponent.inject(this)

        InitializationHelper.setInitialized(context)
        InitializationHelper.setDeviceRole(context, "dummy-export-file.txt")
        val realmHelper = RealmHelper(securityInterfaceHolder, context, RealmOfflinePersistenceConfiguration())

        val activity = welcomeActivityRule.activity

        realmHelper.deleteRealmAndCloseCurrentSession()

        // TODO: Move to core test utils and store online file there as well, give proper Naming.
        activity.backupFragment.importDummyFile("Z")

        // Doesnt crash so works properly
    }
}
