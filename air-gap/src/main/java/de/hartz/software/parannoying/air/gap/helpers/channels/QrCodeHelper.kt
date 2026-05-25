package de.hartz.software.parannoying.air.gap.helpers.channels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.text.TextUtils
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import de.hartz.software.parannoying.core.R
import de.hartz.software.parannoying.core.extensions.app
import net.sourceforge.zbar.Image
import net.sourceforge.zbar.ImageScanner

object QrCodeHelper {

    private const val QR_SIZE: Int = 512
    val QR_FORMAT = BarcodeFormat.QR_CODE // Currently only qrcodes will work. As we need to create the images, but also scan static images. Which is only working for qrcodes.

    // Maybe enable setting for showing image behind qrcode
    // https://aboullaite.me/generate-qrcode-with-logo-image-using-zxing/
    // https://stackoverflow.com/questions/35104305/how-to-generate-qr-code-with-logo-inside-it
    fun dataToQrCode(data: String, context: Context) : Bitmap? {
        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix = multiFormatWriter.encode(data, QR_FORMAT, QR_SIZE, QR_SIZE)
            val barcodeEncoder = BarcodeEncoder()
            val qrCode = barcodeEncoder.createBitmap(bitMatrix)
            if (!context.app.Storage.readSettings().useWatermark) {
                return qrCode
            }
            // Added watermark
            val background = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_foreground)
            return overlayWithoutWhite(qrCode, background)
        } catch (e: WriterException) {
            Log.e("CrashReportHandler", "2.66", e)
            e.printStackTrace()
        }
        return null
    }

    // https://stackoverflow.com/questions/32134072/qr-code-scan-from-image-file
    fun scanQRImage(bMap: Bitmap): String? {

        val mImageScanner = ImageScanner()

        // https://sourceforge.net/p/zbar/discussion/2308158/thread/ce0d36f8/
        val width = bMap.getWidth();
        val height = bMap.getHeight();
        val pixels = IntArray(width * height)

        bMap.getPixels(pixels, 0, width, 0, 0, width, height)

        var barcode = Image(width, height, "RGB4")
        barcode.setData(pixels)
        barcode = barcode.convert("Y800")

        var qrCodeString: String? = null

        val result = mImageScanner.scanImage(barcode)
        if (result != 0) {
            val symSet = mImageScanner.results
            for (sym in symSet)
                qrCodeString = sym.data
        }

        if (!TextUtils.isEmpty(qrCodeString)) {
            // Successfully.
            return qrCodeString
        }
        throw java.lang.RuntimeException("Could not identify image file")
    }

    private fun overlayWithoutWhite(foreground: Bitmap, background: Bitmap): Bitmap {
        val width = foreground.width
        val height = foreground.height

        // Neues Bitmap erstellen
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // Hintergrund zentriert zeichnen
        val bgWidth = background.width
        val bgHeight = background.height
        val left = (width - bgWidth) / 2f
        val top = (height - bgHeight) / 2f
        canvas.drawBitmap(background, left, top, null)

        // Vordergrund vorbereiten (nur weiße Flächen maskieren)
        val mutableForeground = foreground.copy(Bitmap.Config.ARGB_8888, true)
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = mutableForeground.getPixel(x, y)
                if (isPureWhiteBackgroundPixel(pixel)) {
                    mutableForeground.setPixel(x, y, Color.TRANSPARENT)
                }
            }
        }

        // Vordergrund überlagern
        canvas.drawBitmap(mutableForeground, 0f, 0f, null)

        // 3️⃣ Alle transparenten Pixel mit Weiß ersetzen
        val finalBitmap = result.copy(Bitmap.Config.ARGB_8888, true)
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = finalBitmap.getPixel(x, y)
                if (Color.alpha(pixel) == 0) {
                    finalBitmap.setPixel(x, y, Color.WHITE)
                }
            }
        }

        return finalBitmap
    }

    fun isPureWhiteBackgroundPixel(pixel: Int, tolerance: Int = 5): Boolean {
        val r = Color.red(pixel)
        val g = Color.green(pixel)
        val b = Color.blue(pixel)
        val a = Color.alpha(pixel)

        return a > 0 && r >= 255 - tolerance && g >= 255 - tolerance && b >= 255 - tolerance
    }
}