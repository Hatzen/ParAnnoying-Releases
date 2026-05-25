package de.hartz.software.parannoying.offline.model.mapper

import de.hartz.software.parannoying.core.model.domain.CrashLog
import de.hartz.software.parannoying.core.model.domain.DeviceRole
import de.hartz.software.parannoying.core.model.domain.OnboardingSteps
import de.hartz.software.parannoying.core.model.mapper.HashSetMapper
import de.hartz.software.parannoying.core.model.mapper.UniqueRealmObjectMapper
import de.hartz.software.parannoying.core.model.mapper.helper.CycleAvoidingMappingContext
import de.hartz.software.parannoying.core.model.mapper.helper.CycleAvoidingMappingContextFromPersistence
import de.hartz.software.parannoying.core.model.mapper.helper.DoIgnore
import de.hartz.software.parannoying.offline.model.domain.DecryptionKeyCloakForUser
import de.hartz.software.parannoying.offline.model.domain.SendMessage
import de.hartz.software.parannoying.offline.model.domain.dialogs.BaseDialog
import de.hartz.software.parannoying.offline.model.domain.dialogs.CurrentUser
import de.hartz.software.parannoying.offline.model.domain.dialogs.OfflineGroup
import de.hartz.software.parannoying.offline.model.domain.dialogs.OnlineGroup
import de.hartz.software.parannoying.offline.model.domain.dialogs.SimpleDialog
import de.hartz.software.parannoying.offline.model.domain.dialogs.UnknownUser
import de.hartz.software.parannoying.offline.model.domain.dialogs.User
import de.hartz.software.parannoying.offline.model.domain.events.BaseEvent
import de.hartz.software.parannoying.offline.model.domain.events.CorruptedDataEvent
import de.hartz.software.parannoying.offline.model.domain.events.ForwardDataset
import de.hartz.software.parannoying.offline.model.domain.events.MessageEvent
import de.hartz.software.parannoying.offline.model.domain.events.SimpleEvent
import de.hartz.software.parannoying.offline.model.domain.events.UserEvent
import de.hartz.software.parannoying.offline.model.domain.messages.AbstractMessage
import de.hartz.software.parannoying.offline.model.domain.messages.FileMessage
import de.hartz.software.parannoying.offline.model.domain.messages.UserMessage
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
import org.mapstruct.Context
import org.mapstruct.InheritInverseConfiguration
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.ObjectFactory
import org.mapstruct.ReportingPolicy
import org.mapstruct.SubclassMapping

@Mapper(uses = [
    PrivateKeyMapper::class, PublicKeyMapper::class,
    HashSetMapper::class,
    UniqueRealmObjectMapper::class
], unmappedTargetPolicy = ReportingPolicy.WARN)
abstract class DeviceDataMappingInterface {

    @Mappings()
    abstract fun toDomain(persistence: SettingsPersistence, @Context context: CycleAvoidingMappingContextFromPersistence): OfflineSettings
    @InheritInverseConfiguration
    abstract fun toPersistence(domain: OfflineSettings, @Context context: CycleAvoidingMappingContext): SettingsPersistence

    @Mappings
    abstract fun toDomain(persistence: DeviceRolePersistence, @Context context: CycleAvoidingMappingContextFromPersistence): DeviceRole
    @InheritInverseConfiguration
    abstract fun toPersistence(domain: DeviceRole, @Context context: CycleAvoidingMappingContext): DeviceRolePersistence


    @Mappings
    abstract fun toDomain(persistence: CrashLogPersistence, @Context context: CycleAvoidingMappingContextFromPersistence): CrashLog
    @InheritInverseConfiguration
    abstract fun toPersistence(domain: CrashLog, @Context context: CycleAvoidingMappingContext): CrashLogPersistence


    @Mappings
    abstract fun toDomain(persistence: OnboardingStepsPersistence, @Context context: CycleAvoidingMappingContextFromPersistence): OnboardingSteps
    @InheritInverseConfiguration
    abstract fun toPersistence(domain: OnboardingSteps, @Context context: CycleAvoidingMappingContext): OnboardingStepsPersistence

    @Mappings
    abstract fun toDomain(persistence: DecryptionKeyCloakForUserPersistence, @Context context: CycleAvoidingMappingContextFromPersistence): DecryptionKeyCloakForUser
    @InheritInverseConfiguration
    abstract fun toPersistence(domain: DecryptionKeyCloakForUser, @Context context: CycleAvoidingMappingContext): DecryptionKeyCloakForUserPersistence
/*
    // TODO: this seems to lead to incomplete mapping only super class attributes are mapped.. // TODO: remove or document in a better way.. general solution probably not possible..
    @Mappings
    abstract fun toDomain(persistence: UserPersistence, @Context context: CycleAvoidingMappingContextFromPersistence): BaseDialog
    @InheritInverseConfiguration
    abstract fun toPersistence(domain: BaseDialog, @Context context: CycleAvoidingMappingContext): UserPersistence
*/
    @Mappings
    abstract fun toDomain(persistence: ForwardDatasetPersistence, @Context context: CycleAvoidingMappingContextFromPersistence): ForwardDataset
    @InheritInverseConfiguration
    abstract fun toPersistence(domain: ForwardDataset, @Context context: CycleAvoidingMappingContext): ForwardDatasetPersistence

