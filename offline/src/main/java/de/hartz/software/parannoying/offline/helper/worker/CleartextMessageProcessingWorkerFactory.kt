package de.hartz.software.parannoying.offline.helper.worker

import android.content.Context
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.offline.helper.CleartextMessageProcessingWorker

class CleartextMessageProcessingWorkerFactory(val securityInterfaceHolder: SecurityInterfaceHolder)
    : WorkerFactory() {

    companion object {
        fun initializeWorkManager(context: Context, securityInterfaceHolder: SecurityInterfaceHolder) {
            if (WorkManager.isInitialized()) {
                return
            }
            val workerFactory = CleartextMessageProcessingWorkerFactory(securityInterfaceHolder)

            val config = Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()

            WorkManager.initialize(context, config)
        }
    }

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            CleartextMessageProcessingWorker::class.java.name ->
                CleartextMessageProcessingWorker(
                    appContext, workerParameters, securityInterfaceHolder)
            else -> null
        }
    }
}