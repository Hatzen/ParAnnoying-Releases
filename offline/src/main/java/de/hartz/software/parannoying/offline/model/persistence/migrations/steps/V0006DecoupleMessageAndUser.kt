package de.hartz.software.parannoying.offline.model.persistence.migrations.steps

import de.hartz.software.parannoying.core.model.persistence.realm.MigrationStep
import de.hartz.software.parannoying.core.model.persistence.realm.RealmHelper
import de.hartz.software.parannoying.offline.model.persistence.message.MessagePersistence
import io.realm.DynamicRealm
import io.realm.DynamicRealmObject
import io.realm.FieldAttribute
import io.realm.RealmSchema

private const val INCOMPLETE_MESSAGES = "incompleteMessages"
private const val PARTIAL_MESSAGE = "partialMessage"
private const val LAST_MESSAGE_TO_DISPLAY = "lastMessageToDisplay"
private const val MESSAGES = "messages"

// https://www.mongodb.com/docs/realm/sdk/java/examples/modify-an-object-schema/#std-label-java-migration-function
class V0006DecoupleMessageAndUser() : MigrationStep {
    override val REALM_VERSION_AFTER_MIGRATION = 7L

    override fun migrate(realm: DynamicRealm, oldVersion: Long) {
        val schema: RealmSchema = realm.schema


        val userSchema = schema.get("UserPersistence")!!
        val groupSchema = schema.get("OnlineGroupPersistence")!!
        val messageSchema = schema.get("MessagePersistence")!!

        messageSchema
            .addField(PARTIAL_MESSAGE, Boolean::class.java, FieldAttribute.REQUIRED)

        userSchema
            .addRealmObjectField(LAST_MESSAGE_TO_DISPLAY, messageSchema)
        val users = realm.where(userSchema.className).findAll()
        for (user in users) {
            clearMessages(user)
        }


        groupSchema
            .addRealmObjectField(LAST_MESSAGE_TO_DISPLAY, messageSchema)
        val groups = realm.where(groupSchema.className).findAll()
        for (group in groups) {
            clearMessages(group)
        }

        userSchema
            .removeField(MESSAGES)
            .removeField(INCOMPLETE_MESSAGES)

        groupSchema
            .removeField(MESSAGES)
            .removeField(INCOMPLETE_MESSAGES)
    }

    private fun clearMessages(userObj: DynamicRealmObject) {
        // 1. Mark messages in INCOMPLETE_MESSAGES
        val incompleteMessages = userObj.getList(INCOMPLETE_MESSAGES)
        incompleteMessages?.forEach { msg ->
            (msg as DynamicRealmObject).setBoolean(PARTIAL_MESSAGE, true)
        }

        // 2. Find newest message from MESSAGES
        val messages = userObj.getList(MESSAGES)
        var newestMessage: DynamicRealmObject? = null
        var latestTimestamp: Long = Long.MIN_VALUE

        messages?.forEach { msg ->
            val dynMsg = msg as DynamicRealmObject
            val ts = dynMsg.getLong(
                RealmHelper.toFieldSelector(MessagePersistence::createdAtTimestamp))
            if (ts > latestTimestamp) {
                latestTimestamp = ts
                newestMessage = dynMsg
            }
        }

        // Store newest in new field
        if (newestMessage != null) {
            userObj.setObject(LAST_MESSAGE_TO_DISPLAY, newestMessage)
        }
    }
}