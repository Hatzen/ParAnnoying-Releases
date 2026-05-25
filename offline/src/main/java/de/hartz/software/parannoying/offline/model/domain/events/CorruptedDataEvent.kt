package de.hartz.software.parannoying.offline.model.domain.events

class CorruptedDataEvent(eventText: String, var corruptedString: String): BaseEvent(eventText) {

}