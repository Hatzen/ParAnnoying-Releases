package de.hartz.software.parannoying.core.model.persistence.realm

import io.realm.RealmObject


/**
 *
 */
interface UniqueRealmObject {
    companion object {
        const val ID_META_NEWEST_ID: Long = -1
        const val ID_META_DO_NOT_PERSIST: Long = -2

        private const val ID_TO_START_WITH: Long = 1

        val idCache = HashMap<Class<*>, Long>()

        // add a method for bulk inserts so max is only required once?
        @Synchronized
        fun <T> getNewestIdForEntity(objectToUpdate: T, realmHelper: RealmHelper) where T : UniqueRealmObject, T : RealmObject {
            val targetClass = objectToUpdate.javaClass

            var maxId = idCache[targetClass]
            if (maxId == null) {
                val realm = realmHelper.getThreadInstance()
                val table = realm.schema.get(targetClass.simpleName)
                        ?: throw RuntimeException("Table not existing, migration seems to be missing for " + targetClass.simpleName)
                val primaryKeyFied: String = table.primaryKey
                maxId = realm.where(targetClass).max(primaryKeyFied)?.toLong()
                        ?: ID_TO_START_WITH
                realm.close()
            }
            objectToUpdate.persistenceId = maxId + 1
            idCache[targetClass] = objectToUpdate.persistenceId
        }
    }

    var persistenceId: Long

}