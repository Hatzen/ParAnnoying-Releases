package de.hartz.software.parannoying.air.gap.fragments.exchange

import android.os.Bundle
import android.util.Log
import de.hartz.software.parannoying.air.gap.fragments.exchange.receive.BluetoothReceiveChannelFragment
import de.hartz.software.parannoying.air.gap.fragments.exchange.receive.CameraReceiveChannelFragment
import de.hartz.software.parannoying.air.gap.fragments.exchange.receive.NFCReceiveChannelFragment
import de.hartz.software.parannoying.air.gap.fragments.exchange.receive.SDCardReceiveChannelFragment
import de.hartz.software.parannoying.air.gap.fragments.exchange.receive.SoundReceiveChannelFragment
import de.hartz.software.parannoying.air.gap.fragments.exchange.receive.TextReceiveChannelFragment
import de.hartz.software.parannoying.air.gap.fragments.exchange.receive.VideoReceiveChannelFragment
import de.hartz.software.parannoying.air.gap.helpers.DatasetProcessor
import de.hartz.software.parannoying.air.gap.helpers.ExchangeHelper
import de.hartz.software.parannoying.air.gap.interfaces.di.ExchangeApp
import de.hartz.software.parannoying.air.gap.interfaces.exchange.ReceiveFragmentResultListener
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.core.model.domain.settings.Channels
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class DataReceiveFragment: AirGapFragment(), ReceiveFragmentResultListener {

    @Inject
    lateinit var securityInterfaceHolder: SecurityInterfaceHolder

    val file = File.createTempFile("activity-result-file", "txt")
    var fos: FileOutputStream? = null
    private var password: String? = null
    private var preambleProcessed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (app as ExchangeApp).getActivityComponents(requireActivity()).inject(this)
    }

    override fun onResume() {
        super.onResume()
        fos = FileOutputStream(file)
        file.deleteOnExit()
    }

    override fun onPause() {
        super.onPause()
        fos?.close()
        fos = null
    }

    override fun currentFragmentSupportsYesNo (fragment: AbstractExchangeChannelFragment): Boolean {
        val buttonsNeeded = fragment is CameraReceiveChannelFragment
                || fragment is VideoReceiveChannelFragment
        return buttonsNeeded
    }

    override fun onYesClick() {
        // TODO: this wont be sufficient for all?
        currentChannelFragment?.init()
    }


    override fun onKeyButtonClicked() {
        val title = "Enter the key for the received data"
        val freeText = true
        val callback = object: DialogHelper.InputDialogCallback() {
            override fun onFinish(input: String) {
                password = input
            }
        }
        DialogHelper.showInputDialog(requireContext(), title, freeText, callback)
    }

    private fun getCleartext(data: String): String {
        var decryptedText: String? = null
        val password = password ?: throw IllegalArgumentException("Need password for decryption")
        for (salt in securityInterfaceHolder.hashHelper.getStaticSaltList()) {
            try {
                val key = securityInterfaceHolder.symmetricEncryptionHelper.getKeyFromPassphrase(password, salt)
                decryptedText = securityInterfaceHolder.symmetricEncryptionHelper.decrypt(data, key)!!
                break
            } catch (e: java.lang.Exception) {
                Log.v(javaClass.simpleName, "" + e.localizedMessage)
            }
        }
        if (decryptedText == null) {
            throw IllegalArgumentException("Bad password, failed decrypt")
        }
        return decryptedText
    }


    override fun getFragment(channel: Channels) : AbstractExchangeChannelFragment? {
        when (channel) {
            Channels.CAMERA -> return CameraReceiveChannelFragment()
            Channels.TEXT -> return TextReceiveChannelFragment()
            Channels.NFC -> return NFCReceiveChannelFragment()
            Channels.BLUETOOTH -> return BluetoothReceiveChannelFragment()
            Channels.SOUND -> return SoundReceiveChannelFragment()
            Channels.VIDEO -> return VideoReceiveChannelFragment()
            Channels.SD_CARD -> return SDCardReceiveChannelFragment()
        }
        return null
    }

    override fun passResult(result: String, caller: AbstractExchangeChannelFragment) {
        var chunks = result

        // First message is the number of messages if there are more than one.
        if (!preambleProcessed && !useAdditionalEncryption) {
            var indexOfSeperator = result.indexOf(DatasetProcessor.DATASET_SEPERATOR)
            // TODO: do we need this?
            if (indexOfSeperator == -1) {
                indexOfSeperator = result.length
            }
            val potentialPreamble = result.substring(0, indexOfSeperator) //  TODO java.lang.StringIndexOutOfBoundsException: String index out of range: -1
            val exchangeHelper = ExchangeHelper(securityInterfaceHolder)
            if (indexOfSeperator != -1 && exchangeHelper.isPreamble(potentialPreamble, token)) {
                maxProgress = exchangeHelper.getMessageCount(potentialPreamble, token)
                Log.i(javaClass.simpleName, "preamble detected with " + maxProgress + " items")
                preambleProcessed = true
                chunks = result.substring(indexOfSeperator + DatasetProcessor.DATASET_SEPERATOR.length)
                currentProgress++

                requireActivity().runOnUiThread {
                    initButtons()
                }
                // TODO why? we need to process it.. Dont go further as we dont have content to scan?
                // return
            }
        }

        // Process chunk.
        var decrypted = chunks
        if (useAdditionalEncryption) { // TODO: maybe we need preambel and chunking when encrypting is too large for a channel
            val trial = handleEncrypted(chunks)
            if (trial == null) {
                UiHelper.showToastFromBackgroundTask(requireContext(), "Failed decrypting, please try again.")
                return
            }
            decrypted = trial
        }

        // do only write when not null, e.g. when transfer canceled (onPause is called)
        fos?.write(decrypted.toByteArray())

        processItem(chunks)
        Log.i(javaClass.simpleName, "received data " + currentProgress)
        if (isAllDataHandled) {
            fos?.close()
            fos = null
            Log.i(javaClass.simpleName, "finished receive")
        }
    }

    private fun handleEncrypted(result: String): String? {
        try {
            return getCleartext(result)
        } catch (e: IllegalArgumentException) {
            Log.e(javaClass.simpleName, "Error while decrypting", e)
            val context = requireContext()
            val exchangeHelper = ExchangeHelper(securityInterfaceHolder)
            if (exchangeHelper.isPreamble(result, token)) {
                DialogHelper.showDialog(context,
                        "Wrong Message",
                        "You scanned the amount of messages again.")
            } else {
                DialogHelper.showDialog(context,
                        "Error decrypting received data",
                        "Probably you entered the wrong validation token. Please try again."
                )
            }
        }
        return null
    }

}