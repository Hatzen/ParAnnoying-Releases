package de.hartz.software.parannoying.offline.model.exceptions

/**
 * Thrown when the message contains an other onlineId than got scanned by user id.
 */
class UserIsUsingUnknownIdException : RuntimeException("Could not verify User!") {
}