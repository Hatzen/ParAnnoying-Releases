package de.hartz.software.parannoying.offline.model.persistence.settings

import de.hartz.software.parannoying.core.model.persistence.realm.SingletonRealmObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class HiddenSettingsPersistence : SingletonRealmObject, RealmObject() {
    @PrimaryKey
    override var constantId: Long = 1

    var developerMode: Boolean?
    var allowInternet: Boolean?
    var allowScreenshots: Boolean?

    // Boolean indicating if the hidden settings are accessible
    var developerOptionsActivated: Boolean?

    init {
        // TODO: Set to false.
        developerMode = true //BuildConfig.DEBUG
        allowInternet = true
        allowScreenshots = false
        developerOptionsActivated = false
    }
}