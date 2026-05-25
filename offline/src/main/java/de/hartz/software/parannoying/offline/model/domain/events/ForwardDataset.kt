package de.hartz.software.parannoying.offline.model.domain.events

import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject

class ForwardDataset(var data: String, var note: String?) {

    var persistenceId: Long = UniqueRealmObject.ID_META_NEWEST_ID
    var createdAtTimestamp: Long = -1

    init {
        createdAtTimestamp = IOHelper.getCurrentDateAsUnixTimestamp()
    }
}