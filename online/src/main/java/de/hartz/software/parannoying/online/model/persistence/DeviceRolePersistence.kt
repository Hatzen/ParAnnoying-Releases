package de.hartz.software.parannoying.online.model.persistence

import de.hartz.software.parannoying.core.model.domain.DeviceRole
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

// TODO: Implement as interface and copy the exact same to all moduels OR just move realm to custom persistence project with all in the same modules
//  https://github.com/realm/realm-java/issues/6661
open class DeviceRolePersistence(@PrimaryKey var roleId: Long = DeviceRole.UNKNOWN): RealmObject()