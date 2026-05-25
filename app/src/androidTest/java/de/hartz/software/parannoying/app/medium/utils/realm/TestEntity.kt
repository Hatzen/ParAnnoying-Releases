package de.hartz.software.parannoying.app.medium.utils.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

// Beispiel-Entität
open class TestEntity(
    @PrimaryKey var id: String = "",
    var value: String = ""
) : RealmObject()