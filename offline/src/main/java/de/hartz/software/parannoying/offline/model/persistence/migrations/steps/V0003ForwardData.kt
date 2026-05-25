package de.hartz.software.parannoying.offline.model.persistence.migrations.steps

import de.hartz.software.parannoying.core.model.persistence.realm.MigrationStep
import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmSchema


// https://www.mongodb.com/docs/realm/sdk/java/examples/modify-an-object-schema/#std-label-java-migration-function
class V0003ForwardData() : MigrationStep {
    override val REALM_VERSION_AFTER_MIGRATION = 4L

    override fun migrate(realm: DynamicRealm, oldVersion: Long) {
        val schema: RealmSchema = realm.schema
        val forwardPersistenceSchema = schema.create("ForwardDatasetPersistence")
                .addField("persistenceId", Long::class.java, FieldAttribute.PRIMARY_KEY)
                .addField("data", String::class.java, FieldAttribute.REQUIRED)
                .addField("note", String::class.java)// .setNullable("note", true)
                .addField("createdAtTimestamp", Long::class.java)
    }

}