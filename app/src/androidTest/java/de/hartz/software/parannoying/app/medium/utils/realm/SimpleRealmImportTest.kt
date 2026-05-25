package de.hartz.software.parannoying.app.medium.utils.realm

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.MediumTest
import io.mockk.every
import io.mockk.mockk
import io.realm.Realm
import io.realm.RealmConfiguration
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@Ignore("Save performance as this test is kinda static and tests only realm on android..")
@MediumTest
@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
class SimpleRealmImportTest {

    private lateinit var realmFile: File
    private lateinit var realmPassword: ByteArray

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        Realm.init(context)

        realmPassword = ByteArray(64) { 1 }
        realmFile = File.createTempFile("test", ".realm").apply { delete() }

        val config = RealmConfiguration.Builder()
            .name(realmFile.name)
            .encryptionKey(realmPassword)
            .directory(realmFile.parentFile!!)
            .schemaVersion(1)
            .deleteRealmIfMigrationNeeded()
            .modules(TestModule())
            .build()

        Realm.getInstance(config).use { realm ->
            realm.executeTransaction {
                it.insert(
                    TestEntity(
                        "123",
                        "original"
                    )
                )
            }
            Log.d("Test", "Before export: ${realm.where(TestEntity::class.java).findAll()}")
        }
    }

    @After
    fun cleanup() {
        Realm.deleteRealm(
            RealmConfiguration.Builder()
            .name(realmFile.name)
            .directory(realmFile.parentFile!!)
            .build()
        )
        realmFile.delete()
    }

    @Test
    fun export_and_import_encrypted_realm_with_mocked_shared_preferences() {
        val backupFile = File.createTempFile("backup", ".realm")
        val prefs = mockk<android.content.SharedPreferences>()
        every { prefs.getString("realm_password", any()) } returns realmPassword.joinToString(",")

        realmFile.copyTo(backupFile, overwrite = true)

        Realm.deleteRealm(
            RealmConfiguration.Builder()
            .name(realmFile.name)
            .directory(realmFile.parentFile!!)
            .build()
        )

        backupFile.copyTo(realmFile, overwrite = true)

        val importedPassword = prefs.getString("realm_password", null)!!
            .split(",").map { it.toByte() }.toByteArray()

        val importedConfig = RealmConfiguration.Builder()
            .name(realmFile.name)
            .directory(realmFile.parentFile!!)
            .encryptionKey(importedPassword)
            .schemaVersion(1)
            .modules(TestModule())
            .build()

        Realm.getInstance(importedConfig).use { realm ->
            Log.d("Test", "After import: ${realm.where(TestEntity::class.java).findAll()}")
            val entity = realm.where(TestEntity::class.java).equalTo("id", "123").findFirst()
            // this fails as entity?.value is empty string because of realm proxy
            //  assertEquals("original", entity?.value)
            val result = realm.copyFromRealm(entity)
            assertEquals("original", result?.value)
        }
    }
}