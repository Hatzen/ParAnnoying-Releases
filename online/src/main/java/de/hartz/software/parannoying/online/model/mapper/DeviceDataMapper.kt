package de.hartz.software.parannoying.online.model.mapper;

import de.hartz.software.parannoying.core.model.domain.CrashLog
import de.hartz.software.parannoying.core.model.domain.DeviceRole
import de.hartz.software.parannoying.core.model.domain.OnboardingSteps
import de.hartz.software.parannoying.core.model.mapper.helper.CycleAvoidingMappingContext
import de.hartz.software.parannoying.core.model.mapper.helper.CycleAvoidingMappingContextFromPersistence
import de.hartz.software.parannoying.core.model.persistence.realm.RealmHelper
import de.hartz.software.parannoying.online.model.domain.InboxEncryptedMessage
import de.hartz.software.parannoying.online.model.domain.LoggedEncryptedMessage
import de.hartz.software.parannoying.online.model.domain.OutboxEncryptedMessage
import de.hartz.software.parannoying.online.model.domain.SettingsOnline
import de.hartz.software.parannoying.online.model.persistence.CrashLogPersistence
import de.hartz.software.parannoying.online.model.persistence.DeviceRolePersistence
import de.hartz.software.parannoying.online.model.persistence.InboxEncryptedMessagePersistence
import de.hartz.software.parannoying.online.model.persistence.LoggedEncryptedMessagePersistence
import de.hartz.software.parannoying.online.model.persistence.OnboardingStepsPersistence
import de.hartz.software.parannoying.online.model.persistence.OutboxEncryptedMessagePersistence
import de.hartz.software.parannoying.online.model.persistence.SettingsPersistence
import org.mapstruct.factory.Mappers

class DeviceDataMapper(private val realmHelper: RealmHelper) {

    fun toDomain(deviceDataPersistence: SettingsPersistence) : SettingsOnline{
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.toDomain(deviceDataPersistence, CycleAvoidingMappingContextFromPersistence())
    }

    fun toPersistence(storeData: SettingsOnline) : SettingsPersistence {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.toPersistence(storeData, CycleAvoidingMappingContext(realmHelper))
    }

    fun toDomain(deviceDataPersistence: InboxEncryptedMessagePersistence) : InboxEncryptedMessage {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.toDomain(deviceDataPersistence, CycleAvoidingMappingContextFromPersistence())
    }

    fun toPersistence(storeData: InboxEncryptedMessage) : InboxEncryptedMessagePersistence {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.toPersistence(storeData, CycleAvoidingMappingContext(realmHelper))
    }

    fun toDomain(deviceDataPersistence: OutboxEncryptedMessagePersistence) : OutboxEncryptedMessage {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)

        return converter.toDomain(deviceDataPersistence, CycleAvoidingMappingContextFromPersistence())
    }

    fun toPersistence(storeData: OutboxEncryptedMessage) : OutboxEncryptedMessagePersistence {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.toPersistence(storeData, CycleAvoidingMappingContext(realmHelper))
    }

       fun toDomain(deviceDataPersistence: LoggedEncryptedMessagePersistence) : LoggedEncryptedMessage {
         val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
         return converter.toDomain(deviceDataPersistence, CycleAvoidingMappingContextFromPersistence())
     }

    fun toPersistence(storeData: LoggedEncryptedMessage) : LoggedEncryptedMessagePersistence {
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

    fun toDomain(deviceDataPersistence: CrashLogPersistence) : CrashLog {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.toDomain(deviceDataPersistence, CycleAvoidingMappingContextFromPersistence())
    }

    fun toPersistence(storeData: CrashLog) : CrashLogPersistence {
        val converter = Mappers.getMapper(DeviceDataMappingInterface::class.java)
        return converter.toPersistence(storeData, CycleAvoidingMappingContext(realmHelper))
    }
}