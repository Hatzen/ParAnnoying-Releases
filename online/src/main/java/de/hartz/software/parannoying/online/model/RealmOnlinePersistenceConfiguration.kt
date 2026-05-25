package de.hartz.software.parannoying.online.model;

import de.hartz.software.parannoying.core.interfaces.di.RealmEntityCollection
import de.hartz.software.parannoying.online.model.persistence.ChannelsPersistence
import de.hartz.software.parannoying.online.model.persistence.CrashLogPersistence
import de.hartz.software.parannoying.online.model.persistence.DeviceDataOnlinePersistence
import de.hartz.software.parannoying.online.model.persistence.DeviceRolePersistence
import de.hartz.software.parannoying.online.model.persistence.InboxEncryptedMessagePersistence
import de.hartz.software.parannoying.online.model.persistence.LoggedEncryptedMessagePersistence
import de.hartz.software.parannoying.online.model.persistence.OnboardingStepsPersistence
import de.hartz.software.parannoying.online.model.persistence.OutboxEncryptedMessagePersistence
import de.hartz.software.parannoying.online.model.persistence.QrVideoSpeedPersistence
import de.hartz.software.parannoying.online.model.persistence.ServerConfigPersistence
import de.hartz.software.parannoying.online.model.persistence.ServerTypePersistence
import de.hartz.software.parannoying.online.model.persistence.SettingsPersistence
import de.hartz.software.parannoying.online.model.persistence.SoundSpectrumAndSpeedPersistence
import de.hartz.software.parannoying.online.model.persistence.migrations.OnlineDeveloperMigration
import io.realm.annotations.RealmModule

@RealmModule(library = true, classes = [
        DeviceDataOnlinePersistence::class,
        InboxEncryptedMessagePersistence::class,
        LoggedEncryptedMessagePersistence::class,
        OutboxEncryptedMessagePersistence::class,
        SettingsPersistence::class,
        ServerConfigPersistence::class,
        ServerTypePersistence::class,
        // Core.
        ChannelsPersistence::class,
        QrVideoSpeedPersistence::class,
        SoundSpectrumAndSpeedPersistence::class,
        DeviceRolePersistence::class,
        OnboardingStepsPersistence::class,
        CrashLogPersistence::class
])
class RealmOnlinePersistenceConfiguration: RealmEntityCollection {
        override val migration
                get() = OnlineDeveloperMigration()

}