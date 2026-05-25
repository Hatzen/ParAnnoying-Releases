package de.hartz.software.parannoying.offline.model.persistence

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class UserSecurityPersistence(@PrimaryKey var key: String = "-", var description: String = "invalid construction", var errorLevel: Int = 0): RealmObject()