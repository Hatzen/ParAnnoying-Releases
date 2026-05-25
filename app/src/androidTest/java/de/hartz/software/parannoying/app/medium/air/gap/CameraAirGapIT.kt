package de.hartz.software.parannoying.app.medium.air.gap

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.core.view.drawToBitmap
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.isInternal
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.filters.MediumTest
import de.hartz.software.parannoying.app.R
import de.hartz.software.parannoying.core.model.domain.settings.Channels
import org.hamcrest.CoreMatchers.not
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


@MediumTest
@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
class CameraAirGapIT: AbstractAirGapIntegrationTest() {

    val messages = ArrayList<Bitmap>()


    @Test
    fun tesMultiple() {
        Intents.init()
        testChannelWithUseCase(Channels.CAMERA, R.id.sendMultipleDataButton)
        Intents.release()
    }

    @Test
    fun tesSingle() {
        Intents.init()
        testChannelWithUseCase(Channels.CAMERA, R.id.sendSingleDataButton)
        Intents.release()
    }

    override fun send() {
        val qrCodeView = sendActivity.findViewById<View>(android.R.id.content)
                .findViewWithTag<ImageView>("qrCodeImage")!!
        val image = qrCodeView.drawToBitmap()
        messages.add(image)
        Espresso.onView(ViewMatchers.withId(R.id.fab_yes)).perform(ViewActions.click())
        Thread.sleep(1000L)
    }

    override fun receive() {
        messages.forEach {
            val resultData = Intent()
            resultData.data = getImageUri(context, it)

            // https://stackoverflow.com/a/32273525/8524651
            intending(not(isInternal())).respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, resultData))

            // TODO: Currently needed as confirmation is still playing..
            Thread.sleep(5000L)
            Espresso.onView(ViewMatchers.withId(R.id.fab_action_send)).perform(ViewActions.click())
            Thread.sleep(2000L)
        }
    }

    // https://stackoverflow.com/a/73547282/8524651
    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val tempFile = File.createTempFile("temprentpk", ".png")
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes)
        val bitmapData = bytes.toByteArray()

        val fileOutPut = FileOutputStream(tempFile)
        fileOutPut.write(bitmapData)
        fileOutPut.flush()
        fileOutPut.close()
        return Uri.fromFile(tempFile)
    }

}