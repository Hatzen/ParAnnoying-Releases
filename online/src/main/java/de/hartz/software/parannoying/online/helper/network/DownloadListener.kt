package de.hartz.software.parannoying.online.helper.network

import android.app.Activity
import android.app.DownloadManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.helper.ui.NotificationHelper.DOWNLOAD_APK_NOTIFICATIONS
import java.io.File


// https://stackoverflow.com/a/31275425/8524651
open class CustomDownloadListener(private val mContext: Activity) {
    private val mDownloadManager: DownloadManager
    private var mDownloadedFileID: Long = -1
    private var mRequest: DownloadManager.Request? = null

    init {
        mDownloadManager = mContext
                .getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    fun startDownload(url: String) {

        // Function is called once download completes.
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                // Prevents the occasional unintentional call. I needed this.
                if (mDownloadedFileID == -1L)
                    return
                val fileIntent = Intent(Intent.ACTION_VIEW)

                // Grabs the Uri for the file that was downloaded.
                val mostRecentDownload = mDownloadManager.getUriForDownloadedFile(mDownloadedFileID)
                showCrashReportNotification(mContext, File(mostRecentDownload.path))

                // Sets up the prevention of an unintentional call. I found it necessary. Maybe not for others.
                mDownloadedFileID = -1
            }
        }
        mRequest = DownloadManager.Request(Uri.parse(url))
        // Limits the download to only over WiFi. Optional.
        mRequest!!.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
        // Makes download visible in notifications while downloading, but disappears after download completes. Optional.
        mRequest!!.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        //mRequest!!.setMimeType(mimetype)

        // If necessary for a security check. it's not mandatory.
        // val cookie = CookieManager.getInstance().getCookie(url)
        // mRequest!!.addRequestHeader("Cookie", cookie)

        // Grabs the file name from the Content-Disposition
        val filename: String = "ParAnnoying.apk"

        // Sets the file path to save to, including the file name. Make sure to have the WRITE_EXTERNAL_STORAGE permission!!
        mRequest!!.setDestinationInExternalFilesDir(mContext, Environment.DIRECTORY_DOWNLOADS, filename)
        // Sets the title of the notification and how it appears to the user in the saved directory.
        mRequest!!.setTitle(filename)

        // Adds the request to the DownloadManager queue to be executed at the next available opportunity.
        mDownloadedFileID = mDownloadManager.enqueue(mRequest)

        // Registers function to listen to the completion of the download.
        ContextCompat.registerReceiver(
            mContext,
            onComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_EXPORTED
        )
    }


    fun showCrashReportNotification(context: Context, file: File) {
        val intent = IOHelper.initShareFile(file, mContext)
        val channelId = "PARANNOYING_DOWNLOAD_APK"
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

        notification.setContentTitle("Download finished")
        notification.setContentText("Click notification to share the downloaded apk file.")
        notification.setPriority(NotificationCompat.PRIORITY_HIGH)
        val notificationManager = (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    channelId,
                    "Channel for notifications to report app crashes on offline device.",
                    NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
            notification.setChannelId(channelId)
        }

        val notificationResult = notification.build()
        notificationResult.flags = notificationResult.flags or Notification.FLAG_AUTO_CANCEL

        notificationManager.notify(DOWNLOAD_APK_NOTIFICATIONS, notificationResult)
    }
}