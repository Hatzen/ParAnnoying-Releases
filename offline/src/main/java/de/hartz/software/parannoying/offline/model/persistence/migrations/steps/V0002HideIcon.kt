package de.hartz.software.parannoying.offline.model.persistence.migrations.steps

import de.hartz.software.parannoying.core.model.persistence.realm.MigrationStep
import io.realm.DynamicRealm
import io.realm.RealmSchema


// https://www.mongodb.com/docs/realm/sdk/java/examples/modify-an-object-schema/#std-label-java-migration-function
class V0002HideIcon() : MigrationStep {
    override val REALM_VERSION_AFTER_MIGRATION = 3L

    override fun migrate(realm: DynamicRealm, oldVersion: Long) {
        val schema: RealmSchema = realm.schema

        schema.get("SettingsPersistence")!!
                .addField("useFakeEntryActivity", Boolean::class.java).setNullable("useFakeEntryActivity", true)
    }

}