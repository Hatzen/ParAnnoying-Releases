package de.hartz.software.parannoying.air.gap.fragments.exchange.receive

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.BarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import de.hartz.software.parannoying.air.gap.fragments.exchange.AbstractReceiveChannelFragment
import de.hartz.software.parannoying.air.gap.fragments.exchange.AirGapFragment
import de.hartz.software.parannoying.air.gap.fragments.exchange.receive.CameraReceiveChannelFragment.Companion.DECODER_TYPE
import de.hartz.software.parannoying.air.gap.fragments.exchange.receive.CameraReceiveChannelFragment.Companion.REQUEST_CODE_CHOOSE_IMAGE_TO_SCAN
import de.hartz.software.parannoying.air.gap.helpers.channels.QrCodeHelper
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.getVideoIcon
import de.hartz.software.parannoying.core.model.domain.settings.Channels
import de.hartz.software.parannoying.txqr.VideoHandlerApi


class VideoReceiveChannelFragment: AbstractReceiveChannelFragment(), BarcodeCallback {

    override val channel: Channels
        get() = Channels.VIDEO


    // TODO: Why cant this override super type View
    // override lateinit var mainView: BarcodeView
    private lateinit var mainView: BarcodeView
    override var buttonResource = { context: Context -> context.getVideoIcon(IconHelper.SMALL_ICON_WHITE) }
    lateinit var decoder: VideoHandlerApi

    override fun getMainView(): View {
        return mainView
    }

    override fun createMainView () {
        mainView = BarcodeView(context)
        val supportedCodes = listOf(BarcodeFormat.QR_CODE)
        val preferences = HashMap<DecodeHintType, Any>()
        // Only for large qr codes..
        // preferences.put(DecodeHintType.TRY_HARDER, true)
        preferences.put(DecodeHintType.POSSIBLE_FORMATS, supportedCodes)
        mainView.setDecoderFactory(DefaultDecoderFactory(supportedCodes, preferences, "UTF-8", DECODER_TYPE))
        mainView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
    }

    override fun runAdditionalAction() {
        DialogHelper.showYesNoAlert(requireContext(), "You can also select a qrcode from the gallery. " +
                "But probably it is easier to use the share functionality of the app that received the qrcode. You can share it with this app. Still want to open the Gallery?",
                DialogInterface.OnClickListener { dialog, which ->
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        val intent = Intent(Intent.ACTION_GET_CONTENT)
                        intent.type = "image/*"
                        startActivityForResult(intent, REQUEST_CODE_CHOOSE_IMAGE_TO_SCAN)
                    }
                })
    }

    override fun additionalActionDescription() {
        // Inform user that qrcodes have to be scanned, that there might be an issue with large qr codes
        // And try to import them via share functionality. Also show this message every 30 Seconds..
        Snackbar.make(requireView(), "Aim to focus the qr code with camera or import one.", Snackbar.LENGTH_LONG)
                .show()
    }

    override fun isStatusSupported(): Boolean {
        return true
    }

    override fun isConfirmationSupported(): Boolean {
        return true
    }

    override fun init() {
        decoder = VideoHandlerApi()
        mainView.resume()
        mainView.decodeContinuous(this)
    }

    override fun deinit() {
        mainView.pause()
        mainView.stopDecoding()
    }

    override fun onStart() {
        super.onStart()
        // TODO: Is this the correct place for permission handling? It mustnt be within the init method!
        // General attempt would be showing fragment only when permission is granted!
        setupCameraPermissions()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CHOOSE_IMAGE_TO_SCAN && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                // TODO: Display an error as qrcode isnt passed for some reason
                return
            }
            val inputStream = requireContext().getContentResolver().openInputStream(data.getData()!!)
            val result = QrCodeHelper.scanQRImage(BitmapFactory.decodeStream(inputStream))
            fragmentReceivedSomeData(result!!) // TODO: Do we need to handle multiple images here as well? I guess we do.
        }
    }

    private val CAMERA_REQUEST_CODE = 101

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(activity, "Scanning barcode wont work unless access granted", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun barcodeResult(result: BarcodeResult?) {
        if (Storage.readSettings().hiddenSettings.developerMode) {
            Toast.makeText(activity, result?.text, Toast.LENGTH_LONG).show()
        }
        val decoded = decoder.decode(result!!.text)

        if (decoded != null) {
            deinit()
            fragmentReceivedSomeData(decoded)
        }
    }

    private fun setupCameraPermissions() {
        val permission = ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.RECORD_AUDIO)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_REQUEST_CODE)
        }
    }

    override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {
        val points = resultPoints!!.size
        if (points > 0) {
            Log.e(javaClass.simpleName, "Found points" + points)
        }
        val status = when (points) {
            in 0..1 -> AirGapFragment.StatusColor.NO_MATCH
            in 2..3 -> AirGapFragment.StatusColor.CONNECTING
            else -> AirGapFragment.StatusColor.CONNECTED
        }
        callback!!.setExchangeStatus(status)
    }
}