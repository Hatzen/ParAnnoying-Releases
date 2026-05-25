package de.hartz.software.parannoying.air.gap.activities.dummy

import android.app.Activity
import android.content.Intent
import android.content.Intent.CATEGORY_DEFAULT
import android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import de.hartz.software.parannoying.air.gap.R
import de.hartz.software.parannoying.air.gap.helpers.channels.QrCodeHelper
import de.hartz.software.parannoying.air.gap.interfaces.di.ExchangeApp
import de.hartz.software.parannoying.air.gap.model.ExchangeDataWrapperImpl
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.extensions.launchActivity
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.helper.ui.NotificationHelper
import de.hartz.software.parannoying.core.interfaces.di.StorageInterface
import de.hartz.software.parannoying.core.interfaces.di.air.gap.AirGapAdapter
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ExchangeDataWrapper
import de.hartz.software.parannoying.core.model.domain.settings.Channels
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

// TODO: AppCompatActivity leads to java.lang.IllegalStateException: You need to use a Theme.AppCompat theme (or descendant) with this activity.
class DummyAirGapActivity: Activity() {

    companion object {
        const val REQUEST_CODE_SINGLE = 1
        const val REQUEST_CODE_MULTIPLE = 2
        const val REQUEST_CODE_FILE = 3
        const val ENCRYPTED_CODE_SINGLE = 4
    }

    @Inject
    lateinit var storage: StorageInterface<*, *>
    @Inject
    lateinit var airGapAdapter: AirGapAdapter

    val first = ExchangeDataWrapperImpl("FirstSimpleTestData")
    val second = ExchangeDataWrapperImpl("FirstSimpleTestData2")
    val third = ExchangeDataWrapperImpl("FirstSimpleTestData3")
    val fourth = ExchangeDataWrapperImpl("FirstSimpleTestData4")

    val firstFile get() = getFileMessage()
    val secondFile get()  = getFileMessage()

