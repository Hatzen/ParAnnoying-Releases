package de.hartz.software.parannoying.offline.model.domain.events

import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject

abstract class BaseEvent {

    companion object {
        val EVENT_WRONG_PIN = "Entered wrong PIN"
        val EVENT_MOVEMENT = "Device Moved"
        val EVENT_TIME_CHANGED = "System Time Changed"
        // Connection Log, only relevant for developer mode.
        val EVENT_INTERNET_CONNECTION = "Detected Internet Connection"
        val EVENT_PHONE_CONNECTION = "Detected Phone Connection"
        val EVENT_USB_CONNECTION = "Detected USB Connection"

        // User manipulated messages.
        val EVENT_SYNCED_MESSAGES = "Synced Messages"
        val EVENT_RECEIVED_MESSAGE = "Received a message"
        val EVENT_FAILED_RECEIVED_MESSAGE = "Failed to receive Message"
        val EVENT_SEND_MESSAGES = "Send Message"
        val EVENT_DELETED_MESSAGE = "Deleted a Message"
        val EVENT_IMPORT_MESSAGE = "Import Message"
        // User manipulated Users.
        val EVENT_DELETED_USER = "Deleted User"
        val EVENT_ADDED_USER = "Added User"
        val EVENT_RENAMED_USER = "Renamed User"
    }

    // TODO: Differentiate between type and text more..
    constructor(eventText: String) {
        this.eventType = eventText
        this.eventText = eventText
        createdAtTimestamp = IOHelper.getCurrentDateAsUnixTimestamp()
    }

    var persistenceId: Long = UniqueRealmObject.ID_META_NEWEST_ID
    lateinit var eventType: String
    lateinit var eventText: String
    var createdAtTimestamp: Long = -1
}