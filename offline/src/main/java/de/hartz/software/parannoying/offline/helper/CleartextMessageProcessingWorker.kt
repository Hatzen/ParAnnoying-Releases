package de.hartz.software.parannoying.offline.helper

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.helper.ui.NotificationHelper
import de.hartz.software.parannoying.core.interfaces.di.ApplicationInfoComponent
import de.hartz.software.parannoying.offline.businesslogic.CreateMessageProcessor
import de.hartz.software.parannoying.offline.businesslogic.CreateUserProcessor
import de.hartz.software.parannoying.offline.businesslogic.ReceiveMessageProcessor
import de.hartz.software.parannoying.offline.model.domain.dialogs.BaseDialog
import de.hartz.software.parannoying.offline.model.domain.messages.FileMessage
import de.hartz.software.parannoying.offline.model.domain.messages.UserMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

class CleartextMessageProcessingWorker
    (context: Context, params: WorkerParameters, val securityInterfaceHolder: SecurityInterfaceHolder)
    : CoroutineWorker(context, params) {

    enum class TaskType {
        CREATE_MESSAGE,
        CREATE_FILE_MESSAGE,
        CREATE_USER,
        RECEIVE_MESSAGE,
        RECEIVE_FILE
    }

    data class Task<T> (
        val data: String,
        val type: TaskType,
        val callback: (T) -> Unit = {},
        val user: BaseDialog? = null,
        val applicationInfo: ApplicationInfoComponent? = null,
    )

    companion object {
        private val queue = ConcurrentLinkedQueue<Task<*>>()
        private var processedCount = 0
        private const val CHANNEL_ID = "processing_channel"

        fun enqueueCreatedMessage(context: Context, item: String, user: BaseDialog, applicationInfo: ApplicationInfoComponent,  callback: (UserMessage) -> Unit) {
            queue.add(Task(item, TaskType.CREATE_MESSAGE, callback, user, applicationInfo))
            startWorker(context)
        }

        fun enqueueCreatedFileMessage(context: Context, fileName: String, user: BaseDialog, applicationInfo: ApplicationInfoComponent,  callback: (FileMessage) -> Unit) {
            queue.add(Task(fileName, TaskType.CREATE_FILE_MESSAGE, callback, user, applicationInfo))
            startWorker(context)
        }

        fun enqueueReceivedMessage(context: Context, item: String) {
            queue.add(Task<Unit>(item, TaskType.RECEIVE_MESSAGE))
            startWorker(context)
        }
        fun enqueueReceivedFile(context: Context, item: String) {
            queue.add(Task<Unit>(item, TaskType.RECEIVE_FILE))
            startWorker(context)
        }

        fun enqueueCreateUser(context: Context, item: String, callback: () -> Unit) {
            queue.add(Task<String>(item, TaskType.CREATE_USER, { callback.invoke() }))
            startWorker(context)
        }

        private fun startWorker(context: Context) {
            val workManager = WorkManager.getInstance(context)
            val request = OneTimeWorkRequestBuilder<CleartextMessageProcessingWorker>()
                .setInitialDelay(0, TimeUnit.SECONDS)
                .build()
            workManager.enqueueUniqueWork("CleartextMessageProcessingWorker", ExistingWorkPolicy.KEEP, request)
        }

    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        var scheduleTimes = 0
        updateNotification(applicationContext)
        try {
            do {
                scheduleTimes++
                val item = queue.poll() ?: continue
                processItem(item)
                processedCount++
                updateNotification(applicationContext)

                // TODO: Remove. Leads to Waiting 3 Seconds between messages, we wanted a "keep worker alive" but not mecessary
                // delay(TimeUnit.SECONDS.toMillis(3))
                // scheduleTimes < 20 &&
            } while (queue.isNotEmpty())
            finalizeProcessing()
        } catch (e: Exception) {
            // Forward crash to acra.
            val handler = Thread.getDefaultUncaughtExceptionHandler()
            handler?.uncaughtException(Thread.currentThread(), e)
            NotificationManagerCompat.from(applicationContext).cancel(NotificationHelper.WOKER_NOTIFICATION_ID)
            return@withContext Result.failure()
        }
        NotificationManagerCompat.from(applicationContext).cancel(NotificationHelper.WOKER_NOTIFICATION_ID)
        return@withContext Result.success()
    }

    private fun finalizeProcessing() {
        // Final cleanup logic before stopping completely
    }

    private fun processItem(item: Task<*>) {
        when (item.type) {
            TaskType.CREATE_MESSAGE -> CreateMessageProcessor(securityInterfaceHolder, applicationContext, item.applicationInfo!!)
                .queue(item.data, item.user!!, item.callback as (UserMessage) -> Unit)
            TaskType.CREATE_FILE_MESSAGE -> CreateMessageProcessor(securityInterfaceHolder, applicationContext, item.applicationInfo!!)
                .queueFileMessage(item.data, item.user!!, item.callback as (FileMessage) -> Unit)
            TaskType.CREATE_USER -> CreateUserProcessor(applicationContext, securityInterfaceHolder)
                .addUser(item.data, callbackWithParameterToSimpleCallback(item.callback))
            TaskType.RECEIVE_MESSAGE -> ReceiveMessageProcessor(applicationContext, securityInterfaceHolder)
                .addReceivedMessage(item.data)
            TaskType.RECEIVE_FILE -> ReceiveMessageProcessor(applicationContext, securityInterfaceHolder)
                .addReceivedFileMessage(item.data)
        }
    }

    // TODO: Simply no argument callback must be simpler/prettier possible.. Use dedicated attributes?
    // Hacky..
    private fun <T> callbackWithParameterToSimpleCallback (callback: (T) -> Unit): () -> Unit {
        return { (callback as (String) -> Unit).invoke("") }
    }


    // TODO: Maybe move to notificationhelper?
    private fun updateNotification(context: Context) {
        createNotificationChannel(context)
        val notificationManager = NotificationManagerCompat.from(context)
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Encrypting and Decrypting Messages")
            .setContentText("Queued: ${queue.size} | Processed: $processedCount")
            .setProgress(queue.size + processedCount, processedCount, false)

        notificationBuilder.setSmallIcon(de.hartz.software.parannoying.core.R.mipmap.ic_launcher_round)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setSmallIcon(de.hartz.software.parannoying.core.R.mipmap.ic_stat_notification)
            notificationBuilder.setColor(context.getResources().getColor(de.hartz.software.parannoying.core.R.color.colorWhite))
        } else {
            notificationBuilder.setSmallIcon(de.hartz.software.parannoying.core.R.mipmap.ic_stat_notification)
        }
        notificationBuilder.priority = NotificationManagerCompat.IMPORTANCE_HIGH
        notificationBuilder.setOngoing(true)
        notificationBuilder.setOnlyAlertOnce(true)

        val notification = notificationBuilder.build()
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(NotificationHelper.WOKER_NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Worker"
            val descriptionText = "Processes encryption for messages and users so process wont be killed"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
