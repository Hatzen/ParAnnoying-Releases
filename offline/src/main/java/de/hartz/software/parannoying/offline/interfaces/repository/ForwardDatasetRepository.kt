package de.hartz.software.parannoying.offline.interfaces.repository

import de.hartz.software.parannoying.offline.model.domain.events.ForwardDataset


interface ForwardDatasetRepository {

    fun addForwardDatasets(forwardDataset: ForwardDataset)

    fun readForwardDatasets(): List<ForwardDataset>

    fun deleteForwardDataset(dataset: ForwardDataset)

    fun deleteAllForwardDataset()

}