package de.hartz.software.parannoying.core.model.persistence.realm

/**
 * An RealmObject which should be stored only once.
 * Needs to add an @PrimaryKey which is always the same.
 */
interface SingletonRealmObject {
    var constantId: Long
}