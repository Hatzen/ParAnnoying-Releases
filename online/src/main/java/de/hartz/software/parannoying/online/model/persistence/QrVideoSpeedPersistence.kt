package de.hartz.software.parannoying.online.model.persistence

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class QrVideoSpeedPersistence(@PrimaryKey var speed: Int = -1 ) : RealmObject()
