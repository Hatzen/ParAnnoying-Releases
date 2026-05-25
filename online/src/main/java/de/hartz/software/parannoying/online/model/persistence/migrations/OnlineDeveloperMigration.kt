package de.hartz.software.parannoying.online.model.persistence.migrations

import de.hartz.software.parannoying.core.model.persistence.realm.BasicRealmMigration
import de.hartz.software.parannoying.core.model.persistence.realm.MigrationStep
import de.hartz.software.parannoying.online.model.persistence.migrations.steps.V0001HideIcon
import de.hartz.software.parannoying.online.model.persistence.migrations.steps.V0002ChannelSettings
import de.hartz.software.parannoying.online.model.persistence.migrations.steps.V0003ServerConfigSettings


// https://www.mongodb.com/docs/realm/sdk/java/examples/modify-an-object-schema/#std-label-java-migration-function
class OnlineDeveloperMigration : BasicRealmMigration {


    override val MIGRATIONS: List<MigrationStep>
        get() = listOf(
                V0001HideIcon(),
                V0002ChannelSettings(),
                V0003ServerConfigSettings()
        )

    // TODO: needs to be override for some reason.. https://stackoverflow.com/q/44923455/8524651#
    override fun hashCode(): Int {
        return 12;
    }
    override fun equals(o: Any?): Boolean {
        return o is OnlineDeveloperMigration
    }
}