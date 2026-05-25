package de.hartz.software.parannoying.offline.activities.offline

import android.annotation.SuppressLint
import android.graphics.BlurMaskFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.camera.camera2.Camera2AppConfig
import androidx.camera.core.CameraX
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysisConfig
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureConfig
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidviewhover.BlurLayout
import com.mikepenz.iconics.Iconics
import de.hartz.software.parannoying.core.R
import de.hartz.software.parannoying.core.activities.BaseActivity
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.offline.helper.guard.ConnectionGuard
import de.hartz.software.parannoying.offline.helper.guard.facedetection.FaceTrackingAnalyzer
import de.hartz.software.parannoying.offline.interfaces.OfflineApplication
import de.hartz.software.parannoying.offline.model.OfflineStorage
import de.hartz.software.parannoying.offline.model.domain.dialogs.CurrentUser


abstract class BaseOfflineActivity : BaseActivity() {

    private var isShadowed: Boolean = false
    // 40 secs.
    private val TIME_TO_WAIT: Long = 40000
    private var inactivityHandler: Handler? = Handler()
    private lateinit var blurLayout: BlurLayout
    private lateinit var hover: View

    val app: OfflineApplication get() = application as OfflineApplication

    val Storage: OfflineStorage
        get() = app.Storage as OfflineStorage

    val currentUser: CurrentUser
        get() = Storage.currentUser

