package de.hartz.software.parannoying.core.activities.insecured

import android.app.Activity
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import de.hartz.software.parannoying.core.R
import de.hartz.software.parannoying.core.activities.RealmMigrationActivity
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.core.interfaces.di.ActivityProvider
import de.hartz.software.parannoying.core.interfaces.di.StorageInterface
import javax.inject.Inject

// TODO: Use new android splash screen api https://developer.android.com/develop/ui/views/launch/splash-screen
class SplashActivity: AppCompatActivity() {

    @Inject lateinit var activityProvider: ActivityProvider
    @Inject lateinit var storageInterface: StorageInterface<*, *>

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val window = window
        window.setFormat(PixelFormat.RGBA_8888)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        // TODO: we should probably call creating application componentes like storage etc. here in async and not just wait..
        app.coreComponents.inject(this)
        setContentView(de.hartz.software.parannoying.core.R.layout.activity_splash)
        initAnimation()
    }

    private fun initAnimation() {
        val imageView = findViewById<ImageView>(R.id.animation_view)
        val drawable = imageView.drawable
        if (drawable is Animatable) {
            // TODO: Animation is only correct on lollipop (on other platforms it is reversed). But it looks ok everywhere.
            (drawable as Animatable).start()
        }

        if (storageInterface.isMigrationNeeded()) {
            val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // Migration successful, continue
                    routeToAppropriateActivity()
                    finish()
                } else {
                    // Handle failure or cancellation
                    UiHelper.showToastFromBackgroundTask(this, "Migration failed, please try to update or reset your app.")
                    finish()
                }
            }
            val intent = Intent(this, RealmMigrationActivity::class.java)
            launcher.launch(intent)
            return
        }
        var timeout = 2000L
        // If debug skip intro.
        if (Storage.readSettings().hiddenSettings.developerMode) {
            // Can be reduced to speed up tests etc.
            timeout = 1000
        }

        // Wait til animation finished.
        Handler().postDelayed({
            routeToAppropriateActivity()
            finish()
        }, timeout)
    }

    private fun routeToAppropriateActivity() {
        val intent = Intent(this, StartupRedirectActivity::class.java)
        startActivity(intent)
    }
}