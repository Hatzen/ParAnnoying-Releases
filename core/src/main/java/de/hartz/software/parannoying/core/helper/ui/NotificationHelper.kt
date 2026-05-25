package de.hartz.software.parannoying.core.helper.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat


object NotificationHelper {

    val PERSISTENT_NOTIFICATIONS = 12312
    val CRASH_REPORT_NOTIFICATIONS = 12445
    val DOWNLOAD_APK_NOTIFICATIONS = 12124
    val WOKER_NOTIFICATION_ID = 54124

    fun showPermanentNotification(context: Context, text: String, id: Int = PERSISTENT_NOTIFICATIONS, targetActivityClass: Class<*>) {
        val channelId = "PARANNOYING_WARNING"
        val notification = NotificationCompat.Builder(context, channelId)
        val intent = Intent(context, targetActivityClass)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        notification.setContentIntent(pendingIntent)
        notification.setOnlyAlertOnce(true)
        notification.setSmallIcon(de.hartz.software.parannoying.core.R.mipmap.ic_launcher_round)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification.setSmallIcon(de.hartz.software.parannoying.core.R.mipmap.ic_stat_notification)
            notification.setColor(context.getResources().getColor(de.hartz.software.parannoying.core.R.color.colorWhite))
        } else {
            notification.setSmallIcon(de.hartz.software.parannoying.core.R.mipmap.ic_stat_notification)
        }

        notification.setContentTitle("Warning:")
        notification.setContentText(text)
        notification.setPriority(Notification.PRIORITY_MAX)
        val notificationManager = (context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    channelId,
                    "Channel for notifications to show critical settings making app useless.",
                    NotificationManager.IMPORTANCE_MIN)
            notificationManager.createNotificationChannel(channel)
            notification.setChannelId(channelId)
        }

        // Avoid notifying user to shorten ui tests..
        notification.setSilent(true)
        val notificationResult = notification.build()
        notificationResult.flags = notificationResult.flags or
                (Notification.FLAG_NO_CLEAR or Notification.FLAG_ONGOING_EVENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationResult.flags = notificationResult.flags or
                NotificationManager.IMPORTANCE_LOW  or NotificationManager.IMPORTANCE_MIN
        }

        notificationManager.notify(id, notificationResult)
    }

    fun showCrashReportNotification(context: Context, intent: Intent) {
        val channelId = "PARANNOYING_CRASH"
        val notification = NotificationCompat.Builder(context, channelId)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        notification.setContentIntent(pendingIntent)
        notification.setSmallIcon(de.hartz.software.parannoying.core.R.mipmap.ic_launcher_round)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification.setSmallIcon(de.hartz.software.parannoying.core.R.mipmap.ic_stat_notification)
            notification.setColor(context.getResources().getColor(de.hartz.software.parannoying.core.R.color.colorWhite))
        } else {
            notification.setSmallIcon(de.hartz.software.parannoying.core.R.mipmap.ic_stat_notification)
        }

        notification.setContentTitle("ParAnnoying Crashed")
        notification.setContentText("Click notification to exchange the crash report with an online device.")
        notification.setPriority(NotificationCompat.PRIORITY_HIGH)
        val notificationManager = (context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    channelId,
                    "Channel for notifications to report app crashes on offline device.",
                    NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
            notification.setChannelId(channelId)
        }

        val notificationResult = notification.build()
        notificationResult.flags = notificationResult.flags or Notification.FLAG_AUTO_CANCEL or PendingIntent.FLAG_IMMUTABLE

        notificationManager.notify(CRASH_REPORT_NOTIFICATIONS, notificationResult)
    }

}