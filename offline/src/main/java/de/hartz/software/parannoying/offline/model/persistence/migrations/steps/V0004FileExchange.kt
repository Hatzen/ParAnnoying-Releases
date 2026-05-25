package de.hartz.software.parannoying.offline.model.persistence.migrations.steps

import de.hartz.software.parannoying.core.model.persistence.realm.MigrationStep
import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmSchema


// https://www.mongodb.com/docs/realm/sdk/java/examples/modify-an-object-schema/#std-label-java-migration-function
class V0004FileExchange() : MigrationStep {
    override val REALM_VERSION_AFTER_MIGRATION = 5L

    override fun migrate(realm: DynamicRealm, oldVersion: Long) {
        val schema: RealmSchema = realm.schema
        val forwardPersistenceSchema = schema.create("FileMetaDataPersistence")
                .addField("persistenceId", Long::class.java, FieldAttribute.PRIMARY_KEY)
                .addField("targetHash", String::class.java, FieldAttribute.REQUIRED)
                .addField("sourcehash", String::class.java, FieldAttribute.REQUIRED)
                .addField("fileName", String::class.java, FieldAttribute.REQUIRED)
                .addField("hmac", Int::class.java, FieldAttribute.REQUIRED)
                .addField("sendTimestamp", Long::class.java, FieldAttribute.REQUIRED)
                .addRealmObjectField("metaData", schema.get("MetaDataPersistence")!!)

        val messagePersistenceSchema = schema.rename("UserMessagePersistence", "MessagePersistence")!!
        messagePersistenceSchema
                .addRealmObjectField("fileMetaData", forwardPersistenceSchema)
                .addField("filePath", String::class.java)
                .addRealmObjectField("targetDialogUser", schema.get("UserPersistence")!!)
                .addRealmObjectField("targetDialogGroup", schema.get("OnlineGroupPersistence")!!)

        val deviceDataOfflinePersistenceSchema = schema.get("DeviceDataOfflinePersistence")!!
                .removeField("messageStorage")

        val onlineGroupPersistencePersistenceSchema = schema.get("OnlineGroupPersistence")!!
                .addRealmListField("incompleteMessages", messagePersistenceSchema)

        val userPersistenceSchema = schema.get("UserPersistence")!!
                .addRealmListField("incompleteMessages", messagePersistenceSchema)


        val SendMessagePersistenceSchema = schema.create("SendMessagePersistence")
                .addField("persistenceId", Long::class.java, FieldAttribute.PRIMARY_KEY)
                .addField("encryptedMessage", String::class.java)
                .addRealmObjectField("message", schema.get("MessagePersistence")!!)

    }

}