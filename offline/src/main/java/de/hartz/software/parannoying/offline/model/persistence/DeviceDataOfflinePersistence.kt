package de.hartz.software.parannoying.offline.model.persistence

import de.hartz.software.parannoying.core.model.domain.DeviceRole
import de.hartz.software.parannoying.core.model.persistence.realm.SingletonRealmObject
import de.hartz.software.parannoying.offline.model.persistence.settings.DeviceRolePersistence
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class DeviceDataOfflinePersistence: SingletonRealmObject, RealmObject()  {
    @PrimaryKey
    override var constantId: Long = 1

    var userId: String? = ""

    // Same for offline and online device
    // Realm needs default values for this.
    var onlineUserEmail: String? = ""
    var onlineUserIdPassword: String? = ""
    var onlineUserId: String? = ""

    var deviceRole: DeviceRolePersistence? = DeviceRolePersistence(DeviceRole.UNDEFINED)
}