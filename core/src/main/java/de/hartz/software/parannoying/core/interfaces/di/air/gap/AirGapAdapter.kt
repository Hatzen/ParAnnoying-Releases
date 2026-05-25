package de.hartz.software.parannoying.core.interfaces.di.air.gap

import android.content.Intent

interface AirGapAdapter {
    fun handleDataFromIntent(intent: Intent, callback: ResultCallback)

    fun startSync(launchOptions: SyncLaunchOptions)

    fun startSend(launchOptions: ISendLaunchOptions)

    fun getSendIntent(launchOptions: ISendLaunchOptions): Intent

    fun startReceive(launchOptions: ReceiveLaunchOptions)

    // Receive: return stream of Data
    // Send: handle abort or success
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): ExchangeResult
}