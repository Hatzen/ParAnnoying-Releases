package de.hartz.software.parannoying.offline.model.domain.events

import de.hartz.software.parannoying.offline.model.domain.messages.AbstractMessage
import de.hartz.software.parannoying.offline.model.domain.messages.UserMessage

class MessageEvent(eventText: String, var relatedMessage: AbstractMessage): BaseEvent(eventText) {

}