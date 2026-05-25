package de.hartz.software.parannoying.core.model.mapper

import de.hartz.software.parannoying.core.model.mapper.helper.CycleAvoidingMappingContext
import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject
import io.realm.RealmObject
import org.mapstruct.AfterMapping
import org.mapstruct.Context
import org.mapstruct.MappingTarget

class UniqueRealmObjectMapper {

    // TODO: https://github.com/mapstruct/mapstruct/issues/3036
    @AfterMapping
    fun <T> updateUniqueIdIfNeeded(source: Object, @MappingTarget target: Object, @Context realmHelper: CycleAvoidingMappingContext) {
        if (target is UniqueRealmObject && target is RealmObject) {
            updateUniqueIdIfNeededWhenClassMatches(source, target, realmHelper)
        }
    }

    fun <T> updateUniqueIdIfNeededWhenClassMatches(source: Object, @MappingTarget target: T, @Context  realmHelper: CycleAvoidingMappingContext) where T : UniqueRealmObject, T : RealmObject {
        if (target.persistenceId == UniqueRealmObject.ID_META_NEWEST_ID) {
            UniqueRealmObject.getNewestIdForEntity(target, realmHelper.realmHelper)
        }
    }

}