    private var handleInactivity: Runnable = Runnable {
        if (!Storage.readSettings().screenSaver) {
            return@Runnable
        }
        try {
            // Renderscripts got removed no library works.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val blurEffect = RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.CLAMP)
                findViewById<ViewGroup>(android.R.id.content).getChildAt(0).setRenderEffect(blurEffect)
            } else {
                blurLayout.showHover()
                blurHackForTextViews(blurLayout, true)
            }
            restart()
            isShadowed = true
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Blur not supported", e)
            runOnUiThread {
                DialogHelper.showAlert(this, "Device doesnt support Screensaver go to settings to disable it: " + e.localizedMessage)
            }
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        Log.d("Blurry", "onUserInteraction")
        if (isShadowed) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                findViewById<ViewGroup>(android.R.id.content).getChildAt(0).setRenderEffect(null)
            } else {
                blurLayout.dismissHover()
                blurHackForTextViews(blurLayout, false)
            }
        }
        restart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Needs to be called for some reason per activity, otherwise the icons are not set properly.
        Iconics.init(applicationContext)
        // Add screen protection if enabled.
        if (!Storage.readSettings().hiddenSettings.allowScreenshots) {
            // https://stackoverflow.com/questions/9822076/how-do-i-prevent-android-taking-a-screenshot-when-my-app-goes-to-the-background
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        initViewProtection()
    }

    override fun onPause() {
        super.onPause()
        inactivityHandler!!.removeCallbacks(handleInactivity)
        if (isFaceDetectionPossible()) {
            CameraX.unbindAll()
        }
    }

    override fun onResume() {
        super.onResume()
        checkConnectionStatus()
        initViewProtection()
        initCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        checkConnectionStatus()
    }

    private fun checkConnectionStatus() {
        ConnectionGuard.decideToKillApp(this)
    }

    private fun initViewProtection() {
        // TODO: This is ugly. Remove! Make all BaseActivities have a BlurLayout or not?
        if (findViewById<View>(R.id.blurlayout) != null) {
            blurLayout = findViewById(R.id.blurlayout)
            blurLayout.setBlurDuration(200)
            blurLayout.setBlurRadius(25)
            blurLayout.enableTouchEvent(false)

            hover = LayoutInflater.from(this).inflate(R.layout.view_blur, null)
            blurLayout.setHoverView(hover)
            blurLayout.addChildAppearAnimator(hover, R.id.blurLayout, Techniques.FadeIn)
            blurLayout.addChildDisappearAnimator(hover, R.id.blurLayout, Techniques.FadeOut)
            restart()
        }
    }

    private fun restart() {
        if (inactivityHandler == null) {
            inactivityHandler = Handler()
        }
        inactivityHandler!!.removeCallbacks(handleInactivity)
        inactivityHandler!!.postDelayed(handleInactivity, TIME_TO_WAIT)
    }

    private fun blurHackForTextViews(viewGroup: ViewGroup, blur: Boolean) {
        val count = viewGroup.childCount
        for (i in 0 until count) {
            val view = viewGroup.getChildAt(i)
            if (view is ViewGroup)
                blurHackForTextViews(view, blur)
            else if (view is TextView) {
                if (blur) {
                    blurTextView(view)
                } else {
                    unblurTextView(view)
                }
            }
        }
    }

    private fun blurTextView(textView: TextView) {
        textView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        val radius = 25f
        val filter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
        textView.paint.maskFilter = filter

        // FUCKing undeeded hacky hack to unblur hover view....
        val viewNotToBlur = findViewById<TextView>(R.id.blurView)
        if (textView == viewNotToBlur) {
            unblurTextView(viewNotToBlur)
        }
    }

    // https://stackoverflow.com/questions/49774819/how-to-undo-a-mask-filter-blur-on-a-textview-in-android
    private fun unblurTextView(textView: TextView) {
        textView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        textView.paint.maskFilter = null
    }

    @SuppressLint("RestrictedApi")
    private fun initCamera() {
        if (!isFaceDetectionPossible()) {
            return
        }
        // Init FaceRecognition. Needed to avoid crashes on SDK-19 https://stackoverflow.com/questions/56374939/android-application-with-camera2-library-crash-on-start-for-sdk19
        CameraX.init(applicationContext, Camera2AppConfig.create(applicationContext))

        // https://github.com/asmaamirkhan/MLKitDemo/blob/master/app/src/main/java/com/asmaamir/mlkitdemo/FaceTracking/FaceTrackingActivity.java
        val lens: CameraX.LensFacing = CameraX.LensFacing.FRONT
        CameraX.unbindAll()
        val pc = PreviewConfig.Builder()
                .setLensFacing(lens)
                .build()
        val preview = Preview(pc)
        val icc: ImageCaptureConfig =
                ImageCaptureConfig.Builder()
                        .setLensFacing(lens)
                        .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                        .build()
        val imgCap = ImageCapture(icc)
        val iac: ImageAnalysisConfig = ImageAnalysisConfig.Builder()
                .setLensFacing(lens)
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_NEXT_IMAGE)
                .build()
        val imageAnalysis = ImageAnalysis(iac)
        imageAnalysis.setAnalyzer({ obj: Runnable -> obj.run() }, FaceTrackingAnalyzer(handleInactivity, this))
        CameraX.bindToLifecycle(this, imgCap, preview, imageAnalysis)
    }

    @SuppressLint("RestrictedApi")
    private fun isFaceDetectionPossible (): Boolean {
        // TODO: Remove try catch when Storage is not onlineStorage in onPause anymore in e2e tests..
        try {
            val naturalPreconditionMet = IOHelper.isCameraPermissionGranted(this) // Camera permissions not granted
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP // Camera Sdk not supported
                    && Storage.readSettings().screenSaver // Save energy.
            if (!naturalPreconditionMet) {
                return false
            } else {
                // For some reason github emulator has camera permissions granted..
                // java.lang.IllegalArgumentException: Fail to find supported surface info - CameraId:null
                // https://stackoverflow.com/questions/58968106/fail-to-find-supported-surface-info-cameraidnull-camerax-on-android-things
                return try {
                    CameraX.init(applicationContext, Camera2AppConfig.create(applicationContext))
                    Preview(PreviewConfig.Builder()
                            .setLensFacing(CameraX.LensFacing.FRONT)
                            .build())
                    true
                } catch (e: IllegalArgumentException) {
                    CameraX.unbindAll()
                    Log.e(javaClass.simpleName, e.message, e)
                    false
                }
            }
        } catch (e: Exception) {
            // TODO:
            return false
        }
    }
}