    val MIXED_SEND_DATA get() = mutableListOf(first,
            second,
            firstFile,
            third,
            fourth,
            secondFile)
    val MULTIPLE_SEND_DATA get() = (1..250).map {
        // val elements = mutableListOf(first,
        //         second,
        //         third,
        //         fourth)
        // elements[it % elements.size]
        ExchangeDataWrapperImpl("SimpleTestData$it")
    }
    val FILE_SEND_DATA get()  = firstFile
    val SINGLE_SEND_DATA get()  = first
    val ENCRYPTED_SEND_DATA get()  = second
    val SEND_SYNC_DATA get()  = mutableListOf(
            second,
            fourth)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dummy_air_gap)

        (app as ExchangeApp).getActivityComponents(this)
                .inject(this)

        // Change channels.
        val button = findViewById<Button>(R.id.toggleAvailableChannels)
        val channles = storage.readSettings().allowedChannels
        if (channles.size > 1) {
            button.text = "Change to only text channels"
        } else if (channles.size == 0) {
            button.text = "Change to all channels"
            setToAllChannels()
        }

        button.setOnClickListener {
            val channles = storage.readSettings().allowedChannels
            if (channles.size > 1) {
                storage.updateSettings {
                    it.allowedChannels.clear()
                    it.allowedChannels.addAll(listOf(
                            Channels.TEXT
                    ))
                }
                button.text = "Change to all channels"
            } else {
                button.text = "Change to only text channels"
                setToAllChannels()
            }
        }

        // Send buttons.
        findViewById<Button>(R.id.sendSingleDataButton).setOnClickListener {
            airGapAdapter.startSend(DummyUseCases.SINGLE_SEND.useText(SINGLE_SEND_DATA.exchangeData))
        }
        findViewById<Button>(R.id.sendMixedDataButton).setOnClickListener {
            airGapAdapter.startSend(DummyUseCases.MULTIPLE_SEND.useDataWrappers(MIXED_SEND_DATA))
        }
        findViewById<Button>(R.id.sendMultipleDataButton).setOnClickListener {
            airGapAdapter.startSend(DummyUseCases.MULTIPLE_SEND.useDataWrappers(MULTIPLE_SEND_DATA))
        }
        findViewById<Button>(R.id.sendFileButton).setOnClickListener {
            airGapAdapter.startSend(DummyUseCases.FILE_SEND.useFile(FILE_SEND_DATA.filePath))
        }
        findViewById<Button>(R.id.sendEncryptedDataButton).setOnClickListener {
            airGapAdapter.startSend(DummyUseCases.ENCRYPTED_SEND.useText(ENCRYPTED_SEND_DATA.exchangeData))
        }
        findViewById<Button>(R.id.sendSync).setOnClickListener {
            airGapAdapter.startSync(DummyUseCases.SEND_SYNC.useDataWrappers(SEND_SYNC_DATA))
        }

        // Intent
        findViewById<Button>(R.id.importIntent).setOnClickListener {
            val bitmap = QrCodeHelper.dataToQrCode(first.exchangeData, this)!!
            // TODO: Redirect Activity wont redirect properly..
            IOHelper.initShareImage(bitmap, this)
        }
        findViewById<Button>(R.id.notificationSend).setOnClickListener {
            val it = airGapAdapter.getSendIntent(DummyUseCases.SEND_NOTIFICATION.useText(first.exchangeData))

            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK // Needed as started from context

            NotificationHelper.showCrashReportNotification(this, it)
        }
        findViewById<Button>(R.id.quickLinkPermissions).setOnClickListener {
            openAppPermissionsSettings()
        }

        // Receive
        findViewById<Button>(R.id.receiveDataButton).setOnClickListener {
            airGapAdapter.startReceive(DummyUseCases.RECEIVE)
        }
        findViewById<Button>(R.id.receiveEncryptedDataButton).setOnClickListener {
            airGapAdapter.startReceive(DummyUseCases.RECEIVE_ENCRYPTED)
        }
        findViewById<Button>(R.id.receiveSync).setOnClickListener {
            airGapAdapter.startSync(DummyUseCases.RECEIVE_SYNC.useText(second.exchangeData))
        }
        findViewById<Button>(R.id.receiveNfc).setOnClickListener {
            launchActivity<NfcReaderActivity> {  }
        }
        findViewById<Button>(R.id.sendNfc).setOnClickListener {
            launchActivity<NfcSenderActivity> {  }
        }


    }

    fun setToAllChannels() {
        storage.updateSettings {
            it.allowedChannels.clear()
            it.allowedChannels.addAll(listOf(
                    Channels.TEXT,
                    Channels.BLUETOOTH,
                    Channels.NFC,
                    Channels.CAMERA,
                    Channels.SD_CARD,
                    Channels.SOUND,
                    Channels.VIDEO
            ))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        // TODO: This is goddamn slow for multiple data... 1 sec per chunk. Probably we should use a coroutine to parse the file. mainthread is probably slow..
        val exchangeResult = airGapAdapter.onActivityResult(requestCode, resultCode, data)

        val view = findViewById<TextView>(R.id.activity_result)
        val color = if (exchangeResult.success) {
            R.color.av_green
        } else {
            R.color.av_red
        }
        view.setBackgroundColor(resources.getColor(color))

        val resultView = findViewById<EditText>(R.id.resultTextView)
        var input = mutableListOf<ExchangeDataWrapper>()
        val result = exchangeResult.result?.toList() ?: mutableListOf()

        var differences: List<ExchangeDataWrapper> = mutableListOf()
        var areNotEqual = true
        var useCase = "Unknown"

        if(exchangeResult.matchesUseCase(DummyUseCases.RECEIVE)) {
            input = mutableListOf(SINGLE_SEND_DATA)
            differences = getDifferences(input, result)
            areNotEqual = differences.isNotEmpty()
            useCase = "SINGLE_SEND"
            if (areNotEqual) {
                input = mutableListOf(FILE_SEND_DATA)
                differences = getDifferences(input, result)
                areNotEqual = differences.isNotEmpty()
                useCase = "SINGLE_FILE_SEND"
            }
            if (areNotEqual) {
                input = MULTIPLE_SEND_DATA.toMutableList()
                differences = getDifferences(input, result)
                areNotEqual = differences.isNotEmpty()
                useCase = "MULTIPLE_SEND_DATA"
            }
            if (areNotEqual) {
                useCase = "Unknown"
            }
            // TODO: When no data matches we should show error as well..
        } else if (exchangeResult.matchesUseCase(DummyUseCases.RECEIVE_ENCRYPTED)) {
            input.add(ENCRYPTED_SEND_DATA)
            useCase = "ENCRYPTED_SEND_DATA"
        } else if (exchangeResult.matchesUseCase(DummyUseCases.RECEIVE_SYNC.sendLaunchOptions)) {
            input.addAll(SEND_SYNC_DATA)
            useCase = "SEND_SYNC_DATA"
        }
        // TODO: Current problem with multiple text, leading to 84 is contained twice at least half "tData84"
        differences = getDifferences(input, result)
        areNotEqual = differences.isNotEmpty()

        resultView.setText("detected use case: " + useCase + "\n" +
                "send size:" + input.size +
                " received size: " + result.size  + "\n" +
                " has differences: " + areNotEqual + "\n" +
                " differences: " + differences.size + "\n" +
                " example message: " + (result.getOrNull(0) ?: "No data"))
    }

    private fun getDifferences(input: List<ExchangeDataWrapper>, result: List<ExchangeDataWrapper>): List<ExchangeDataWrapper> {
        return result.filter {
            val matchWithinInput = input.find { x ->
                val compareFiles = x.isFile && it.isFile
                val filesEqual = compareFiles && File(x.filePath).readText() == File(it.filePath).readText()
                filesEqual || x.exchangeData == it.exchangeData
            }
            matchWithinInput == null
        }
    }

    fun getFileMessage(): ExchangeDataWrapperImpl {
        // TODO: maybe better use a static file so we dont pollute the hdd
        val file = File.createTempFile("sample-image", "jpg")
        val outputStream = FileOutputStream(file)
        assets.open("dummy/sample.jpg").copyTo(outputStream)
        outputStream.close()
        return ExchangeDataWrapperImpl(file.absolutePath, true)
    }

    // https://stackoverflow.com/a/32822298/8524651
    fun openAppPermissionsSettings() {
        val intent = Intent().apply {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    action =  ACTION_APPLICATION_DETAILS_SETTINGS // Settings.ACTION_APPLICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                }
                else -> {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    addCategory(Intent.CATEGORY_DEFAULT)
                    data = Uri.parse("package:" + packageName)
                }
            }
        }
        with(intent) {
            data = Uri.fromParts("package", this@DummyAirGapActivity.packageName, null)
            addCategory(CATEGORY_DEFAULT)
            addFlags(FLAG_ACTIVITY_NEW_TASK)
            // The Intent.FLAG_ACTIVITY_NO_HISTORY may sometimes cause a problemous situation on tablets when a pop-up is shown within the settings screen it will close the settings screen. Simply removing that flag will solve this issue.
            //addFlags(FLAG_ACTIVITY_NO_HISTORY)
            addFlags(FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }

        startActivity(intent)
    }

}