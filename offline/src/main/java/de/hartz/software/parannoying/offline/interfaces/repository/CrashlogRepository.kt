package de.hartz.software.parannoying.offline.interfaces.repository

import de.hartz.software.parannoying.core.model.domain.CrashLog


interface CrashlogRepository {
    val crashLog: List<CrashLog>

    fun addCrashlog(forwardDataset: CrashLog)

    fun readCrashlogs(): List<CrashLog>

    fun deleteAllCrashlogs()

}