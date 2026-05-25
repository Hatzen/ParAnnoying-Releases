package de.hartz.software.parannoying.offline.helper.guard.internet

import android.annotation.TargetApi
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.offline.helper.guard.ConnectionGuard
import de.hartz.software.parannoying.offline.model.OfflineStorage


// https://stackoverflow.com/questions/48527171/detect-connectivity-change-in-android-7-and-above-when-app-is-killed-in-backgrou
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class NetworkSchedulerService : JobService() {

    companion object {

        private val TAG = NetworkSchedulerService::class.java.simpleName

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        fun scheduleJob(context: Context) {
            val myJob = JobInfo.Builder(0, ComponentName(context, NetworkSchedulerService::class.java))
                    .setMinimumLatency(1000)
                    .setOverrideDeadline(2000)
                    // Also gets triggered by cellular connectivity.
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPersisted(true)
                    .build()

            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler?
            jobScheduler!!.schedule(myJob)
        }
    }

    private var mConnectivityReceiver: ConnectivityReceiver? = null

    override fun onCreate() {
        super.onCreate()
        if (OfflineStorage.INSTANCE.DEVELOPER_MODE) {
            UiHelper.showToastFromBackgroundTask(this, "Service created")
        }
        ConnectionGuard.decideToKillApp(this)
        Log.i(TAG, "Service created")
        mConnectivityReceiver = ConnectivityReceiver()
    }


    /**
     * When the app's NetworkConnectionActivity is created, it starts this service. This is so that the
     * activity and this service can communicate back and forth. See "setUiCallback()"
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (OfflineStorage.INSTANCE.DEVELOPER_MODE) {
            UiHelper.showToastFromBackgroundTask(this, "onStartJob" + mConnectivityReceiver!!)
        }
        Log.i(TAG, "onStartCommand")
        ConnectionGuard.decideToKillApp(this)
        return START_NOT_STICKY
    }


    override fun onStartJob(params: JobParameters): Boolean {
        if (OfflineStorage.INSTANCE.DEVELOPER_MODE) {
            UiHelper.showToastFromBackgroundTask(this, "onStartJob" + mConnectivityReceiver!!)
        }
        Log.i(TAG, "onStartJob" + mConnectivityReceiver!!)
        registerReceiver(mConnectivityReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        if (OfflineStorage.INSTANCE.DEVELOPER_MODE) {
            UiHelper.showToastFromBackgroundTask(this, "onStopJob")
        }
        Log.i(TAG, "onStopJob")
        // TODO: Does it make sense to unregister? As then nobody is watching for connection...
        unregisterReceiver(mConnectivityReceiver)
        ConnectionGuard.decideToKillApp(this)
        return true
    }

}