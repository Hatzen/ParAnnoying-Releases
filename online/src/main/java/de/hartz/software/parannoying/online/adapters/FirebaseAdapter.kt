package de.hartz.software.parannoying.online.adapters

import android.content.Context
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.ServerValue
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.online.helper.network.NotificationReceiver
import de.hartz.software.parannoying.online.model.OnlineStorage
import de.hartz.software.parannoying.online.model.domain.LoggedEncryptedMessage


class FirebaseAdapter(private val storage: OnlineStorage) {

    interface FirebaseCallback {

        fun onLoggedIn ()

        fun onError ()

        fun onDataReceived()
    }

    companion object {
        // TODO: get rid of statics..

        fun getUsersReference(): DatabaseReference {
            return database().reference.child("users")
        }


        private var configName: String? = null


        fun database(): FirebaseDatabase {
            return FirebaseDatabase.getInstance(app())
        }

        fun availability(): GoogleApiAvailability {
            return GoogleApiAvailability.getInstance()
        }

        fun messaging(): FirebaseMessaging {
            // https://stackoverflow.com/a/65863769/8524651
            // return FirebaseMessaging.getInstance(app())
            return app().get(FirebaseMessaging::class.java) as FirebaseMessaging
        }

        fun app(): FirebaseApp {
            val config = configName ?: return FirebaseApp.getInstance()
            return FirebaseApp.getInstance(config)
        }

        fun auth(): FirebaseAuth {
            return FirebaseAuth.getInstance(app())
        }


    }


    private var callback: FirebaseCallback? = null
    private var context: Context? = null



    private val onlineUserId: String
        get() = storage.onlineUserId!!

