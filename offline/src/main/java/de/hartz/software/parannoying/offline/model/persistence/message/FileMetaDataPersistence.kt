package de.hartz.software.parannoying.offline.model.persistence.message

import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class FileMetaDataPersistence : UniqueRealmObject, RealmObject() {
    @PrimaryKey
    override var persistenceId = UniqueRealmObject.ID_META_NEWEST_ID

    var targetHash = ""
    var sourcehash = ""
    // TODO: Why is this an int?
    //  var hmac = -1
    var hmac: String? = ""
    lateinit var metaData: MetaDataPersistence
    var sendTimestamp = -1L
    var fileName = ""
}
