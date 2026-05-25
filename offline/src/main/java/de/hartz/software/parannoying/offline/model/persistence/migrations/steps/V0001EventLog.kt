package de.hartz.software.parannoying.offline.model.persistence.migrations.steps

import de.hartz.software.parannoying.core.model.persistence.realm.MigrationStep
import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmSchema


// https://www.mongodb.com/docs/realm/sdk/java/examples/modify-an-object-schema/#std-label-java-migration-function
class V0001EventLog() : MigrationStep {
    override val REALM_VERSION_AFTER_MIGRATION = 2L

    override fun migrate(realm: DynamicRealm, oldVersion: Long) {
        val schema: RealmSchema = realm.schema
        // https://stackoverflow.com/a/37656774/8524651
        val eventPersistenceSchema = schema.create("EventPersistence")
                .addField("persistenceId", Long::class.java, FieldAttribute.PRIMARY_KEY)
                .addField("eventType", String::class.java, FieldAttribute.REQUIRED)
                .addField("eventText", String::class.java, FieldAttribute.REQUIRED)
                .addField("createdAtTimestamp", Long::class.java)
                .addField("corruptedString", String::class.java, FieldAttribute.REQUIRED) // TODO: this should not be required??
                .addField("numberOfSyncedMessages", Int::class.java)
                .addRealmObjectField("relatedUser", schema.get("UserPersistence")!!)
                .addRealmObjectField("relatedMessage", schema.get("UserMessagePersistence")!!)

        schema.get("DeviceDataOfflinePersistence")!!
                .addRealmListField("eventStorage", eventPersistenceSchema)

    }

}