    private var connectionListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val connected = snapshot.getValue(Boolean::class.java)!!
            if (connected) {
                callback?.onLoggedIn()
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e(javaClass.simpleName,error.toException().localizedMessage)
            callback?.onError()
        }
    }

    private val messageListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            // This method is called once with the initial value and again
            // whenever data at this location is updated.

            val map = dataSnapshot.value as Map<String, Any>?
            // Receive messages and delete them after that.
            Log.e(javaClass.simpleName, "initOnDataChange")
            if (map != null) {
                getMessageReference(onlineUserId).runTransaction(object : Transaction.Handler {
                    override fun doTransaction(mutableData: MutableData): Transaction.Result {
                        Log.e(javaClass.simpleName, "doTransaction")
                        for (key in map.keys) {
                            val messageWithTimestamp = map[key] as Map<String, Any>
                            // TODO: Replace with DTO
                            val rawMessage = messageWithTimestamp["data"] as String
                            val sendAt = messageWithTimestamp["createdAt"] as Long
                            Log.v(javaClass.simpleName, "Received Message" + rawMessage)
                            storage.addInboxEncryptedMessage(rawMessage, sendAt)
                            mutableData.child(key).value = null
                        }
                        return Transaction.success(mutableData)
                    }

                    override fun onComplete(databaseError: DatabaseError?, b: Boolean, dataSnapshot: DataSnapshot?) {
                        // This ref might be deleted when testing in debug mode and test data is cleared online. Which throws error Permission Denied.
                        Log.d(javaClass.simpleName, "postTransaction:onComplete")
                        if (databaseError != null) {
                            Log.e(javaClass.simpleName, "postTransaction:onComplete:with error" + databaseError.details)
                        } else {
                            callback!!.onDataReceived()
                        }
                    }
                })
            }
        }

        override fun onCancelled(error: DatabaseError) {
            // Failed to read value. Probably not logged in yet? Or Permission denied for other reasons?
            /*
             Failed to read value.
            com.google.firebase.database.DatabaseException: Firebase Database error: This client does not have permission to perform this operation
                at com.google.firebase.database.DatabaseError.toException(DatabaseError.java:230)
                at de.hartz.software.parannoying.online.adapters.FirebaseAdapter$messageListener$1.onCancelled(FirebaseAdapter.kt:106)
                at com.google.firebase.database.core.ValueEventRegistration.fireCancelEvent(ValueEventRegistration.java:80)
                at com.google.firebase.database.core.view.CancelEvent.fire(CancelEvent.java:40)
                at com.google.firebase.database.core.view.EventRaiser$1.run(EventRaiser.java:55)
                at android.os.Handler.handleCallback(Handler.java:883)
                at android.os.Handler.dispatchMessage(Handler.java:100)
                at android.os.Looper.loop(Looper.java:214)
                at android.app.ActivityThread.main(ActivityThread.java:7356)
                at java.lang.reflect.Method.invoke(Native Method)
                at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:492)
                at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:930)

                Error message:
             */
            Log.w(javaClass.simpleName, "Failed to read value.", error.toException())
            Log.w(javaClass.simpleName, "Error message: " + error.details)
        }
    }

   // https://stackoverflow.com/a/62595363/8524651
    fun isGooglePlayServicesAvailable(context: Context): Boolean {
       /*
       https://github.com/Hatzen/ParAnnoying/issues/181
       // TODO: evaluate, may be misleading, when no internet connection we cannot setup google api. but maybe wanted?
       //   but freezes wont be...
       val connectedRef = database().getReference(".info/connected").get()

       val startTime = System.currentTimeMillis()
       while (!connectedRef.isComplete && System.currentTimeMillis() - startTime < 2000);

       return try {
           connectedRef.result.value == true
       } catch (e: Exception) {
            false
        }
          return try {
              val connection = URL("https://theUrl.com").openConnection() as HttpURLConnection
              connection.setRequestProperty("User-Agent", "Test")
              connection.connectTimeout = 10000
              connection.connect()
              connection.disconnect()
              true
          } catch (e: Exception) {
              false
          }
           return try {
               app() != null   //that is always true, nevertheless it will throw exception if Firebase is not available
           } catch (e: IllegalStateException) {
               false
           }
           */

        // TODO: But above check does fail on working devices...

        /*// TODO: On an emulator with some google apps, but not play store this is returning true but not able to register as an online device.
        */
        val googleApiAvailability = availability()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(context)
        if (status != ConnectionResult.SUCCESS) {
            return false
        }
        return true
    }

    // Only relevant for test. Maybe useful when disabling usage of google services.
    fun resetCurrentNotificationId() {
        messaging().deleteToken()
    }

    fun removeAllListeners() {

        if (storage.useGoogleApi) {
            val ref = getMessageReference(onlineUserId)
            ref.removeEventListener(messageListener)

            val connectedRef = database().getReference(".info/connected")
            connectedRef.removeEventListener(connectionListener)
        }
    }

    fun initializeCustomFirebaseApp(): FirebaseApp {
        val config = storage.readSettings().serverConfigs.firstOrNull()

        if (config == null) {
            configName = null
            FirebaseApp.initializeApp(context!!)
            return app()
        }
        configName = config.name

        val options = FirebaseOptions.Builder()
            .setApiKey(config.apiKey)
            .setApplicationId(config.appId)
            .setDatabaseUrl(config.databaseUrl)
            .setProjectId(config.projectId)
            .setGcmSenderId(config.gcmSenderId)
            .build()

        // Check if an app with this name already exists
        return try {
            FirebaseApp.getInstance(config.name)
        } catch (e: IllegalStateException) {
            FirebaseApp.initializeApp(context!!, options, config.name)
        }
    }

    fun signIn(context: Context, callback: FirebaseCallback) {
        this.callback = callback
        this.context = context
        if (storage.useGoogleApi) {
            initializeCustomFirebaseApp()

            // Sign in user.
            auth()
                    .signInWithEmailAndPassword(storage.onlineUserEmail!!, storage.onlineUserIdPassword!!)
                    .addOnSuccessListener {
                        initConnection()
                        if (storage.tmpNotificationId != null) {
                            NotificationReceiver.updateNotificationId(storage.tmpNotificationId!!, context)
                        }
                        callback.onLoggedIn()
                        updateLastSeen(context)
                    }.addOnFailureListener {
                        // TODO: Exception occours on emulator after updating version and
                        // com.google.firebase.FirebaseNetworkException: A network error (such as timeout, interrupted connection or unreachable host) has occurred.
                        // Before updating the error looked different but probably had the same cause.
                        // Probably the error occours when using a proxy or Hotspot?
                        // Because of switching wlan and emulator using proxy. Also it showed it finishes activity when no internet connection. See https://github.com/Hatzen/ParAnnoying/issues/150
                        Log.e(javaClass.simpleName, "Sign in failed: " + it.localizedMessage, it)

                        callback.onError()
                    }

            // Get connection interruption feedback. It does not seem to notice hotspot network changes (online => offline, but offline => online). But local network changes.
            // https://stackoverflow.com/a/50481219/8524651
            val connectedRef = database().getReference(".info/connected")
            connectedRef.addValueEventListener(connectionListener)
        }
    }

    // TODO: Replace with getUsersReference().child(Storage.onlineUserId!!).child("lastSeen").onDisconnect().setValue(ServerValue.TIMESTAMP)
    private fun updateLastSeen (context: Context) {
        val map:  HashMap<String, Any> = HashMap()
        map.put("lastSeen", ServerValue.TIMESTAMP)
        getUsersReference().child(storage.onlineUserId!!).updateChildren(map as Map<String, Any>).addOnSuccessListener {
            UiHelper.showToastFromBackgroundTask(context, "Updating user last seen.")
        }
        .addOnFailureListener {
            Log.e(javaClass.simpleName, it.localizedMessage)
            UiHelper.showToastFromBackgroundTask(context, "Error updating user last seen.")
        }
    }

    fun signOut () {
        database().purgeOutstandingWrites()
        auth().signOut()
        callback = null
        context = null
    }

    fun updatePassword (newPassword: String) {
        // val newPassword = DataGeneratorHelper.computeRandomHashWithSpecificLength(32)
        auth().currentUser!!.updatePassword(newPassword)

        storage.persistOnlineUserIdPassword(newPassword)
    }

    /**
     * Initalizes a cconnection to the firbase realtime database if the use was successfully signed in.
     * The connection to the realtime database will only happen within this app. New messages notifications are
     * implemented by using push notifications triggered by database changes.
     */
    private fun initConnection() {
        val ref = getMessageReference(onlineUserId)
        ref.addValueEventListener(messageListener)
    }

    interface MessageCallback {
        fun onMessageStateChanged()
    }

    fun isUserSignedIn(): Boolean {
        return auth().currentUser != null
    }

    fun sentMessage(onlineId: String, message: String, messageCallback: MessageCallback) {
        // If sign in takes to long just await before sending message.
        val isAlreadySignedIn = isUserSignedIn()
        if (isAlreadySignedIn) {
            Log.e(javaClass.simpleName, "Authentication needed again onAuthStateChanged" + auth().currentUser)
            forceSendMessage(onlineId, message, messageCallback)
        } else {
            val authStateListener = object: FirebaseAuth.AuthStateListener {
                override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
                    forceSendMessage(onlineId, message, messageCallback)
                    firebaseAuth.removeAuthStateListener(this)
                }
            }
            auth().addAuthStateListener(authStateListener)
        }
    }

    private fun forceSendMessage(onlineId: String, message: String, messageCallback: MessageCallback) {
        val ref = getMessageReference(onlineId).push()
        val map:  HashMap<String, Any> = HashMap()
        map.put("createdAt", ServerValue.TIMESTAMP)
        map.put("data", message)

        val onlineStorage = storage
        val settings = onlineStorage.readSettings()

        // TODO: Can this be useful to get updates of whether the message got already be sent? But might drain data usage.
        //  ref.keepSynced(true)
        ref.updateChildren(map as Map<String, Any>).addOnSuccessListener {
            Log.e(javaClass.simpleName, "message createdAt created.")
            val outboxEncryptedMessages = onlineStorage.readOutboxEncryptedMessages()
            val storedMessage = outboxEncryptedMessages.find { it.message == message }
            if (storedMessage == null) {
                // TODO: This probably happens only in e2e test as we pushed the reference already but this listener takes to long async..
                return@addOnSuccessListener
            }
            storage.deleteOutboxEncryptedMessages(storedMessage)

            // TODO: Check if targetUserHash is correct
            val loggedMessage = LoggedEncryptedMessage(message, IOHelper.getCurrentDateAsUnixTimestamp() * 1000, targetUserHash = onlineId)
            if (settings.logEncryptedMessages) {
                onlineStorage.persistLoggedEncryptedMessages(loggedMessage)
            }

            messageCallback.onMessageStateChanged() // Delivered
            UiHelper.showToastFromBackgroundTask(context!!, "Message successfully sent")
        }
        .addOnFailureListener {
            Log.e(javaClass.simpleName, "message createdAt Failed." + it.localizedMessage)
            Log.e(javaClass.simpleName, "Error sending message. For onlineId " + onlineId + " and message \n" + message, it)
            // TODO: Currently the rules also deny rewriting the exact same message. This should be displayed/logged somehow?
            UiHelper.showToastFromBackgroundTask(context!!, "Error sending message. Did you already sent this message?")
            messageCallback.onMessageStateChanged() // Error.
            // TODO: Flag message with error:
            // outboxEncryptedMessage.setError()
            // Storage.outboxEncryptedMessages.update(outboxEncryptedMessage)
        }
    }

    private fun getMessageReference(onlineUserId: String) : DatabaseReference  {
        return getUsersReference().child(onlineUserId).child("messages")
    }
}