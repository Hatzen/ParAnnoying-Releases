package de.hartz.software.parannoying.offline.model.persistence.message

import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class MetaDataPersistence : UniqueRealmObject, RealmObject() {
    @PrimaryKey
    override var persistenceId = UniqueRealmObject.ID_META_NEWEST_ID

    var newToken = ""
    var previousToken = ""
    var tokenCheckSum= ""
    var renewKey = ""
    var securityRisks: String = ""
    var appVersion: String = "-1"

}
