package de.hartz.software.parannoying.core.model.persistence.realm

import android.util.Log
import io.realm.DynamicRealm
import io.realm.RealmMigration

interface BasicRealmMigration: RealmMigration {

    val NEWEST_REALM_VERSION get() = MIGRATIONS.maxOfOrNull { it.REALM_VERSION_AFTER_MIGRATION } ?: 1
    val MIGRATIONS: List<MigrationStep>

    // When leading to leading to Illegal Argument: Class already exists: 'EventPersistence'. or something similar then we need to reset data as the realm version is set with old and wrong version.
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        val allVersionsCount = MIGRATIONS.groupingBy { it.REALM_VERSION_AFTER_MIGRATION }.eachCount()
        val anyVersionDuplicated = allVersionsCount.any { it.value > 1 }
        assert(!anyVersionDuplicated)

        var currentVersion = oldVersion
        MIGRATIONS
                //  && it.REALM_VERSION_AFTER_MIGRATION < newVersion; only old version check needed as we always want newest.
                .filter { it.REALM_VERSION_AFTER_MIGRATION > oldVersion}
                .sortedBy { it.REALM_VERSION_AFTER_MIGRATION }
                .forEach {
                    Log.i(javaClass.simpleName, "Start Migratíon from $currentVersion to ${it.REALM_VERSION_AFTER_MIGRATION}")
                    it.migrate(realm, currentVersion)
                    Log.i(javaClass.simpleName, "Migrated successfully from $currentVersion to ${it.REALM_VERSION_AFTER_MIGRATION}")
                    currentVersion = it.REALM_VERSION_AFTER_MIGRATION
                }
    }
}