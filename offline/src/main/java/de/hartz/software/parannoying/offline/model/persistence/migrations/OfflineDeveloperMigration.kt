package de.hartz.software.parannoying.offline.model.persistence.migrations

import de.hartz.software.parannoying.core.model.persistence.realm.BasicRealmMigration
import de.hartz.software.parannoying.core.model.persistence.realm.MigrationStep
import de.hartz.software.parannoying.offline.model.persistence.migrations.steps.V0001EventLog
import de.hartz.software.parannoying.offline.model.persistence.migrations.steps.V0002HideIcon
import de.hartz.software.parannoying.offline.model.persistence.migrations.steps.V0003ForwardData
import de.hartz.software.parannoying.offline.model.persistence.migrations.steps.V0004FileExchange
import de.hartz.software.parannoying.offline.model.persistence.migrations.steps.V0005ChannelSettings
import de.hartz.software.parannoying.offline.model.persistence.migrations.steps.V0006DecoupleMessageAndUser
import io.realm.RealmMigration


// https://www.mongodb.com/docs/realm/sdk/java/examples/modify-an-object-schema/#std-label-java-migration-function
class OfflineDeveloperMigration : BasicRealmMigration {

    override val MIGRATIONS: List<MigrationStep>
        get() = listOf(
                V0001EventLog(),
                V0002HideIcon(),
                V0003ForwardData(),
                V0004FileExchange(),
                V0005ChannelSettings(),
                V0006DecoupleMessageAndUser()
        )

    // https://stackoverflow.com/a/36919305/8524651
    override fun hashCode(): Int {
        return 37
    }

    override fun equals(o: Any?): Boolean {
        return o is RealmMigration
    }
}