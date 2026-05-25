package de.hartz.software.parannoying.core.model.mapper

import io.realm.RealmList

// Map hashset to realmlist
class HashSetMapper {
    fun <T> asRealmList(hashSet: HashSet<T>): RealmList<T> {
        val realmList = RealmList<T>()
        realmList.addAll(hashSet)
        return realmList
    }

    fun <T> asHashMap(key: RealmList<T>): HashSet<T> {
        val hashSet = HashSet<T>()
        hashSet.addAll(key)
        return hashSet
    }
}