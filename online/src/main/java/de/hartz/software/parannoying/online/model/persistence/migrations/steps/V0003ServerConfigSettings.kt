package de.hartz.software.parannoying.online.model.persistence.migrations.steps

import de.hartz.software.parannoying.core.model.persistence.realm.MigrationStep
import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmSchema


// https://www.mongodb.com/docs/realm/sdk/java/examples/modify-an-object-schema/#std-label-java-migration-function
class V0003ServerConfigSettings() : MigrationStep {
    override val REALM_VERSION_AFTER_MIGRATION = 4L

    override fun migrate(realm: DynamicRealm, oldVersion: Long) {
        val schema: RealmSchema = realm.schema

        // TODO: Set proper fields..

        /*
         - Class 'ServerConfigPersistence' has been added.
        - Class 'ServerTypePersistence' has been added.
        - Property 'OutboxEncryptedMessagePersistence.sendAt' has been removed.

        - Property 'OutboxEncryptedMessagePersistence.isFileMessage' has been removed.
        - Property 'OutboxEncryptedMessagePersistence.isFile' has been added.
        - Property 'SettingsPersistence.useGoogleApi' has been removed.
        - Property 'SettingsPersistence.syncAllMessages' has been removed.
        - Property 'SettingsPersistence.videoSpeed' has been added.
        - Property 'SettingsPersistence.serverConfigs' has been added.
        - Property 'SettingsPersistence.reportErrorOnline' has been added.
         */

        schema.get("OutboxEncryptedMessagePersistence")!!
            .removeField("sendAt")
            .removeField("isFileMessage")
            .removeField("isFile")

        val serverTypePersistence =  schema.create("ServerTypePersistence")!!
                .addField("type", Int::class.java, FieldAttribute.PRIMARY_KEY)

        val serverConfigPersistence = schema.create("ServerConfigPersistence")!!
            .addField("name", String::class.java)
            .addField("apiKey", String::class.java)
            .addField("appId", String::class.java)
            .addField("databaseUrl", String::class.java)
            .addField("projectId", String::class.java)
            .addField("gcmSenderId", String::class.java)
            .addRealmObjectField("serverTypePersistence", serverTypePersistence)

        schema.get("SettingsPersistence")!!
            .removeField("syncAllMessages")

            .addRealmObjectField("videoSpeed", schema.get("QrVideoSpeedPersistence"))
            .addRealmListField("serverConfigs", serverConfigPersistence)
            .addField("reportErrorOnline", Boolean::class.java)

    }

}