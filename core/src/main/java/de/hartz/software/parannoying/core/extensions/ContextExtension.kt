package de.hartz.software.parannoying.core.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import de.hartz.software.parannoying.core.interfaces.AbstractApp

val ExtensionPathAsString = "de.hartz.software.parannoying.core.extensions.ContextExtensionKt"
val Context.app: AbstractApp get() = applicationContext as AbstractApp

inline fun <reified T : Activity> Context.launchActivity(
        options: Bundle? = null,
        noinline init: Intent.() -> Unit = {}) {

    val intent = newIntent<T>(this)
    intent.init()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        startActivity(intent, options)
    } else {
        startActivity(intent)
    }
}


inline fun <reified T : Any> Context.launchActivity(
        options: Bundle? = null,
        intent: Intent) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        startActivity(intent, options)
    } else {
        startActivity(intent)
    }
}