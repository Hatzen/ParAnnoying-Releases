package de.hartz.software.parannoying.online.model.persistence

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class ServerTypePersistence(@PrimaryKey var type: Int = -1 ) : RealmObject()
