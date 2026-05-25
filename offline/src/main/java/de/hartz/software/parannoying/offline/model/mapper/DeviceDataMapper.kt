package de.hartz.software.parannoying.offline.model.mapper;

import de.hartz.software.parannoying.core.model.domain.CrashLog
import de.hartz.software.parannoying.core.model.domain.DeviceRole
import de.hartz.software.parannoying.core.model.domain.OnboardingSteps
import de.hartz.software.parannoying.core.model.mapper.helper.CycleAvoidingMappingContext
import de.hartz.software.parannoying.core.model.mapper.helper.CycleAvoidingMappingContextFromPersistence
import de.hartz.software.parannoying.core.model.persistence.realm.RealmHelper
import de.hartz.software.parannoying.offline.model.domain.DecryptionKeyCloakForUser
import de.hartz.software.parannoying.offline.model.domain.SendMessage
import de.hartz.software.parannoying.offline.model.domain.dialogs.BaseDialog
import de.hartz.software.parannoying.offline.model.domain.dialogs.OnlineGroup
import de.hartz.software.parannoying.offline.model.domain.dialogs.SimpleDialog
import de.hartz.software.parannoying.offline.model.domain.events.BaseEvent
import de.hartz.software.parannoying.offline.model.domain.events.ForwardDataset
import de.hartz.software.parannoying.offline.model.domain.messages.AbstractMessage
import de.hartz.software.parannoying.offline.model.domain.settings.OfflineSettings
import de.hartz.software.parannoying.offline.model.persistence.CrashLogPersistence
import de.hartz.software.parannoying.offline.model.persistence.DecryptionKeyCloakForUserPersistence
import de.hartz.software.parannoying.offline.model.persistence.EventPersistence
import de.hartz.software.parannoying.offline.model.persistence.ForwardDatasetPersistence
import de.hartz.software.parannoying.offline.model.persistence.OnboardingStepsPersistence
import de.hartz.software.parannoying.offline.model.persistence.SendMessagePersistence
import de.hartz.software.parannoying.offline.model.persistence.dialogs.OnlineGroupPersistence
import de.hartz.software.parannoying.offline.model.persistence.dialogs.UserPersistence
import de.hartz.software.parannoying.offline.model.persistence.message.MessagePersistence
import de.hartz.software.parannoying.offline.model.persistence.settings.DeviceRolePersistence
import de.hartz.software.parannoying.offline.model.persistence.settings.SettingsPersistence
import org.mapstruct.factory.Mappers

class DeviceDataMapper(private val realmHelper: RealmHelper) {

    fun toDomain(deviceDataPersistence: SettingsPersistence) : OfflineSettings {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.toDomain(deviceDataPersistence, CycleAvoidingMappingContextFromPersistence())
    }

    fun toPersistence(storeData: OfflineSettings) : SettingsPersistence {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.toPersistence(storeData, CycleAvoidingMappingContext(realmHelper))
    }

    fun toDomain(deviceDataPersistence: DeviceRolePersistence) : DeviceRole {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.toDomain(deviceDataPersistence, CycleAvoidingMappingContextFromPersistence())
    }

    fun toPersistence(storeData: DeviceRole) : DeviceRolePersistence {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.toPersistence(storeData, CycleAvoidingMappingContext(realmHelper))
    }

    fun toDomain(deviceDataPersistence: OnboardingStepsPersistence) : OnboardingSteps {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.toDomain(deviceDataPersistence, CycleAvoidingMappingContextFromPersistence())
    }

    fun toPersistence(storeData: OnboardingSteps) : OnboardingStepsPersistence {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.toPersistence(storeData, CycleAvoidingMappingContext(realmHelper))
    }

    fun toDomain(deviceDataPersistence: SendMessagePersistence) : SendMessage {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.toDomain(deviceDataPersistence, CycleAvoidingMappingContextFromPersistence())
    }

    fun toPersistence(storeData: SendMessage) : SendMessagePersistence {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.toPersistence(storeData, CycleAvoidingMappingContext(realmHelper))
    }


    fun toDomain(deviceDataPersistence: DecryptionKeyCloakForUserPersistence) : DecryptionKeyCloakForUser {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.toDomain(deviceDataPersistence, CycleAvoidingMappingContextFromPersistence())
    }

    fun toPersistence(storeData: DecryptionKeyCloakForUser) : DecryptionKeyCloakForUserPersistence {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.toPersistence(storeData, CycleAvoidingMappingContext(realmHelper))
    }

    fun toDomain(deviceDataPersistence: ForwardDatasetPersistence) : ForwardDataset {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.toDomain(deviceDataPersistence, CycleAvoidingMappingContextFromPersistence())
    }

    fun toPersistence(storeData: ForwardDataset) : ForwardDatasetPersistence {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.toPersistence(storeData, CycleAvoidingMappingContext(realmHelper))
    }

    fun toDomain(deviceDataPersistence: EventPersistence) : BaseEvent {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.createEvent(deviceDataPersistence, CycleAvoidingMappingContextFromPersistence())
    }

    fun toPersistence(storeData: BaseEvent) : EventPersistence {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.toEventPersistence(storeData, CycleAvoidingMappingContext(realmHelper))
    }

    fun toDomain(deviceDataPersistence: CrashLogPersistence) : CrashLog {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.toDomain(deviceDataPersistence, CycleAvoidingMappingContextFromPersistence())
    }

    fun toPersistence(storeData: CrashLog) : CrashLogPersistence {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.toPersistence(storeData, CycleAvoidingMappingContext(realmHelper))
    }


    fun toDomain(deviceDataPersistence: UserPersistence) : BaseDialog {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.createDialog(deviceDataPersistence, CycleAvoidingMappingContextFromPersistence())
    }

    fun toPersistence(storeData: SimpleDialog) : UserPersistence {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.toUserPersistence(storeData, CycleAvoidingMappingContext(realmHelper))
    }


    fun toDomain(deviceDataPersistence: MessagePersistence) : AbstractMessage {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.createMessage(deviceDataPersistence, CycleAvoidingMappingContextFromPersistence())
    }

    fun toPersistence(storeData: AbstractMessage) : MessagePersistence {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.toMessagePersistence(storeData, CycleAvoidingMappingContext(realmHelper))
    }

    fun toDomain(deviceDataPersistence: OnlineGroupPersistence) : BaseDialog {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.toOnlineGroup(deviceDataPersistence, CycleAvoidingMappingContextFromPersistence())
    }

    fun toPersistence(storeData: OnlineGroup) : OnlineGroupPersistence {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.toOnlineGroupPersistence(storeData, CycleAvoidingMappingContext(realmHelper))
    }
}