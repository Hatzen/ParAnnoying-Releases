package de.hartz.software.parannoying.offline.model.persistence.settings

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class QrVideoSpeedPersistence(@PrimaryKey var speed: Int = -1 ) : RealmObject()
