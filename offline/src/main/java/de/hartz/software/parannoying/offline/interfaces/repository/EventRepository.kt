package de.hartz.software.parannoying.offline.interfaces.repository

import de.hartz.software.parannoying.offline.model.domain.events.BaseEvent


interface EventRepository {

    fun readEvents(): List<BaseEvent>

    fun persistEvent(inboxEncryptedMessage: BaseEvent)

    fun deleteEvent(inboxEncryptedMessage: BaseEvent)

    fun deleteAllEvents()

}