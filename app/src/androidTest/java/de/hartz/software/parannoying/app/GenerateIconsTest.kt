package de.hartz.software.parannoying.app

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.test.core.app.ApplicationProvider
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import de.hartz.software.parannoying.app.large.tests.utils.DevelopmentFileUploader
import de.hartz.software.parannoying.core.activities.insecured.welcome.WelcomeActivity
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.getGearIcon
import de.hartz.software.parannoying.core.helper.ui.getInfoIcon
import de.hartz.software.parannoying.core.helper.ui.getSyncMessagesIcon
import de.hartz.software.parannoying.core.helper.ui.getUserIdIcon
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4ClassRunner::class)
class GenerateIconsTest {

    @Rule
    @JvmField var welcomeActivityRule: ActivityTestRule<WelcomeActivity> = ActivityTestRule(WelcomeActivity::class.java)

    lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<App>()
    }

    @Ignore
    @Test
    fun generate() {
        val uploader = DevelopmentFileUploader()
        try {
            // set faster timeout as casually we would wait 60 seconds per test..
            val latch = CountDownLatch(1)

            // TODO: Like this we dont get the exception anymore.. But latch should lead to exception..
            // TODO:
            Thread {
                // Test if the server is running. which might not be the case when running single tests..
                uploader.test()
                latch.countDown()
            }.start()

            latch.await(5, TimeUnit.SECONDS)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        var bitmap = context.getGearIcon(IconHelper.SMALL_ICON_PRIMARY).toBitmap()
        var file = saveBitmapToFile(context, bitmap, "tmpIcon.png")
        uploader.uploadFile(file, "gear.png")

        bitmap = context.getSyncMessagesIcon(IconHelper.SMALL_ICON_PRIMARY).toBitmap()
        file = saveBitmapToFile(context, bitmap, "tmpIcon.png")
        uploader.uploadFile(file, "sync.png")

        bitmap = context.getInfoIcon(IconHelper.SMALL_ICON_PRIMARY).toBitmap()
        file = saveBitmapToFile(context, bitmap, "tmpIcon.png")
        uploader.uploadFile(file, "info.png")

        bitmap = context.getUserIdIcon(IconHelper.SMALL_ICON_PRIMARY).toBitmap()
        file = saveBitmapToFile(context, bitmap, "tmpIcon.png")
        uploader.uploadFile(file, "userid.png")

    }

    fun saveBitmapToFile(context: Context, bitmap: Bitmap, filename: String): File {
        val file = File(context.cacheDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }
}
