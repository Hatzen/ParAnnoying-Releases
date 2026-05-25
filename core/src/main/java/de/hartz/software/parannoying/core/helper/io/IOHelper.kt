package de.hartz.software.parannoying.core.helper.io
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import de.hartz.software.parannoying.core.R
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.profilepicturegenerator.PictureGenerator
import de.hartz.software.profilepicturegenerator.SingleShape
import de.hartz.software.profilepicturegenerator.Symmetric
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream


// TODO: Handle context in other nmanner
@SuppressLint("StaticFieldLeak")
object IOHelper {

    fun getFirstFileWithExtension(folder: File, extension: String): File? {
        return folder
            .listFiles { file -> file.isFile && file.extension.equals(extension, ignoreCase = true) }
            ?.firstOrNull()
    }

    fun getBackupDir(context: Context): File {
        return File(context.getExternalFilesDir(null), "realm/")
    }

    fun zipFiles(files: List<File>, zipFile: File) {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
            files.forEach { file ->
                if (!file.exists()) return@forEach
                FileInputStream(file).use { fis ->
                    val entry = ZipEntry(file.name)
                    zos.putNextEntry(entry)

                    fis.copyTo(zos, bufferSize = 1024)
                    zos.closeEntry()
                }
            }
        }
    }

    fun unzip(zipFile: File, targetDir: File) {
        ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                val outFile = File(targetDir, entry.name)

                // Verzeichnisse anlegen, falls nötig
                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { fos ->
                        zis.copyTo(fos)
                    }
                }

                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }

    // https://stackoverflow.com/questions/40907742/how-to-disable-firebase-messaging-on-boolean-from-shared-preferences-android
    /**
     * Enable services which are only needed for specific devices.
     */
    fun <T> startDisabledService(service: Class<T>, context: Context, enable: Boolean = true) where T: Service {
        val pm = context.packageManager
        val componentName = ComponentName(
            context,
            service
        )
        var enabledEnum = PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        if (!enable) {
            enabledEnum = PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        pm.setComponentEnabledSetting(
            componentName,
            enabledEnum,
            PackageManager.DONT_KILL_APP
        )
    }

    fun isHardwareEncrypted(context: Context): Boolean {
        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val status = devicePolicyManager.storageEncryptionStatus
        if (DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE == status) {
            return true
        }
        return false
    }

    fun getFileProviderPackagename(context: Context): String {
        return context.getApplicationContext().getPackageName() + ".fileprovider"
    }

    fun hasInternetConnection(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting // TODO: Maybe better use detailed state
    }

    val RESULT_CODE_SHARE_IMAGE = 9233

    fun initShareImage(bitmap: Bitmap, activity: Activity) {
        if (!isStoragePermissionGranted(activity)) {
            ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    STORAGE_REQUEST_CODE)
            return
        }
        init(activity)
        initNewImageUriAndFile()
        saveImageFile(bitmap)

        // create a new share intent and start it
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_STREAM, getImageUri())
        shareIntent.type = getImageMimeType()
        activity.startActivityForResult(Intent.createChooser(shareIntent, "Share QrCode"), RESULT_CODE_SHARE_IMAGE)
        deinit()
    }

    fun initShareFile(file: File, context: Context) : Intent? {
        if (!isStoragePermissionGranted(context)) {
            return null
        }
        init(context)

        sharedFile = getOutputMediaFile(MEDIA_TYPE_RAW, "DUMP_" + getTimestamp())

        val targetUri = FileProvider.getUriForFile(context,
                getFileProviderPackagename(context), sharedFile)
        storeFile(file, sharedFile)
        // create a new share intent and start it
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_STREAM, targetUri)
        shareIntent.type = "text/plain" // TODO: is this correct?
        deinit()
        return Intent.createChooser(shareIntent, "Share data files")
    }

    fun isCameraPermissionGranted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        } else { //permission is automatically granted on sdk<23 upon installation
            return true
        }
    }

    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.lowercase().contains("emulator")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || "google_sdk" == Build.PRODUCT)
    }

     fun isStoragePermissionGranted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        } else { //permission is automatically granted on sdk<23 upon installation
            return true
        }
    }

    /**
     * Imageseed needs a string with format: "text#double"
     */
    fun getProfilePictureForUser(imageSeed: String, context: Context): Bitmap {
        val shape = SingleShape.Rectangle
        val width = 6
        val height = 6
        val indexOfSeperator = imageSeed.lastIndexOf('#')
        val imageKey = imageSeed.substring(0, indexOfSeperator)
        val imageColorSeed = imageSeed.substring(indexOfSeperator + 1).toDouble()

        val color1 = UiHelper.getTrafficLightColor(imageColorSeed.toFloat(), context) // ResourcesCompat.getColor(context.getResources(), R.color.colorPrimaryDark, null)
        val color2 = ResourcesCompat.getColor(context.getResources(), R.color.grayAccent, null)
        val colors = intArrayOf(color1, color2)
        val symmetric = Symmetric.NONE
        val instance = PictureGenerator.generate(width, height, colors, 2, shape, symmetric)
        instance.seed = imageKey
        return instance.createNew()
    }


    /**
     * Imageseed needs a string with format: "text"
     */
    fun getProfilePictureForUserWithHashOnly(imageSeed: String, context: Context): Bitmap {
        val shape = SingleShape.Rectangle
        val width = 6
        val height = 6

        val color1 = ResourcesCompat.getColor(context.getResources(), R.color.colorAccent, null)
        val color2 = ResourcesCompat.getColor(context.getResources(), R.color.grayAccent, null)
        val colors = intArrayOf(color1, color2)
        val symmetric = Symmetric.NONE
        val instance = PictureGenerator.generate(width, height, colors, 2, shape, symmetric)
        instance.seed = imageSeed
        return instance.createNew()
    }

    /**
     * Gets the state of Airplane Mode.
     *
     * @param context
     * @return true if enabled.
     */
    fun isAirplaneModeOn(context: Context): Boolean {
        return Settings.System.getInt(context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0
    }

    /**
     * Returns time in seconds.
     */
    fun getCurrentDateAsUnixTimestamp() : Long {
        return System.currentTimeMillis() / 1000L
    }

    val STORAGE_REQUEST_CODE = 1232

    val DEFAULT_CHARSET = Charset.forName("UTF-8")

    /**
     * Possible values for DEFAULT_IMG_TYPE.
     */
    enum class ImgType {
        PNG,
        JPG
    }

    /**
     * The image quality that is kept when saving bitmaps in jpeg files.
     * 0-100. 0 meaning compress for small size, 100 meaning compress for max quality. Some formats,
     * like PNG which is lossless, will ignore the quality setting.
     */
    val IMAGE_EXPORT_QUALITY = 100

    /**
     * This image type will be used by default for exporting bitmaps into image files.
     * Changing this value will invalidate all existing history entries. So at least the
     * HistoryEntry.serialVersionUID value must be incremented, too.
     * When trying to use jpg, pay special attention to the sharable image. At least
     * the pie chart part must probably be changed, because the scanResult will look ugly otherwise.
     */
    val DEFAULT_IMG_TYPE = ImgType.PNG

    /**
     * This file object represents the newly created image.
     */
    private lateinit var sharedFile: File

    /**
     * The file path where the final image is saved.
     * I.e. this points to sharedFile.
     */
    private lateinit var imageUri: Uri

    val MEDIA_TYPE_IMAGE_JPG = 1
    val MEDIA_TYPE_IMAGE_PNG = 2
    val MEDIA_TYPE_RAW = 3

    private var context: Context? = null


    /**
     * Create the image uri that might be used later to actually save the file.
     * @return
     */
    fun initNewImageUriAndFile() {
        sharedFile = getOutputMediaFile(
                if (DEFAULT_IMG_TYPE === ImgType.JPG)
                    MEDIA_TYPE_IMAGE_JPG
                else
                    MEDIA_TYPE_IMAGE_PNG,
                "IMG_SHARE_" + getTimestamp())
        /**
         * Instead of using the privateUri, we must create another public Uri which can be accessed
         * by other apps without the need to have read permissions, too.
         * Otherwise an error will be thrown for Android N+.
         * See https://stackoverflow.com/a/38858040
         */
        //Uri privateUri = Uri.fromFile(sharedFile);
        imageUri = FileProvider.getUriForFile(context!!,
                getFileProviderPackagename(context!!), sharedFile)
    }

    private fun getTimestamp() : String {
        return "TIME"
    }

    /**
     * Save the final scanResult to an image file.
     */
    fun saveImageFile(bitmap: Bitmap) {
        storeBitmap(bitmap, sharedFile)
    }

    // TODO: This have to be moved!!! Either have an Singleton or an init...
    fun init(context: Context) {
        IOHelper.context = context
    }

    // TODO: This have to be moved!!! Either have an Singleton or an init...
    fun deinit() {
        context = null
    }

    /**
     * Store the given bitmap on disk.
     * @param bitmap
     * @param dstPath The destination file path. This includes already the image file name itself
     */
    fun storeBitmap(bitmap: Bitmap, dstPath: File) {
        // store image
        var out : FileOutputStream? = null
        try {
            out = FileOutputStream(dstPath)
            val format : Bitmap.CompressFormat
            if (DEFAULT_IMG_TYPE == ImgType.JPG) {
                format = Bitmap.CompressFormat.JPEG
            } else {
                format = Bitmap.CompressFormat.PNG
            }
            bitmap.compress(format, IMAGE_EXPORT_QUALITY, out)
        } catch (e: Exception) {
            Log.e("IOHelper", "Could not store bitmap: " + e.localizedMessage)
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (e: Exception) {
                // couldn't close the stream, doesn't matter
            }
        }
    }

    /**
     * Store the given bitmap on disk.
     * @param src
     * @param dest The destination file path. This includes already the image file name itself
     */
    fun storeFile(src: File, dest: File) {
        val input = FileInputStream(src)
        val output = FileOutputStream(dest)

        val buffer = ByteArray(1024)
        var read = 0
        while (read != -1) {
            read = input.read(buffer)
            if (read == -1) {
                break
            }
            output.write(buffer, 0, read)
        }
        input.close()

        output.flush()
        output.close()
    }

    /**
     * Get the file path of the new combined image file.
     * Keep in mind that the image might not exist yet.
     * @return
     */
    fun getImageUri(): Uri {
        return imageUri
    }

    /**
     * Get the mime type of the saved image file.
     * @return
     */
    fun getImageMimeType(): String {
        return "image/PNG"
    }

    /**
     * Create a file for saving an image or video by using a predefined file name.
     * @param type
     * @param fileNameWithoutExt
     * @return
     */
    @Synchronized
    fun getOutputMediaFile(type: Int, fileNameWithoutExt: String): File {

        if (type != MEDIA_TYPE_IMAGE_JPG && type != MEDIA_TYPE_IMAGE_PNG
                && type != MEDIA_TYPE_RAW) {
            throw IllegalArgumentException("The given media file type ($type) is not supported.")
        }

        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        val environmentDir = if (type == MEDIA_TYPE_RAW) Environment.DIRECTORY_DOWNLOADS else Environment.DIRECTORY_PICTURES
        var mediaStorageDir = File(Environment.getExternalStoragePublicDirectory(
                environmentDir), getApplicationName(context!!))
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // ensure that the external storage is writable,
        // if it's not, just use the temporary cache
        val externalStorageState = Environment.getExternalStorageState()
        if (!Environment.MEDIA_MOUNTED.equals(externalStorageState)) {
            // TODO manually delete these cached files asap, because the user can't access them
            // in the first place
            Log.e("IOHelper", "External storage is not writable, so we're going to use the cache instead.")
            mediaStorageDir = getCacheDir(context!!)
        }

        // Create the storage directory if it does not exist
        var dirTrial = 1
        while (dirTrial < 3 && !mediaStorageDir.exists()) {
            try {
                if (!mediaStorageDir.mkdirs()) {
                    // theoretically, it may be possible that the above call returns false, because
                    // another thread created the same dir in between. so ignore the error, if the dir
                    // exists now
                    // see https://gitlab.timhartz.de/tim/dogs/issues/112
                    if (!mediaStorageDir.exists()) {
                        throw RuntimeException("Failed to create directory for media file: $mediaStorageDir")
                    } else if (dirTrial > 1) {
                        // log success on re-trial
                        // TODO remove this log if it is clear that it can help
                        val eFake = RuntimeException("Creating a dir worked in trial $dirTrial")
                    }
                }
            } catch (e: RuntimeException) {
                mediaStorageDir = getCacheDir(context!!)
                Log.e("IOHelper", "Starting a second trial using the cache dir: $mediaStorageDir")
            }

            dirTrial++
        }

        // Create a media file name
        val mediaFile: File
        if (type == MEDIA_TYPE_IMAGE_JPG || type == MEDIA_TYPE_IMAGE_PNG) {
            val ext = if (type == MEDIA_TYPE_IMAGE_PNG) "png" else "jpg"
            mediaFile = File(mediaStorageDir.getPath() + File.separator +
                    fileNameWithoutExt + "." + ext)
        } else { // if(type == MEDIA_TYPE_RAW)
            mediaFile = File(mediaStorageDir.getPath() + File.separator +
                    fileNameWithoutExt + ".raw")
        }

        return mediaFile
    }

    /**
     * Get a dir in the cache (instead of one on the sd card).
     * @return
     */
    private fun getCacheDir(context: Context): File {
        return File(
                context.getCacheDir().toString() + File.separator + "Pictures")
    }

    /**
     * Get the (localized) name of this app.
     * See https://stackoverflow.com/a/15114434/1665966
     * @return
     */
    fun getApplicationName(context: Context) : String {
        val applicationInfo = context.getApplicationInfo();
        val stringId = applicationInfo.labelRes
        if (stringId == 0 ) {
            return applicationInfo.nonLocalizedLabel.toString()
        }
        return context.getString(stringId);
    }


}