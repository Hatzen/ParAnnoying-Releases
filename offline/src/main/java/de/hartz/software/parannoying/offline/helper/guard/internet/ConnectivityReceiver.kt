package de.hartz.software.parannoying.offline.helper.guard.internet

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.hartz.software.parannoying.offline.helper.guard.ConnectionGuard


class ConnectivityReceiver(): BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver") // We just check the connection state so it is ok, to be called more often.
    override fun onReceive(context: Context, intent: Intent) {
        ConnectionGuard.decideToKillApp(context)
    }

}