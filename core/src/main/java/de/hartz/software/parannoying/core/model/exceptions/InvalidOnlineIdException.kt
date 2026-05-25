package de.hartz.software.parannoying.core.model.exceptions

class InvalidOnlineIdException : RuntimeException("Online user id seems to be invalid missing prefix or email content.") {
}