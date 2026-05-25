package de.hartz.software.parannoying.online.model.persistence.migrations.steps

import de.hartz.software.parannoying.core.model.persistence.realm.MigrationStep
import io.realm.DynamicRealm
import io.realm.RealmSchema


// https://www.mongodb.com/docs/realm/sdk/java/examples/modify-an-object-schema/#std-label-java-migration-function
class V0001HideIcon() : MigrationStep {
    override val REALM_VERSION_AFTER_MIGRATION = 2L

    // TODO: Could be unified with V003 of offline migration somehow, but is it really useful?
    override fun migrate(realm: DynamicRealm, oldVersion: Long) {
        val schema: RealmSchema = realm.schema

        schema.get("SettingsPersistence")!!
                .addField("useFakeEntryActivity", Boolean::class.java).setNullable("useFakeEntryActivity", true)
    }

}