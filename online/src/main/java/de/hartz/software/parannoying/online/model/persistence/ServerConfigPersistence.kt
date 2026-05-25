package de.hartz.software.parannoying.online.model.persistence

import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class ServerConfigPersistence : UniqueRealmObject, RealmObject() {
    @PrimaryKey
    override var persistenceId: Long = UniqueRealmObject.ID_META_NEWEST_ID

    var serverType: ServerTypePersistence? = ServerTypePersistence()

    var name: String = ""
    var apiKey: String = ""
    var appId: String = ""
    var databaseUrl: String = ""
    var projectId: String = ""
    var gcmSenderId: String = ""
}