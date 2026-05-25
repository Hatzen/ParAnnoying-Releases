package de.hartz.software.parannoying.core.model.mapper.helper

import org.mapstruct.BeforeMapping
import org.mapstruct.MappingTarget
import org.mapstruct.TargetType

class CycleAvoidingMappingContextFromPersistence {
    // Use hashmap as realm proxies the objects and the same objects dont have object identity
    private val knownInstances: HashMap<Any, Any> = HashMap<Any, Any>()

    @BeforeMapping
    fun <T> getMappedInstance(source: Any?, @TargetType targetType: Class<T>?): T? {
        return knownInstances[source] as T?
    }

    @BeforeMapping
    fun storeMappedInstance(source: Any, @MappingTarget target: Any) {
        // Avoid that empty lists get always returned as the exact same list as List<Integer>() == List<String>()
        // Same problem has to be managed for generic classes which dont have different members.
        if (source is Collection<*>) {
            if (source.isEmpty()) {
                return
            }
        }
        knownInstances[source] = target
    }
}