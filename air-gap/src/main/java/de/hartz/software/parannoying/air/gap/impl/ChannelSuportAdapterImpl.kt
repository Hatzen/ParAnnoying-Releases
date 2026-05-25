package de.hartz.software.parannoying.air.gap.impl

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Build
import android.os.Environment
import android.util.Log
import de.hartz.software.parannoying.air.gap.fragments.exchange.receive.SDCardReceiveChannelFragment
import de.hartz.software.parannoying.air.gap.helpers.channels.bluetooth.BluetoothHelper
import de.hartz.software.parannoying.air.gap.nfc.helper.NFCHelper
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ChannelSupportAdapter


class ChannelSupportAdapterImpl(val context: Context): ChannelSupportAdapter {

    override fun isNFCSupported(): Boolean {
        // TODO: This does not work on yota phone 3+. Maybe its an os issue?
        return NFCHelper.isNFCSupported(context)
    }

    override fun isBluetoothSupported(): Boolean {
        return BluetoothHelper.isSupported()
    }

    override fun isCameraSupported(): Boolean {
        return getBackCameraResolutionInMp(context) > 5.5f
    }

    override fun isSoundSupported(): Boolean {
        val pm: PackageManager = context.packageManager
        val micPresent = pm.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)

        val audioPresent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            pm.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)
        } else {
            // Just assume it has audio output..
            true
        }
        return micPresent || audioPresent
    }

    override fun isSDSupported(): Boolean {
        // https://stackoverflow.com/a/7429264/8524651
        return Environment.isExternalStorageRemovable()
    }

    override fun getSDDrives(): List<String> {
        return SDCardReceiveChannelFragment.getSdDrives(context).map { it.absolutePath }
    }


    // https://stackoverflow.com/a/27000029/8524651
    fun getBackCameraResolutionInMp(context: Context): Float {
        val default = 100.0f
        val versionAboveM = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        if (versionAboveM) {
            // TODO: usually we should require camera permission..
            val permissionGranted =
                    context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            if (!permissionGranted) {
                Log.w(javaClass.simpleName, "Cannot determine camera value. Assuming camera works.")
                return default
            }
        }
        return try {
            getMps()
        } catch (e: Exception) {
            Log.w(javaClass.simpleName, "Cannot determine camera value. Assuming camera works.")
            default
        }
    }

    private fun getMps (): Float {
        val noOfCameras = Camera.getNumberOfCameras()
        var maxResolution = -1f
        var pixelCount: Long = -1
        for (i in 0 until noOfCameras) {
            val cameraInfo = android.hardware.Camera.CameraInfo()
            Camera.getCameraInfo(i, cameraInfo)

            if (cameraInfo.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK) {
                val camera = Camera.open(i)
                val cameraParams = camera.getParameters()
                for (j in 0 until cameraParams.getSupportedPictureSizes().size) {
                    val pixelCountTemp = cameraParams.getSupportedPictureSizes().get(j).width * cameraParams.getSupportedPictureSizes().get(j).height // Just changed i to j in this loop
                    if (pixelCountTemp > pixelCount) {
                        pixelCount = pixelCountTemp.toLong()
                        maxResolution = pixelCountTemp.toFloat() / 1024000.0f
                    }
                }

                camera.release()
            }
        }

        return maxResolution
    }
}