package de.hartz.software.parannoying.core.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import de.hartz.software.parannoying.core.interfaces.AbstractApp
import de.hartz.software.parannoying.core.interfaces.di.StorageInterface

/**
 * Extensions for simpler launching of Activities
 */
// https://gist.github.com/passsy/3e6a12150af02120f8c6c156100277cc
inline fun <reified T : Activity> Activity.launchActivity(
        requestCode: Int = -1,
        options: Bundle? = null,
        noinline init: Intent.() -> Unit = {}) {

    val intent = newIntent<T>(this)
    intent.init()
    launchActivity<T>(requestCode, options, intent)
}

inline fun <reified T : Any> Activity.launchActivity(
        requestCode: Int = -1,
        options: Bundle? = null,
        intent: Intent) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        startActivityForResult(intent, requestCode, options)
    } else {
        startActivityForResult(intent, requestCode)
    }
}


val Activity.app: AbstractApp get() = application as AbstractApp

var Activity.Storage: StorageInterface<*, *>
    get() = app.Storage
    private set(value) { /* do something */ }

inline fun <reified T : Activity> newIntent(context: Context): Intent =
        Intent(context, T::class.java)

/**
 * Store custom chile beneath main enties
 */
fun Activity.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}