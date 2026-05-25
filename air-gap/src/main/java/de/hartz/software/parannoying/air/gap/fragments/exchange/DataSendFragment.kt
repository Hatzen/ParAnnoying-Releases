package de.hartz.software.parannoying.air.gap.fragments.exchange

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import de.hartz.software.parannoying.air.gap.fragments.exchange.send.BluetoothSendChannelFragment
import de.hartz.software.parannoying.air.gap.fragments.exchange.send.CameraSendChannelFragment
import de.hartz.software.parannoying.air.gap.fragments.exchange.send.NFCSendChannelFragment
import de.hartz.software.parannoying.air.gap.fragments.exchange.send.SDCardSendChannelFragment
import de.hartz.software.parannoying.air.gap.fragments.exchange.send.SoundSendChannelFragment
import de.hartz.software.parannoying.air.gap.fragments.exchange.send.TextSendChannelFragment
import de.hartz.software.parannoying.air.gap.fragments.exchange.send.VideoSendChannelFragment
import de.hartz.software.parannoying.air.gap.helpers.ConfirmationHelper
import de.hartz.software.parannoying.air.gap.helpers.DatasetProcessor
import de.hartz.software.parannoying.air.gap.helpers.ExchangeHelper
import de.hartz.software.parannoying.air.gap.interfaces.di.ExchangeApp
import de.hartz.software.parannoying.air.gap.interfaces.exchange.SendFragmentDataProvider
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.core.model.domain.settings.Channels
import de.hartz.software.parannoying.ggwave.interfaces.ReceivedConfirmation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ArrayBlockingQueue
import javax.inject.Inject

class DataSendFragment: AirGapFragment(), SendFragmentDataProvider {

    @Inject
    lateinit var securityInterfaceHolder: SecurityInterfaceHolder
    companion object {
        val ONE_CHUNK_AS_MAX = -1

        const val EXTRA_YES_NO = "EXTRA_YES_NO"
        const val EXTRA_USE_ADDITIONAL_ENCRYPTION = "EXTRA_USE_ADDITIONAL_ENCRYPTION"
        const val EXTRA_DATA_FILE = "EXTRA_DATA_FILE"
        const val EXTRA_CHUNK_COUNT = "EXTRA_CHUNK_COUNT"

        const val VALIDATION_TOKEN_PREFIX = "Validationtoken: "
    }

    lateinit var rawDatas: Iterator<String>

    val numberOfMessagesToHandle get() = maxProgress
    // We are only finish when user is after last message (so he can send the last message).
    override val isAllDataHandled get() = run { maxProgress + 1 <= currentProgress }

    var currentElement: String? = null
    private var stackedData = StringBuilder()


