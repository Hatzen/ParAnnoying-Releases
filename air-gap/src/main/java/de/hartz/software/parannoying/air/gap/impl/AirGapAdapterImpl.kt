package de.hartz.software.parannoying.air.gap.impl

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import de.hartz.software.parannoying.air.gap.activities.ReceiveActivity
import de.hartz.software.parannoying.air.gap.activities.SendActivity
import de.hartz.software.parannoying.air.gap.fragments.exchange.AirGapFragment.Companion.EXTRA_PERSIST_LAUNCH_OPTIONS
import de.hartz.software.parannoying.air.gap.fragments.exchange.AirGapFragment.Companion.EXTRA_PURPOSE
import de.hartz.software.parannoying.air.gap.fragments.exchange.AirGapFragment.Companion.EXTRA_TARGET
import de.hartz.software.parannoying.air.gap.fragments.exchange.AirGapFragment.Companion.EXTRA_TEXT
import de.hartz.software.parannoying.air.gap.fragments.exchange.DataSendFragment.Companion.EXTRA_CHUNK_COUNT
import de.hartz.software.parannoying.air.gap.fragments.exchange.DataSendFragment.Companion.EXTRA_DATA_FILE
import de.hartz.software.parannoying.air.gap.fragments.exchange.DataSendFragment.Companion.EXTRA_USE_ADDITIONAL_ENCRYPTION
import de.hartz.software.parannoying.air.gap.fragments.exchange.DataSendFragment.Companion.EXTRA_YES_NO
import de.hartz.software.parannoying.air.gap.helpers.DatasetProcessor
import de.hartz.software.parannoying.air.gap.helpers.DatasetProcessor.Companion.RESULT_EXTRA_FILE_NAME
import de.hartz.software.parannoying.air.gap.helpers.channels.IntentHelper
import de.hartz.software.parannoying.air.gap.interfaces.di.ExchangeApp
import de.hartz.software.parannoying.air.gap.model.ExchangeResultImpl
import de.hartz.software.parannoying.air.gap.model.SendLaunchOptions
import de.hartz.software.parannoying.core.extensions.launchActivity
import de.hartz.software.parannoying.core.extensions.newIntent
import de.hartz.software.parannoying.core.interfaces.di.air.gap.AirGapAdapter
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ExchangeResult
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ILaunchOptions
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ISendLaunchOptions
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ReceiveLaunchOptions
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ResultCallback
import de.hartz.software.parannoying.core.interfaces.di.air.gap.SyncLaunchOptions

class AirGapAdapterImpl(val context: Context, val app: ExchangeApp): AirGapAdapter {


    override fun handleDataFromIntent(intent: Intent, callback: ResultCallback) {
        IntentHelper().handleDataFromIntent(intent, context, callback)
    }

    override fun startSync(launchOptions: SyncLaunchOptions) {
        if (launchOptions.receiveThenSend) {
            startReceive(launchOptions.receiveLaunchOptions, launchOptions.sendLaunchOptions)
        } else {
            startSend(launchOptions.sendLaunchOptions, launchOptions.receiveLaunchOptions)
        }
    }
    override fun startSend(launchOptions: ISendLaunchOptions) {
        startSend(launchOptions, null)
    }

    fun startSend(launchOptions: ISendLaunchOptions, persistLaunchOptions: ReceiveLaunchOptions?) {
        val intent = newIntent<SendActivity>(context)
        enrichIntent(launchOptions, intent)
        intent.putExtra(EXTRA_PERSIST_LAUNCH_OPTIONS , persistLaunchOptions)

        val currentActivity = app.currentActivity
        if (currentActivity != null) {
            currentActivity.launchActivity<SendActivity>(launchOptions.requestCode, null, intent)
        } else {
            // Needed for e.g. acra.
            context.launchActivity<SendActivity>(Bundle(), intent)
        }
    }

    override fun getSendIntent(launchOptions: ISendLaunchOptions): Intent {
        val it = Intent(context, SendActivity::class.java)
        enrichIntent(launchOptions, it)
        return it
    }

    override fun startReceive(launchOptions: ReceiveLaunchOptions) {
        startReceive(launchOptions, null)
    }

    private fun startReceive(launchOptions: ReceiveLaunchOptions, persistLaunchOptions: ISendLaunchOptions?) {
        val passParams : (userIntent: Intent) -> Unit = fun(it) {
            // TODO: Check title and be more expressive.
            it.putExtra(EXTRA_TEXT, launchOptions.text )
                    .putExtra(EXTRA_PURPOSE, launchOptions.purpose)
                    .putExtra(EXTRA_TARGET, launchOptions.source)
                    .putExtra(EXTRA_USE_ADDITIONAL_ENCRYPTION, launchOptions.additionalDecryption)
                    .putExtra(EXTRA_PERSIST_LAUNCH_OPTIONS , persistLaunchOptions)
        }

        val currentActivity = app.currentActivity
        currentActivity?.launchActivity<ReceiveActivity>(launchOptions.requestCode, Bundle(),  passParams)
    }


    // TODO: Pass callback to handle receive and start sync send afterwards..
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): ExchangeResult {

        val launchOptions =  data?.getSerializableExtra(EXTRA_PERSIST_LAUNCH_OPTIONS) as? ILaunchOptions
        if (launchOptions != null) {
            when (launchOptions) {
                is ReceiveLaunchOptions -> {
                    startReceive(launchOptions)
                }
                is SendLaunchOptions -> {
                    startSend(launchOptions)
                }
                else -> {
                    throw RuntimeException("weird launch options")
                }
            }
        }

        if (resultCode == Activity.RESULT_OK) {
            val filePath =  data?.getStringExtra(RESULT_EXTRA_FILE_NAME)
            if (filePath == null) {
                return ExchangeResultImpl(requestCode, null, true)
            }
            val result = DatasetProcessor().readFile(filePath)
            return ExchangeResultImpl(requestCode, result, true)
        }
        return ExchangeResultImpl(requestCode, null, false)
    }


    private fun enrichIntent(launchOptions: ISendLaunchOptions, it: Intent) {
        val processor = DatasetProcessor()
        val file = processor.writeFile(launchOptions.data)
        // TODO: Check title and be more expressive.
        it.putExtra(EXTRA_TEXT, launchOptions.text )
                .putExtra(EXTRA_YES_NO, launchOptions.confirmAndCancle)
                .putExtra(EXTRA_PURPOSE, launchOptions.purpose)
                .putExtra(EXTRA_TARGET, launchOptions.target)
                .putExtra(EXTRA_DATA_FILE, file.absolutePath)
                .putExtra(EXTRA_CHUNK_COUNT, processor.textChunkCount)
                .putExtra(EXTRA_USE_ADDITIONAL_ENCRYPTION, launchOptions.additionalEncryption)
    }
}