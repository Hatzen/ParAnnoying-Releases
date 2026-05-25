package de.hartz.software.parannoying.air.gap.helpers

import android.app.Activity
import android.content.Context
import android.util.Log
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.ggwave.SoundHandlerApi
import de.hartz.software.parannoying.ggwave.interfaces.ReceivedConfirmation
import de.hartz.software.parannoying.ggwave.interfaces.ReceivedMessageCallback
import de.hartz.software.parannoying.ggwave.interfaces.SendMessageCallback

class ConfirmationHelper(val securityInterfaceHolder: SecurityInterfaceHolder) {

    companion object {
        const val CONFIRMATION_TOKEN = "CONFIRMED"
        const val lengthOfConfirmation = 20
    }

    fun sendConfirmation(context: Context, lastSendMessage: String = "MyHash") {
        val hash = securityInterfaceHolder.hashHelper
            .hashWithSpecificLength(CONFIRMATION_TOKEN, lastSendMessage, length = lengthOfConfirmation)
        SoundHandlerApi.sendData(context, hash, object: SendMessageCallback {
            override fun playbackFinish() {
                // TODO: When finished switch to receive of confirmation of this message, then callback for going to next message or repeat..
            }
        })
    }

    fun receiveConfirmation(receivedData: String, callback: ReceivedConfirmation, activity:  Activity) {
        val hash = securityInterfaceHolder.hashHelper
            .hashWithSpecificLength(CONFIRMATION_TOKEN, receivedData, length = lengthOfConfirmation)

        SoundHandlerApi.receiveData(activity, object: ReceivedMessageCallback {
            override fun receivedMessage(hashedConfirmation: String) {
                Log.i(javaClass.simpleName, "received data via sound" + hashedConfirmation)
                if (hash == hashedConfirmation) {
                    callback.receivedValidConfirmation()
                } else {
                    callback.errorReceiving()
                    Log.w(javaClass.simpleName, "received incorrect data and restarting listening")
                    receiveConfirmation(receivedData, callback, activity)
                }
            }

            override fun process(array: ByteArray) {
                Log.i(javaClass.simpleName, "process data via sound " + array.size)
            }
        })
    }

}