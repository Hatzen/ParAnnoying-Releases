package de.hartz.software.parannoying.air.gap.helpers.channels

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import de.hartz.software.parannoying.air.gap.activities.SharedDataRedirectActivity
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ResultCallback

class IntentHelper {

    fun handleDataFromIntent(intent: Intent, context: Context, callback: ResultCallback) {
        // TODO: Check if data is UserId or Message. By Appending a Checksum which crosssum is randomly 3. 6.
        // if (!intent.hasExtra(Intent.EXTRA_STREAM)) {
        //     throw RuntimeException("Unexpected intent cannot be handled")
        // }
        // TODO: Handle qrvideo and multiple files. Maybe handle sound import or file imports?
        val isImageType = intent.type?.startsWith(SharedDataRedirectActivity.INTENT_TYPE_IMAGE_PREFIX) ?: false
        if (intent.hasExtra(Intent.EXTRA_STREAM) && isImageType) {
            val receivedUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)!!
            try {
                // TODO: use finally block to close stream.
                val inputStream = context.contentResolver.openInputStream(receivedUri)
                val bmp = BitmapFactory.decodeStream(inputStream)
                inputStream!!.close()
                // TODO: Sometimes the result is not found, how to go on? Better finish activity to return to importing palce?
                val result = QrCodeHelper.scanQRImage(bmp)
                callback.onSuccess(result!!)
            } catch (e: Exception) {
                Log.e("", "Error Storing Shared Image: " + e.localizedMessage)
                callback.onError("Could not find qrcode", e)
            } finally {
                // Avoid scanning image again.
                intent.replaceExtras(Bundle())
                intent.removeExtra(Intent.EXTRA_STREAM)
            }
        }
    }

}