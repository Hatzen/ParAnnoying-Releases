package de.hartz.software.parannoying.online.model.persistence

import de.hartz.software.parannoying.core.model.domain.DeviceRole
import de.hartz.software.parannoying.core.model.persistence.realm.SingletonRealmObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class DeviceDataOnlinePersistence: SingletonRealmObject, RealmObject()  {
    @PrimaryKey
    override var constantId: Long = 1


    // TODO: this should be placed within settings, shouldnt it?
    var useGoogleApi: Boolean = false

    // Same for offline and online device
    // Realm needs default values for this.
    var onlineUserEmail: String? = ""
    var onlineUserIdPassword: String? = ""
    var onlineUserId: String? = ""

    var deviceRole: DeviceRolePersistence? = DeviceRolePersistence(DeviceRole.UNDEFINED)
}