package de.hartz.software.parannoying.online.helper

import android.os.Handler
import android.util.Log
import de.hartz.software.parannoying.core.helper.security.DataSecurityHelper
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.online.activities.online.OnlineMainActivity
import de.hartz.software.parannoying.online.adapters.FirebaseAdapter

class FirebaseAdapterCallbackImpl(val activity: OnlineMainActivity)
    : FirebaseAdapter.FirebaseCallback, FirebaseAdapter.MessageCallback {

    private var checkSignInHandler: Handler? = null

    fun cancleRetries() {
        checkSignInHandler?.removeCallbacksAndMessages(null)
        checkSignInHandler = null
    }

    override fun onMessageStateChanged() {
        activity.outboxFragment.updateOutbox()
    }

    override fun onDataReceived() {
        activity.inboxFragment.updateInbox()
    }

    override fun onLoggedIn() {
        val outboxEncryptedMessages = activity.onlineStorage.readOutboxEncryptedMessages()
        activity.setExchangeStatus(de.hartz.software.parannoying.air.gap.fragments.exchange.AirGapFragment.StatusColor.CONNECTED)
        Handler().postDelayed({
            outboxEncryptedMessages
                .filter { DataSecurityHelper(activity.securityInterfaceHolder).isOnlineIdValid(it.targetUserHash) }
                .forEach {
                    activity.sendOnlineMessage(it.targetUserHash, it.message, this)
                }
        }, 5) // TODO: Why do we have to wait here 5 sec?
        // TODO: Verify this does not produce any errors.
        // TODO: Updating password every time leads to: 2021-08-06 21:31:27.738 32028-32028/de.hartz.software.parannoying E/FirebaseAdapter: Sign in failed: We have blocked all requests from this device due to unusual activity. Try again later. [ Access to this account has been temporarily disabled due to many failed login attempts. You can immediately restore it by resetting your password or you can try again later. ]
        // onlineInterface.updatePassword()
    }

    override fun onError () {
        activity.setExchangeStatus(de.hartz.software.parannoying.air.gap.fragments.exchange.AirGapFragment.StatusColor.NO_MATCH)
        Log.e(javaClass.simpleName, "Could not connect to google api. Probably not internet connection? Try again later please.")
        UiHelper.showToastFromBackgroundTask(activity.applicationContext, "Could not connect to google api. Probably not internet connection? Try again later please.")

        val minute1 = 60000L
        checkSignInHandler?.removeCallbacksAndMessages(null)
        checkSignInHandler = Handler()
        checkSignInHandler!!.postDelayed({
            // If googleApi got just deactivated in settings it might lead to red bar.
            if (activity.onlineStorage.useGoogleApi) {
                activity.setExchangeStatus(de.hartz.software.parannoying.air.gap.fragments.exchange.AirGapFragment.StatusColor.CONNECTING)
                activity.onlineInterface?.signIn(activity, this)
                checkSignInHandler = null
            }
        }, minute1)
    }
}