package de.hartz.software.parannoying.offline.helper.guard.usb

import android.annotation.TargetApi
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.offline.helper.guard.ConnectionGuard


// https://stackoverflow.com/questions/48527171/detect-connectivity-change-in-android-7-and-above-when-app-is-killed-in-backgrou
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class UsbSchedulerService : JobService() {

    companion object {

        private val TAG = UsbSchedulerService::class.java.simpleName

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        fun scheduleJob(context: Context) {
            val myJob = JobInfo.Builder(0, ComponentName(context, UsbSchedulerService::class.java))
                    .setMinimumLatency(1000)
                    .setOverrideDeadline(2000)
                    // TODO: It is needed to instantly trigger on connection. But when the device is consuming more it WONT recognize at all!!!
                    .setRequiresCharging(true) // IMPORTANT: This only triggers when the device gets more percent: when consuming more power than getting it is not triggered!
                    .setPersisted(true)
                    .build()

            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler?
            jobScheduler!!.schedule(myJob)
        }

    }

    private var mConnectivityReceiver: UsbConnectivityReceiver? = null

    override fun onCreate() {
        super.onCreate()
        if (app.Storage.DEVELOPER_MODE) {
            UiHelper.showToastFromBackgroundTask(this, "Usb Service created")
        }
        ConnectionGuard.decideToKillApp(this)
        mConnectivityReceiver = UsbConnectivityReceiver()
    }


    /**
     * When the app's NetworkConnectionActivity is created, it starts this service. This is so that the
     * activity and this service can communicate back and forth. See "setUiCallback()"
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (app.Storage.DEVELOPER_MODE) {
            UiHelper.showToastFromBackgroundTask(this, "onStartJob" + mConnectivityReceiver!!)
        }
        Log.i(TAG, "onStartCommand")
        ConnectionGuard.decideToKillApp(this)
        return START_NOT_STICKY
    }

    override fun onStartJob(params: JobParameters): Boolean {
        if (app.Storage.DEVELOPER_MODE) {
            UiHelper.showToastFromBackgroundTask(this, "onStartJob" + mConnectivityReceiver!!)
        }
        Log.i(TAG, "onStartJob" + mConnectivityReceiver!!)
        val connectionChangedIntent = IntentFilter()
        connectionChangedIntent.addAction(Intent.ACTION_POWER_CONNECTED)
        connectionChangedIntent.addAction(Intent.ACTION_POWER_DISCONNECTED)
        connectionChangedIntent.addAction(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(mConnectivityReceiver, connectionChangedIntent)
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        if (app.Storage.DEVELOPER_MODE) {
            UiHelper.showToastFromBackgroundTask(this, "onStopJob")
        }
        Log.i(TAG, "onStopJob")
        // TODO: Does it make sense to unregister? As then nobody is watching for connection...
        unregisterReceiver(mConnectivityReceiver)
        // TODO: would be better to uncomment, but leading to crashes within e2e test io.realm.exceptions.RealmException: 'class de.hartz.software.parannoying.offline.model.persistence.EventPersistence' is not part of the schema for this Realm.
        // ConnectionGuard.decideToKillApp(this)
        return true
    }

}