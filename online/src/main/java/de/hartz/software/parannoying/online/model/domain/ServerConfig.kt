package de.hartz.software.parannoying.online.model.domain

import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject

class ServerConfig {
    var persistenceId: Long = UniqueRealmObject.ID_META_NEWEST_ID

    lateinit var serverType: ServerType

    lateinit var name: String
    lateinit var apiKey: String
    lateinit var appId: String
    lateinit var databaseUrl: String
    lateinit var projectId: String
    // TODO: This is probably superflucid as of notificationId
    //  in onlineSettings but when supporting multiple servers its probably relevant
    lateinit var gcmSenderId: String
}