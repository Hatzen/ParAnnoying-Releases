package de.hartz.software.parannoying.core.model.domain

import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject

data class CrashLog(
        val log: String,
        val createdAtTimestamp: Long,
        val persistenceId: Long = UniqueRealmObject.ID_META_NEWEST_ID)