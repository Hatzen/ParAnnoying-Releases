package de.hartz.software.parannoying.offline.model.domain.messages

data class MessageTokenWrapper(val newToken: String,val previousToken: String,val tokenCheckSum: String )
