package de.hartz.software.parannoying.online.model.persistence

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class ChannelsPersistence(@PrimaryKey var channel: Int = -1 ) : RealmObject()
