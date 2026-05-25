package de.hartz.software.parannoying.core.activities

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import de.hartz.software.parannoying.core.R
import de.hartz.software.parannoying.core.interfaces.di.StorageInterface
import io.realm.exceptions.RealmMigrationNeededException
import javax.inject.Inject

class RealmMigrationActivity : AppCompatActivity() {
    @Inject
    lateinit var storage: StorageInterface<*, *>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_realm_migration)

        val startTime = System.currentTimeMillis()

        try {
            storage.runMigration()
        } catch (e: RealmMigrationNeededException) {
            // This should never happen here since we're migrating explicitly
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        // Ensure at least 4 seconds of display
        val delay = maxOf(4000 - (System.currentTimeMillis() - startTime), 0)
        Handler(Looper.getMainLooper()).postDelayed({
            setResult(Activity.RESULT_OK)
            finish()
        }, delay)
    }
}