    lateinit var password: String
    val currentSendChannelFragment: AbstractSendChannelFragment get() =
        currentChannelFragment as AbstractSendChannelFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (app as ExchangeApp).getActivityComponents(requireActivity()).inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val intent = requireActivity().intent
        // Intent is null in onboarding activity.
        if (intent != null) {
            // TODO: Ensure this is called only once!
            initFromIntent(intent)
        }
    }

    private fun initFromIntent(intent: Intent) {
        showYesNoButtons = intent.getBooleanExtra(EXTRA_YES_NO, false)
        // Handle multiple data.
        val fileName = intent.getStringExtra(EXTRA_DATA_FILE)!!
        val processor = DatasetProcessor()
        var rawDatasSequence = processor.readFileChunks(fileName)

        if (useAdditionalEncryption) {
            val salt = securityInterfaceHolder.hashHelper.getStaticSaltList().random()
            password = securityInterfaceHolder.randomHelper.getRandomPinCode()
            val key = securityInterfaceHolder.symmetricEncryptionHelper.getKeyFromPassphrase(password, salt)
            rawDatasSequence = rawDatasSequence.map {
                securityInterfaceHolder.symmetricEncryptionHelper.encrypt(it, key)!!
            }
        }

        maxProgress = intent.getLongExtra(EXTRA_CHUNK_COUNT, SINGLE_DATA_COUNT.toLong()).toInt()

        if (numberOfMessagesToHandle > 1) {
            // Preamble is salted so looks different every time.
            val preamble = ExchangeHelper(securityInterfaceHolder).getPreamble(token, numberOfMessagesToHandle)
            rawDatasSequence = sequenceOf(preamble).plus(rawDatasSequence)
            // We need to increase the passed data.
            maxProgress++
        }

        rawDatas = rawDatasSequence.iterator()
        initButtons()
    }

    override fun readyForTransmitting() {
        super.readyForTransmitting()
        if (currentElement == null) {
            startTransferAll()
        } else {
            // Generate QrCodes etc. when switching channel. but also starts sound transfer etc.
            currentSendChannelFragment.startTransferDataSet(currentElement!!)
        }
    }


    override fun processItem(item: String) {
        super.processItem(item)
        Log.i(javaClass.simpleName, "processItem" + currentProgress)
        provideData(item)
    }


    fun provideData(newData: String) {
        // TODO: Analyze why it is so slow.. 1 Chunk ca 1 Sec..
        val maxDataSize = currentSendChannelFragment.maxDataSize
        val isSingleChunk = maxDataSize != ONE_CHUNK_AS_MAX && stackedData.isEmpty()
        // TODO: Somehow the chunks ge bigger than buffer e.g. 2008 instead of 2000, maybe additional seperators?
        val hasBufferSpace = (newData.length + stackedData.length) < maxDataSize
        val pullAnotherChunk = isSingleChunk || hasBufferSpace
        val hasAnotherChunk = hasAnotherChunk()
        // TODO: stackeddata
        // if (hasBufferSpace) {
        //     stackedData.append(newData)
        // }
        stackedData.append(newData)
        if (pullAnotherChunk && hasAnotherChunk) {
            processNextItem()
        } else {
            currentElement = stackedData.toString()

            currentSendChannelFragment.startTransferDataSet(currentElement!!)
            stackedData.clear()
            // if (!hasBufferSpace) {
            //     stackedData.append(newData)
            // }
        }
    }

    override fun beforeFragmentChange() {
        val tmp = currentElement ?: return
        val maxDataSize = currentSendChannelFragment.maxDataSize
        if (tmp.length > maxDataSize) {
            currentElement = tmp.substring(0, tmp.length)
            val overhead = tmp.substring(maxDataSize, tmp.length)
            stackedData.insert(0, overhead)
        } else {
            // TODO: this would be useful to check the real max size of every fragment.
            //   and we probably have the same problem for the first case as we might have multiple times of allowed data in stacked data?
            //   maybe fixed with switch to non recursive function??
            // processNextItem()
        }
    }

    private val queue = ArrayBlockingQueue<String>(1)
    val scope = CoroutineScope(Job() + Dispatchers.IO)

    private fun startTransferAll() {
        scope.launch {
            var firstRunInitializeFragmentWithData = false
            while (!isAllDataHandled) {
                if (hasAnotherChunk()) {
                    val currentData = getNextChunkOfDataToSend()

                    // Needed as submethods might access ui
                    withContext(Dispatchers.Main) {
                        processItem(currentData)
                    }
                } else {
                    // finish last send item.
                    currentProgress += 1
                    break
                }

                if (firstRunInitializeFragmentWithData) {
                    // Generate QrCodes etc. when switching channel. but also starts sound transfer etc.
                    currentSendChannelFragment.startTransferDataSet(currentElement!!)
                }

                if (currentSendChannelFragment.useConfirmation()) {
                    // TODO needs to be tested, probably we need to run this on main thread
                    withContext(Dispatchers.Main) {
                        currentElement!!.let {
                            ConfirmationHelper(securityInterfaceHolder)
                                .receiveConfirmation(it, object : ReceivedConfirmation {
                                override fun receivedValidConfirmation() {
                                    processNextItem()
                                }

                                override fun errorReceiving() {
                                    throw RuntimeException("there was something wrong while receiving usually we dont want to crash as noise can always happen?")
                                }
                            }, requireActivity())
                        }
                    }
                }

                // wait until item is processed.
                withContext(Dispatchers.IO) {
                    queue.take()
                }

            }
            if (isAllDataHandled && hasAnotherChunk()) {
                throw RuntimeException("Chunk count didnt match")
            }
            if (isAllDataHandled) {
                Log.i(javaClass.simpleName, "All data is handled")
                airGapActivity?.handleAllDataProcessed()
            }
        }

    }

    fun processNextItem() {
        // Might get called multipe times when switching channels. Maybe a design issue?
        if(queue.size == 0) {
            // Unlock processing next item.
            queue.put("dummy lock")
        }
    }

    override fun currentFragmentSupportsYesNo (fragment: AbstractExchangeChannelFragment): Boolean {
        val buttonsNeeded = fragment is CameraSendChannelFragment
                || fragment is TextSendChannelFragment
                || fragment is SoundSendChannelFragment
                || fragment is VideoSendChannelFragment
        return buttonsNeeded
    }

    override fun onDestroy() {
        super.onDestroy()
        if (scope.isActive) {
            scope.cancel()
        }
    }

    override fun onYesClick() {
        processNextItem()
    }

    override fun onKeyButtonClicked() {
        val snackBarText = VALIDATION_TOKEN_PREFIX + password
        UiHelper.showSnackbarTop(snackBarText, requireView())
    }

    override fun getFragment(channel: Channels): AbstractExchangeChannelFragment? {
        when (channel) {
            Channels.CAMERA -> return CameraSendChannelFragment()
            Channels.TEXT -> return TextSendChannelFragment()
            Channels.NFC -> return NFCSendChannelFragment()
            Channels.BLUETOOTH -> return BluetoothSendChannelFragment()
            Channels.SOUND -> return SoundSendChannelFragment()
            Channels.VIDEO -> return VideoSendChannelFragment()
            Channels.SD_CARD -> return SDCardSendChannelFragment()
        }
        return null
    }

    override fun getNextChunkOfDataToSend(): String {
        airGapActivity?.refreshProgress()
        currentElement = rawDatas.next()

        return currentElement!!
    }

    override fun hasAnotherChunk(): Boolean {
        return rawDatas.hasNext()
    }
}