    @Mappings
    abstract fun toDomain(persistence: SendMessagePersistence, @Context context: CycleAvoidingMappingContextFromPersistence): SendMessage
    @InheritInverseConfiguration
    abstract fun toPersistence(domain: SendMessage, @Context context: CycleAvoidingMappingContext): SendMessagePersistence


    // TODO: saving OnlineGroupPersistence and UserPersistence leads to overlapping ids. They are not unique anymore..
    abstract fun toOnlineGroupPersistence(onlineGroup: OnlineGroup, @Context context: CycleAvoidingMappingContext) : OnlineGroupPersistence
    abstract fun toOnlineGroup(onlineGroupPersistence: OnlineGroupPersistence, @Context context: CycleAvoidingMappingContextFromPersistence) : OnlineGroup

    @SubclassMapping( source = OfflineGroup::class, target = UserPersistence::class )
    // TODO: Mapping for current user is not implemented when subclassmapping for USER AND CURRENTUSER IS SET for some reasonm only offlinegroup and user
    @SubclassMapping( source = CurrentUser::class, target = UserPersistence::class )
    @SubclassMapping( source = UnknownUser::class, target = UserPersistence::class )
    // @SubclassMapping( source = User::class, target = UserPersistence::class )
    abstract fun toUserPersistence(user: SimpleDialog, @Context context: CycleAvoidingMappingContext) : UserPersistence
    @DoIgnore
    abstract fun toUserPersistence(user: OfflineGroup, @Context context: CycleAvoidingMappingContext): UserPersistence
    @DoIgnore
    abstract fun toUserPersistence(user: CurrentUser, @Context context: CycleAvoidingMappingContext): UserPersistence
    @DoIgnore
    abstract fun toUserPersistence(user: User, @Context context: CycleAvoidingMappingContext): UserPersistence
    @DoIgnore
    abstract fun toUserPersistence(user: UnknownUser, @Context context: CycleAvoidingMappingContext): UserPersistence

    @ObjectFactory
    fun createDialog(user: UserPersistence, @Context context: CycleAvoidingMappingContextFromPersistence): SimpleDialog {
        if (user.groupId != null) {
            return createGroup(user, context)
        } else if (user.signKey != null) {
            return createCurrentUser(user, context)
        } else if (user.hash == user.originalName)  {
            return createUnknownUser(user, context)
        } else {
            return createUser(user, context)
        }
        throw NotImplementedError()
    }
    @DoIgnore
    abstract fun createGroup(user: UserPersistence, @Context context: CycleAvoidingMappingContextFromPersistence): OfflineGroup
    @DoIgnore
    abstract fun createUser(user: UserPersistence, @Context context: CycleAvoidingMappingContextFromPersistence): User
    @DoIgnore
    abstract fun createUnknownUser(user: UserPersistence, @Context context: CycleAvoidingMappingContextFromPersistence): UnknownUser
    @DoIgnore
    abstract fun createCurrentUser(user: UserPersistence, @Context context: CycleAvoidingMappingContextFromPersistence): CurrentUser

    @SubclassMapping( source = UserMessage::class, target = MessagePersistence::class )
    @SubclassMapping( source = FileMessage::class, target = MessagePersistence::class )
    abstract fun toMessagePersistence(user: AbstractMessage, @Context context: CycleAvoidingMappingContext) : MessagePersistence


    @Mapping(target = "targetDialogUser", expression = "java((!(user.getRelatedDialog() instanceof OnlineGroup)) ?  (toUserPersistence((SimpleDialog) user.getRelatedDialog(), context)) : null)")
    @Mapping(target = "targetDialogGroup", expression = "java((user.getRelatedDialog() instanceof OnlineGroup) ?  (toOnlineGroupPersistence((OnlineGroup) user.getRelatedDialog(), context)): null)")
    @DoIgnore
    abstract fun toMessagePersistence(user: UserMessage, @Context context: CycleAvoidingMappingContext): MessagePersistence

    @Mapping(target = "targetDialogUser", expression = "java((!(user.getRelatedDialog() instanceof OnlineGroup)) ? (toUserPersistence((SimpleDialog) user.getRelatedDialog(), context)) : null)")
    @Mapping(target = "targetDialogGroup", expression = "java((user.getRelatedDialog() instanceof OnlineGroup) ? (toOnlineGroupPersistence((OnlineGroup) user.getRelatedDialog(), context)): null)")
    @DoIgnore
    abstract fun toMessagePersistence(user: FileMessage, @Context context: CycleAvoidingMappingContext): MessagePersistence

