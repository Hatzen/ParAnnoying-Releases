package de.hartz.software.parannoying.core.model.mapper.helper

import de.hartz.software.parannoying.core.model.persistence.realm.RealmHelper
import org.mapstruct.BeforeMapping
import org.mapstruct.MappingTarget
import org.mapstruct.TargetType
import java.util.*

/**
 * A type to be used as [Context] parameter to track cycles in graphs.
 *
 * Depending on the actual use case, the two methods below could also be changed to only accept certain argument types,
 * e.g. base classes of graph nodes, avoiding the need to capture any other objects that wouldn't necessarily result in
 * cycles.
 *
 * @author Andreas Gudian
 */
// TODO: rename or properly encapsulate
class CycleAvoidingMappingContext(val realmHelper: RealmHelper) {
    // Mapping from domain to persistence should be unique by object refernce.
    private val knownInstances: IdentityHashMap<Any, Any> = IdentityHashMap<Any, Any>()

    @BeforeMapping
    fun <T> getMappedInstance(source: Any?, @TargetType targetType: Class<T>): T? {
        return knownInstances[source] as T?
    }

    @BeforeMapping
    fun storeMappedInstance(source: Any, @MappingTarget target: Any) {
        knownInstances[source] = target
    }
}