package de.hartz.software.parannoying.offline.model.persistence.repository

import de.hartz.software.parannoying.core.model.domain.CrashLog
import de.hartz.software.parannoying.core.model.persistence.realm.RealmHelper
import de.hartz.software.parannoying.offline.interfaces.repository.CrashlogRepository
import de.hartz.software.parannoying.offline.model.mapper.DeviceDataMapper
import de.hartz.software.parannoying.offline.model.persistence.CrashLogPersistence


class CrashlogRepositoryImplRealm(val realmHelper: RealmHelper): CrashlogRepository {
    override val crashLog: List<CrashLog>
        get() = readCrashlogs()

    override fun addCrashlog(forwardDataset: CrashLog) {
        val storeData = DeviceDataMapper(realmHelper).toPersistence(forwardDataset)
        val store = realmHelper.getThreadInstance()
        store.executeTransaction{
            it.copyToRealmOrUpdate(storeData)
        }
        store.close()
    }

    override fun readCrashlogs(): List<CrashLog> {
        val store = realmHelper.getThreadInstance()
        val storeData = store.where(CrashLogPersistence::class.java)
                .findAll()
        val result = store.copyFromRealm(storeData)
            .map { DeviceDataMapper(realmHelper).toDomain(it) }
            .toList()
        store.close()
        return result
    }

    override fun deleteAllCrashlogs() {
        val store = realmHelper.getThreadInstance()
        store.executeTransaction {
            it.delete(CrashLogPersistence::class.java)
        }
        store.close()
    }

}