    @ObjectFactory
    fun createMessage(messagePersistenceSource: MessagePersistence, @Context context: CycleAvoidingMappingContextFromPersistence): AbstractMessage {
        val target: AbstractMessage
        if (messagePersistenceSource.filePath != null) {
            target = createFileMessage(messagePersistenceSource, context)
        } else {
            target = createUserMessage(messagePersistenceSource, context)
        }

        val targetDialogUser = messagePersistenceSource.targetDialogUser
        if (targetDialogUser != null) {
            var cached = context.getMappedInstance(targetDialogUser, BaseDialog::class.java)
            if (cached == null) {
                cached = createDialog(targetDialogUser, context)
            }
            target.relatedDialog = cached
        }
        val targetDialogGroup = messagePersistenceSource.targetDialogGroup
        if (targetDialogGroup != null) {
            var cached = context.getMappedInstance(targetDialogUser, OnlineGroup::class.java)
            if (cached == null) {
                cached = toOnlineGroup(targetDialogGroup, context)
            }
            target.relatedDialog = cached
        }

        return target
    }

    // TODO: Multiple sources to target mapping not possible: https://github.com/mapstruct/mapstruct/issues/621
    //@Mapping(source = "targetDialogUser", target = "relatedDialog", conditionExpression = "java(targetDialogUser != null)")
    //@Mapping(source = "targetDialogGroup", target = "relatedDialog", conditionExpression = "java(targetDialogGroup != null)")
    @DoIgnore
    abstract fun createUserMessage(event: MessagePersistence, @Context context: CycleAvoidingMappingContextFromPersistence): UserMessage
    //@Mapping(source = "targetDialogUser", target = "relatedDialog", conditionExpression = "java(targetDialogUser != null)")
    // @Mapping(source = "targetDialogGroup", target = "relatedDialog", conditionExpression = "java(targetDialogGroup != null)")
    @DoIgnore
    abstract fun createFileMessage(event: MessagePersistence, @Context context: CycleAvoidingMappingContextFromPersistence): FileMessage



    @SubclassMapping( source = UserEvent::class, target = EventPersistence::class )
    @SubclassMapping( source = MessageEvent::class, target = EventPersistence::class )
    @SubclassMapping( source = CorruptedDataEvent::class, target = EventPersistence::class )
    @SubclassMapping( source = SimpleEvent::class, target = EventPersistence::class )
    abstract fun toEventPersistence(event: BaseEvent, @Context context: CycleAvoidingMappingContext) : EventPersistence
    @DoIgnore
    abstract fun toEventPersistence(event: UserEvent, @Context context: CycleAvoidingMappingContext): EventPersistence
    @DoIgnore
    abstract fun toEventPersistence(event: MessageEvent, @Context context: CycleAvoidingMappingContext): EventPersistence
    @DoIgnore
    abstract fun toEventPersistence(event: CorruptedDataEvent, @Context context: CycleAvoidingMappingContext): EventPersistence
    @DoIgnore
    abstract fun toEventPersistence(event: SimpleEvent, @Context context: CycleAvoidingMappingContext): EventPersistence

    @ObjectFactory
    fun createEvent(event: EventPersistence, @Context context: CycleAvoidingMappingContextFromPersistence): BaseEvent {
        if (arrayOf(BaseEvent.EVENT_ADDED_USER, BaseEvent.EVENT_DELETED_USER, BaseEvent.EVENT_RENAMED_USER).contains(event.eventType)) {
            return createUserEvent(event, context)
        } else if (arrayOf(BaseEvent.EVENT_DELETED_MESSAGE, BaseEvent.EVENT_RECEIVED_MESSAGE, BaseEvent.EVENT_SEND_MESSAGES, BaseEvent.EVENT_SYNCED_MESSAGES).contains(event.eventType)) {
            return createMessageEvent(event, context)
        } else if (arrayOf(BaseEvent.EVENT_FAILED_RECEIVED_MESSAGE).contains(event.eventType)) {
            // TODO: We need a failed user scanned event?
            return createCorruptedData(event, context)
        }
        // When internet connection etc. is mapped.
        return createSimpleEvent(event, context)
    }
    @DoIgnore
    abstract fun createUserEvent(event: EventPersistence, @Context context: CycleAvoidingMappingContextFromPersistence): UserEvent
    @DoIgnore
    abstract fun createMessageEvent(event: EventPersistence, @Context context: CycleAvoidingMappingContextFromPersistence): MessageEvent
    @DoIgnore
    abstract fun createCorruptedData(event: EventPersistence, @Context context: CycleAvoidingMappingContextFromPersistence): CorruptedDataEvent
    @DoIgnore
    abstract fun createSimpleEvent(event: EventPersistence, @Context context: CycleAvoidingMappingContextFromPersistence): SimpleEvent

}
