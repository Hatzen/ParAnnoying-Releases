package de.hartz.software.parannoying.offline.model.persistence.repository

import de.hartz.software.parannoying.core.model.persistence.realm.RealmHelper
import de.hartz.software.parannoying.offline.interfaces.repository.ForwardDatasetRepository
import de.hartz.software.parannoying.offline.model.domain.events.ForwardDataset
import de.hartz.software.parannoying.offline.model.mapper.DeviceDataMapper
import de.hartz.software.parannoying.offline.model.persistence.ForwardDatasetPersistence


class ForwardDatasetRepositoryImplRealm(val realmHelper: RealmHelper): ForwardDatasetRepository {

    override fun addForwardDatasets(forwardDataset: ForwardDataset) {
        val storeData = DeviceDataMapper(realmHelper).toPersistence(forwardDataset)
        val store = realmHelper.getThreadInstance()
        store.executeTransaction{
            it.copyToRealmOrUpdate(storeData)
        }
        store.close()
    }

    override fun readForwardDatasets(): List<ForwardDataset> {
        val store = realmHelper.getThreadInstance()
        val storeData = store.where(ForwardDatasetPersistence::class.java)
                .findAll()
        val result = store.copyFromRealm(storeData).map { DeviceDataMapper(realmHelper).toDomain(it) }.toList()
        store.close()
        return result
    }

    override fun deleteForwardDataset(dataset: ForwardDataset) {
        val storeData = DeviceDataMapper(realmHelper).toPersistence(dataset)
        val store = realmHelper.getThreadInstance()
        store.executeTransaction{
            val storeData = store.where(ForwardDatasetPersistence::class.java)
                .equalTo("persistenceId", storeData.persistenceId)
                .findAll()
            storeData.deleteAllFromRealm()
        }
        store.close()
    }

    override fun deleteAllForwardDataset() {
        val store = realmHelper.getThreadInstance()
        store.executeTransaction{
            it.delete(ForwardDatasetPersistence::class.java)
        }
        store.close()
    }

}