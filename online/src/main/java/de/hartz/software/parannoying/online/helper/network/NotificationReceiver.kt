package de.hartz.software.parannoying.online.helper.network

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import de.hartz.software.parannoying.core.activities.insecured.SplashActivity
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.online.R
import de.hartz.software.parannoying.online.adapters.FirebaseAdapter
import de.hartz.software.parannoying.online.model.OnlineStorage

//https://firebase.google.com/docs/cloud-messaging/android/receive#sample-receive

// https://gist.github.com/barbietunnie/190c5fe56f9430f7e6ab1ede19f41570
class NotificationReceiver : FirebaseMessagingService() {
    val onlineStorage: OnlineStorage = applicationContext.app.Storage as OnlineStorage

    init {
        INSTANCE = this
    }


    companion object {
        private lateinit var INSTANCE: NotificationReceiver

        fun updateNotificationId (newToken: String, context: Context) {
            val onlineUserEmail = INSTANCE.onlineStorage.onlineUserEmail
            val onlineUserIdPassword = INSTANCE.onlineStorage.onlineUserIdPassword
            val onlineUserId = INSTANCE.onlineStorage.onlineUserId

            val onlineDeviceIsInitialized = onlineUserEmail?.isNotBlank() == true
                    && onlineUserIdPassword?.isNotBlank() == true
                    && onlineUserId?.isNotBlank() == true

            if (!onlineDeviceIsInitialized) {
                INSTANCE.onlineStorage.tmpNotificationId = newToken
                return
            }

            val failureListener = OnFailureListener {
                Log.e("Registration Failed", it.localizedMessage)
                UiHelper.showToastFromBackgroundTask(context, "Error updating user token")
            }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(onlineUserEmail!!, onlineUserIdPassword!!).addOnSuccessListener {
                val map:  HashMap<String, String> = HashMap()
                map.put("notificationId", newToken)
                FirebaseAdapter.getUsersReference().child(onlineUserId!!).updateChildren(map as Map<String, Any>).addOnSuccessListener {
                    INSTANCE.onlineStorage.tmpNotificationId = null
                    UiHelper.showToastFromBackgroundTask(context, "Updating user token successful")
                }
                        .addOnFailureListener(failureListener)
            }.addOnFailureListener(failureListener)
        }
    }


    private val NOTIFICATION_CHANNEL = "PARANOIDCHANNEL"
    private val NOTIFICATION_MAX_CHARACTERS = 30
    private val LOG_TAG = NotificationReceiver::class.java.simpleName

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with FCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options\

        // The Squawk server always sends just *data* messages, meaning that onMessageReceived when
        // the app is both in the foreground AND the background

        Log.d(LOG_TAG, "From: " + remoteMessage!!.from!!)
        // Check if message contains a data payload.
        val data = remoteMessage.data
        if (data.isNotEmpty()) {
            Log.d(LOG_TAG, "Message data payload: $data")
            // Send a notification that you got a new message
            sendNotification(data)
        }
    }

    override fun onNewToken(newToken: String) {
        updateNotificationId(newToken, this)
    }

    /**
     * Create and show a simple notification containing the received FCM message
     *
     * @param data Map which has the message data in it
     */
    private fun sendNotification(data: Map<String, String>) {
        val intent = Intent(this, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        // Create the pending intent to launch the activity
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        //val author = data[JSON_KEY_AUTHOR]
        //var message = data[JSON_KEY_MESSAGE]
        var message = data.toString()

        // If the message is longer than the max number of characters we want in our
        // notification, truncate it and add the unicode character for ellipsis
        if (message!!.length > NOTIFICATION_MAX_CHARACTERS) {
            message = message.substring(0, NOTIFICATION_MAX_CHARACTERS) + "\u2026"
        }

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                .setSmallIcon(R.mipmap.ic_launcher)
                //.setContentTitle(String.format(getString(R.string.notification_message), author))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

}