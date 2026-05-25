package de.hartz.software.parannoying.core.activities

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.ActionMode
import com.github.omadahealth.lollipin.lib.managers.AppLockActivity
import com.github.omadahealth.lollipin.lib.managers.LockManager
import de.hartz.software.parannoying.core.R
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.core.interfaces.di.StorageInterface


/**
 * Activity that shows lock screen.
 * (Not being extended for securing activity!)
 * https://github.com/omadahealth/LolliPin/blob/master/app/src/main/java/com/github/omadahealth/lollipin/CustomPinActivity.java
 *
 * Use appcompat delegate instead of making it appCompatActivity. Do we really need this?
 * https://stackoverflow.com/questions/44895978/appcompatactivity-vs-appcompatdelegate
 */
class CustomPinActivity : AppLockActivity(), AppCompatCallback {

    private lateinit var delegate: AppCompatDelegate

    override fun showForgotDialog() {
        DialogHelper.showAlert(this, "Data removed.")
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        delegate = AppCompatDelegate.create(this, this)
        delegate.onCreate(savedInstanceState)

        mPinCodeRoundView.setFullDotDrawable(de.hartz.software.parannoying.core.R.drawable.pin_code_round_filled)
        mPinCodeRoundView.refresh(mPinCode.length)

    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        mPinCodeRoundView.setFullDotDrawable(R.drawable.pin_code_round_filled)
        mPinCodeRoundView.refresh(mPinCode.length)
    }

    override fun onPinFailure(attempts: Int) {
        if (attempts > 3) {
            // TODO: Let user decide what to do.. Wait countdown, delete etc.
            StorageInterface.deleteAll(this)
            // TODO: Add event log also for online device..
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // If pin code setup make it cancelable.
        val lockManager = LockManager.getInstance()
        if (lockManager.isAppLockEnabled) {
            finish()
        }
    }

    override fun onPinSuccess(attempts: Int) {

    }

    override fun getPinLength(): Int {
        return 7
    }

    override fun onWindowStartingSupportActionMode(callback: ActionMode.Callback?): ActionMode? {
        return null
    }

    override fun onSupportActionModeStarted(mode: ActionMode) {
    }

    override fun onSupportActionModeFinished(mode: ActionMode) {
    }
}