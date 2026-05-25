package de.hartz.software.parannoying.online.model.mapper

import de.hartz.software.parannoying.core.model.domain.CrashLog
import de.hartz.software.parannoying.core.model.domain.DeviceRole
import de.hartz.software.parannoying.core.model.domain.OnboardingSteps
import de.hartz.software.parannoying.core.model.mapper.HashSetMapper
import de.hartz.software.parannoying.core.model.mapper.UniqueRealmObjectMapper
import de.hartz.software.parannoying.core.model.mapper.helper.CycleAvoidingMappingContext
import de.hartz.software.parannoying.core.model.mapper.helper.CycleAvoidingMappingContextFromPersistence
import de.hartz.software.parannoying.online.model.domain.InboxEncryptedMessage
import de.hartz.software.parannoying.online.model.domain.LoggedEncryptedMessage
import de.hartz.software.parannoying.online.model.domain.OutboxEncryptedMessage
import de.hartz.software.parannoying.online.model.domain.ServerType
import de.hartz.software.parannoying.online.model.domain.SettingsOnline
import de.hartz.software.parannoying.online.model.persistence.CrashLogPersistence
import de.hartz.software.parannoying.online.model.persistence.DeviceRolePersistence
import de.hartz.software.parannoying.online.model.persistence.InboxEncryptedMessagePersistence
import de.hartz.software.parannoying.online.model.persistence.LoggedEncryptedMessagePersistence
import de.hartz.software.parannoying.online.model.persistence.OnboardingStepsPersistence
import de.hartz.software.parannoying.online.model.persistence.OutboxEncryptedMessagePersistence
import de.hartz.software.parannoying.online.model.persistence.ServerTypePersistence
import de.hartz.software.parannoying.online.model.persistence.SettingsPersistence
import org.mapstruct.Context
import org.mapstruct.InheritInverseConfiguration
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.ReportingPolicy

@Mapper(uses = [HashSetMapper::class, UniqueRealmObjectMapper::class], unmappedTargetPolicy = ReportingPolicy.ERROR)
interface DeviceDataMappingInterface {

    @Mappings(Mapping(target = "hiddenSettings", ignore = true)) // TODO: Why ignore hiddenSettings?
    fun toDomain(persistence: SettingsPersistence, @Context context: CycleAvoidingMappingContextFromPersistence): SettingsOnline

    @Mapping(target = "constantId", ignore = true)
    @InheritInverseConfiguration
    fun toPersistence(domain: SettingsOnline, @Context context: CycleAvoidingMappingContext): SettingsPersistence

    @Mappings
    @Mapping(target = "persistenceId", ignore = true)  // TODO: Why ignore persistenceId?
    fun toDomain(persistence: DeviceRolePersistence, @Context context: CycleAvoidingMappingContextFromPersistence): DeviceRole
    @InheritInverseConfiguration
    fun toPersistence(domain: DeviceRole, @Context context: CycleAvoidingMappingContext): DeviceRolePersistence

    @Mappings
    fun toDomain(persistence: ServerTypePersistence, @Context context: CycleAvoidingMappingContextFromPersistence): ServerType
    @InheritInverseConfiguration
    fun toPersistence(domain: ServerType, @Context context: CycleAvoidingMappingContext): ServerTypePersistence


    @Mappings
    @Mapping(target = "copy", ignore = true) // TODO: No clue where this comes from..
    fun toDomain(persistence: OnboardingStepsPersistence, @Context context: CycleAvoidingMappingContextFromPersistence): OnboardingSteps
    @InheritInverseConfiguration
    fun toPersistence(domain: OnboardingSteps, @Context context: CycleAvoidingMappingContext): OnboardingStepsPersistence

    @Mappings
    fun toDomain(persistence: InboxEncryptedMessagePersistence, @Context context: CycleAvoidingMappingContextFromPersistence): InboxEncryptedMessage
    @InheritInverseConfiguration
    fun toPersistence(domain: InboxEncryptedMessage, @Context context: CycleAvoidingMappingContext): InboxEncryptedMessagePersistence

    @Mappings
    fun toDomain(persistence: OutboxEncryptedMessagePersistence, @Context context: CycleAvoidingMappingContextFromPersistence): OutboxEncryptedMessage
    @InheritInverseConfiguration
    fun toPersistence(domain: OutboxEncryptedMessage, @Context context: CycleAvoidingMappingContext): OutboxEncryptedMessagePersistence

    @Mappings
    fun toDomain(persistence: LoggedEncryptedMessagePersistence, @Context context: CycleAvoidingMappingContextFromPersistence): LoggedEncryptedMessage
    @InheritInverseConfiguration
    fun toPersistence(domain: LoggedEncryptedMessage, @Context context: CycleAvoidingMappingContext): LoggedEncryptedMessagePersistence

    @Mappings
    fun toDomain(persistence: CrashLogPersistence, @Context context: CycleAvoidingMappingContextFromPersistence): CrashLog
    @InheritInverseConfiguration
    fun toPersistence(domain: CrashLog, @Context context: CycleAvoidingMappingContext): CrashLogPersistence


}
