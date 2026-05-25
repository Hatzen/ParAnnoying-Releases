package de.hartz.software.parannoying.core.model.persistence.realm

import io.realm.DynamicRealm

interface MigrationStep {
    val REALM_VERSION_AFTER_MIGRATION: Long

    fun migrate(realm: DynamicRealm, oldVersion: Long)
}