package de.hartz.software.parannoying.core.interfaces.di

import de.hartz.software.parannoying.core.model.persistence.realm.BasicRealmMigration

// Needs to provide all RealmModel classes of the implementing module.
// Provides the migration for this realm models.
interface RealmEntityCollection {

    abstract val migration: BasicRealmMigration

}