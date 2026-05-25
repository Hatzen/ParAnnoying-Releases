package de.hartz.software.parannoying.online.fragments.welcome

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ServerValue
import com.google.firebase.iid.FirebaseInstanceId
import com.mikepenz.iconics.view.IconicsImageView
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.fragments.welcome.WelcomeLoadingFragment
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.helper.security.DataGeneratorHelper
import de.hartz.software.parannoying.core.helper.security.DataSecurityHelper
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.online.R
import de.hartz.software.parannoying.online.adapters.FirebaseAdapter
import de.hartz.software.parannoying.online.interfaces.OnlineApplication
import de.hartz.software.parannoying.online.model.OnlineStorage
import java.util.UUID
import javax.inject.Inject

class GenerateOnlineIdFragment : WelcomeLoadingFragment() {
    private var fetchFinished = false
    var finishedRegisteringWithOnline: Boolean? = null
    var initStarted = false

    // Needed for tests as fragment might not be attached to activity.
    lateinit var mContext: Context
    lateinit var mActivity: Activity

    @Inject
    lateinit var securityInterfaceHolder: SecurityInterfaceHolder
    val storage get() = Storage as OnlineStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (app as OnlineApplication).onlineComponents.inject(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mContext = requireContext()
        mActivity = requireActivity()
        if (!storage.useGoogleApi) {
            // Creates a offline id directly.
            tryCreatingNotificationId(requireView() as ViewGroup)
        }
    }

    fun tryCreatingNotificationId (mainContent: ViewGroup) {
        val googleApiSelected = storage.useGoogleApi
        if (!FirebaseAdapter(storage).isGooglePlayServicesAvailable(mContext) || !googleApiSelected) {
            continueWithFakeId(mainContent)
            return
        }
        // Get firebase id for notifications.
        // https://stackoverflow.com/questions/37787373/firebase-fcm-how-to-get-token
        // https://stackoverflow.com/questions/37671380/what-is-fcm-token-in-firebase
        FirebaseInstanceId.getInstance().instanceId.
            addOnSuccessListener(mActivity) { instanceIdResult ->
                // Debugflag is used to identify users created by e2e tests etc. so they can get identified and removed.
                var debugFlag = ""
                if (storage.readSettings().hiddenSettings.developerMode) {
                    debugFlag = "_TEST_"
                }
                val onlineUserEmail = DataSecurityHelper.NOTIFICATION_ID_PREFIX_VALID + debugFlag + UUID.randomUUID().toString() + DataSecurityHelper.ONLINE_EMAIL_POSTFIX
                val onlineUserIdPassword = securityInterfaceHolder.randomHelper.computeRandomHashWithSpecificLength(32)
                val cleanOnlineUserId = DataGeneratorHelper(securityInterfaceHolder).cleanOnlineUserId(onlineUserEmail)
                storage.persistOnlineUserEmail(onlineUserEmail)
                storage.persistOnlineUserId(cleanOnlineUserId)
                storage.persistOnlineUserIdPassword(onlineUserIdPassword)

                val notificationId  = instanceIdResult.token
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(onlineUserEmail, onlineUserIdPassword)
                    .addOnSuccessListener {
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(onlineUserEmail, onlineUserIdPassword)
                            .addOnSuccessListener {
                                createUserAccount(cleanOnlineUserId, notificationId, mainContent)
                            }.addOnFailureListener{
                                // TODO: Error handling needs specific detection whether at least creation worked before, otherwise retrying wont help anything..
                                Log.e(javaClass.simpleName, "Error Signing into firebase" + it.localizedMessage, it)
                                UiHelper.showToastFromBackgroundTask(mContext, "Error with User registration")
                                failed(mainContent)
                            }
                    }.addOnFailureListener{
                        Log.e(javaClass.simpleName, "Error registering into firebase" + it.localizedMessage, it)
                        UiHelper.showToastFromBackgroundTask(mContext, "Error with User registration")
                        failed(mainContent)
                    }
            }.addOnFailureListener{
                // Happens e.g. when airplane mode is enabled.
                failed(mainContent)
            }

    }

    private fun failed(mainContent: ViewGroup) {
        if (context == null) {
            return
        }
        val context = requireContext()
        var hintInternetConnection = ""
        if (!IOHelper.hasInternetConnection(context)) {
            hintInternetConnection = " Please turn on your internet connection to create a full online device."
        }
        // TODO: E2E Test is failing on emulator. probably on all devices without playstore or internet?
        DialogHelper.showYesNoAlert(mContext, "Failed registering to google api!" + hintInternetConnection + " Try again? " +
                "\nBy clicking no it proceeds with fake id which makes you unable to use google realtime api and automatically receiving and sending messages.",
                object: DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            tryCreatingNotificationId(mainContent)
                        } else {
                            continueWithFakeId(mainContent)
                        }
                    }
                }
        )
    }

    public fun continueWithFakeId (mainContent: ViewGroup) {
        storage.setUseGoogleApi(false)
        storage.persistOnlineUserId(DataGeneratorHelper(securityInterfaceHolder).createFakeOnlineId())
        // TODO: is empty striing ok? Better set null like before?
        storage.persistOnlineUserEmail("")
        storage.persistOnlineUserIdPassword("")

        finishedRegisteringWithOnline = false
        finishedLoading(mainContent)
    }

    private fun createUserAccount(onlineUserId: String, notificationId: String, mainContent: ViewGroup) {
        val map:  HashMap<String, Any> = HashMap()
        map.put("notificationId", notificationId)
        map.put("createdAt", ServerValue.TIMESTAMP)
        FirebaseAdapter.getUsersReference().child(onlineUserId).updateChildren(map as Map<String, Any>).addOnSuccessListener {
            UiHelper.showToastFromBackgroundTask(mContext, "User registration successful")
            storage.setUseGoogleApi(true)
            finishedRegisteringWithOnline = true
            finishedLoading(mainContent)
        }
        .addOnFailureListener {
            // TODO: Why is this failing within e2e test with permission denied? probably same as commented in Friebaseadapter?
            Log.e("Registration Failed", it.localizedMessage)
            UiHelper.showToastFromBackgroundTask(mContext, "Error with User registration")
            tryCreatingNotificationId(mainContent)
        }
    }

    private fun finishedLoading (view: ViewGroup) {
        view.findViewById<View>(de.hartz.software.parannoying.online.R.id.loading_element)?.visibility = View.GONE
        fetchFinished = true
        view.findViewById<View>(de.hartz.software.parannoying.core.R.id.finished_wrapper)?.visibility = View.VISIBLE

        val finishView = view.findViewById<IconicsImageView>(de.hartz.software.parannoying.core.R.id.finished_checked)
        if (finishedRegisteringWithOnline!!) {
            val online = view.findViewById<View>(R.id.finished_online_element)
            online?.visibility = View.VISIBLE
            finishView?.setColorFilter(resources.getColor(R.color.colorPrimaryDark))
        } else {
            val offline = view.findViewById<View>(R.id.finished_offline_element)
            offline?.visibility = View.VISIBLE
            finishView?.setColorFilter(resources.getColor(R.color.colorAccent))
        }
    }

    override fun canMoveFurther() : Boolean {
        return fetchFinished
    }
}