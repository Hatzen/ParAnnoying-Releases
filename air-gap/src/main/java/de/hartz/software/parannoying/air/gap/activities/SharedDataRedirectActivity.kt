package de.hartz.software.parannoying.air.gap.activities

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.hartz.software.parannoying.air.gap.interfaces.di.ExchangeApp
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.interfaces.di.ActivityProvider
import javax.inject.Inject


// https://ebaytech.berlin/receiving-shared-content-on-android-823e9d1286ee
class SharedDataRedirectActivity : AppCompatActivity() {

    companion object {
        const val RESULT_INFO: Int = 516
        const val INTENT_TYPE_IMAGE_PREFIX = "image"
    }

    @Inject
    lateinit var activityProvider: ActivityProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        // TODO: Better use splash screen activity? So unified loading animation?

        super.onCreate(savedInstanceState)
        (app as ExchangeApp).getActivityComponents(this)
                .inject(this)
        init(intent)
    }

    private fun init(intent: Intent) {
        if (intent.hasExtra(Intent.EXTRA_STREAM)) {
            if (intent.type != null &&
                    intent.type!!.startsWith(INTENT_TYPE_IMAGE_PREFIX)) {
                handleIncomingImages(intent)
            } else {
                handleWrongMediaType()
            }
        }
        if (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED) {
            handleIncomingImages(intent)
            // NFCHelper.getContentFromIntent(intent)
        }
        finish()
    }

    // Use this together with "singleInstance" to avoid multiple instances
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        init(intent)
    }

    // https://code.tutsplus.com/tutorials/android-sdk-receiving-data-from-the-send-intent--mobile-14878
    private fun handleIncomingImages(intentWithData: Intent) {
        val welcomeClass = activityProvider.getStartActivityClass()
        val intent = Intent(this, welcomeClass)
        // Add extras photo etc to intent
        intent.putExtras(intentWithData)
        intent.type = intentWithData.type
        intent.action = intentWithData.action
        startActivity(intent)
    }

    private fun handleWrongMediaType() {
        Toast.makeText(this, "Wrong media type. Images only.", Toast.LENGTH_LONG).show()
    }

}