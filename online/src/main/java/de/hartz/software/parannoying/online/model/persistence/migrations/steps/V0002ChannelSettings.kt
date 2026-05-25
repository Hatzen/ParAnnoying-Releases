package de.hartz.software.parannoying.online.model.persistence.migrations.steps

import de.hartz.software.parannoying.core.model.domain.settings.QrVideoSpeed
import de.hartz.software.parannoying.core.model.domain.settings.SoundSpectrumAndSpeed
import de.hartz.software.parannoying.core.model.persistence.realm.MigrationStep
import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmObjectSchema
import io.realm.RealmSchema


// https://www.mongodb.com/docs/realm/sdk/java/examples/modify-an-object-schema/#std-label-java-migration-function
class V0002ChannelSettings() : MigrationStep {
    override val REALM_VERSION_AFTER_MIGRATION = 3L

    override fun migrate(realm: DynamicRealm, oldVersion: Long) {
        val schema: RealmSchema = realm.schema

        val qrVideoSpeedPersistence =  schema.create("QrVideoSpeedPersistence")!!
                .addField("speed", Int::class.java, FieldAttribute.PRIMARY_KEY)

        val soundSpeedPersistence =  schema.create("SoundSpectrumAndSpeedPersistence")!!
                .addField("speed", Int::class.java, FieldAttribute.PRIMARY_KEY)

        val forwardPersistenceSchema = schema.get("SettingsPersistence")!!
                .addField("useAutomaticConfirmation", Boolean::class.java)
                .setNullable("useAutomaticConfirmation", true)
                .addField("storedBluetoothMac", String::class.java)
                .addField("storeLastBluetoothMac", Boolean::class.java)
                .setNullable("storeLastBluetoothMac", true)
                .addField("choosenSdCard", String::class.java)
                .addRealmObjectField("videoSpeed", qrVideoSpeedPersistence)
                .addRealmObjectField("soundSpectrumAndSpeed", soundSpeedPersistence)

        forwardPersistenceSchema.transform(RealmObjectSchema.Function() {
            val NORMAL_VAL =it.dynamicRealm.createObject("QrVideoSpeedPersistence", QrVideoSpeed.NORMAL.speed)
            it.setObject("videoSpeed", NORMAL_VAL)
            val ULTRASOUND_FAST =it.dynamicRealm.createObject("SoundSpectrumAndSpeedPersistence", SoundSpectrumAndSpeed.ULTRASOUND_FAST.speed)
            it.setObject("soundSpectrumAndSpeed", ULTRASOUND_FAST)
        })


        schema.create("CrashLogPersistence")!!
                .addField("persistenceId", Long::class.java, FieldAttribute.PRIMARY_KEY)
                .addField("log", String::class.java, FieldAttribute.REQUIRED)
                .addField("createdAtTimestamp", Long::class.java)
    }

}