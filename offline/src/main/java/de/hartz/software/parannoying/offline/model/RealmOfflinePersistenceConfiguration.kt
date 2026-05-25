package de.hartz.software.parannoying.offline.model;

import de.hartz.software.parannoying.core.interfaces.di.RealmEntityCollection
import de.hartz.software.parannoying.offline.model.persistence.CrashLogPersistence
import de.hartz.software.parannoying.offline.model.persistence.DecryptionKeyCloakForUserPersistence
import de.hartz.software.parannoying.offline.model.persistence.DeviceDataOfflinePersistence
import de.hartz.software.parannoying.offline.model.persistence.EncryptionKeyCloakForUserPersistence
import de.hartz.software.parannoying.offline.model.persistence.EventPersistence
import de.hartz.software.parannoying.offline.model.persistence.ForwardDatasetPersistence
import de.hartz.software.parannoying.offline.model.persistence.OfflineKeyPairPersistence
import de.hartz.software.parannoying.offline.model.persistence.OnboardingStepsPersistence
import de.hartz.software.parannoying.offline.model.persistence.SendMessagePersistence
import de.hartz.software.parannoying.offline.model.persistence.UserSecurityPersistence
import de.hartz.software.parannoying.offline.model.persistence.dialogs.OnlineGroupPersistence
import de.hartz.software.parannoying.offline.model.persistence.dialogs.UserPersistence
import de.hartz.software.parannoying.offline.model.persistence.message.FileMetaDataPersistence
import de.hartz.software.parannoying.offline.model.persistence.message.MessagePersistence
import de.hartz.software.parannoying.offline.model.persistence.message.MetaDataPersistence
import de.hartz.software.parannoying.offline.model.persistence.migrations.OfflineDeveloperMigration
import de.hartz.software.parannoying.offline.model.persistence.settings.ChannelsPersistence
import de.hartz.software.parannoying.offline.model.persistence.settings.DeviceRolePersistence
import de.hartz.software.parannoying.offline.model.persistence.settings.HiddenSettingsPersistence
import de.hartz.software.parannoying.offline.model.persistence.settings.MessageSecurityPersistence
import de.hartz.software.parannoying.offline.model.persistence.settings.QrVideoSpeedPersistence
import de.hartz.software.parannoying.offline.model.persistence.settings.SettingsPersistence
import de.hartz.software.parannoying.offline.model.persistence.settings.SoundSpectrumAndSpeedPersistence
import io.realm.annotations.RealmModule

@RealmModule(library = true, classes = [
        DeviceDataOfflinePersistence::class,
        MessageSecurityPersistence::class,
        OnlineGroupPersistence::class,
        UserPersistence::class,
        DecryptionKeyCloakForUserPersistence::class,
        EncryptionKeyCloakForUserPersistence::class,
        HiddenSettingsPersistence::class,
        MetaDataPersistence::class,
        OfflineKeyPairPersistence::class,
        SettingsPersistence::class,
        MessagePersistence::class,
        UserSecurityPersistence::class,
        EventPersistence::class,
        ForwardDatasetPersistence::class,
        FileMetaDataPersistence::class,
        SendMessagePersistence::class,
        // Core.
        ChannelsPersistence::class,
        DeviceRolePersistence::class,
        QrVideoSpeedPersistence::class,
        SoundSpectrumAndSpeedPersistence::class,
        OnboardingStepsPersistence::class,
        CrashLogPersistence::class
])
class RealmOfflinePersistenceConfiguration: RealmEntityCollection {
        override val migration
                get() = OfflineDeveloperMigration()

}