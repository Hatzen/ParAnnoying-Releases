package de.hartz.software.parannoying.core.helper.development

import android.app.Application
import android.content.pm.ApplicationInfo
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Methods within should only be called from test code or debug app.
 */
object DevelopmentUtil {
    private var isRunningTest: AtomicBoolean? = null
    private var isTest = false
    private lateinit var app: Application

    fun init(app: Application) {
        this.app = app
    }

    fun deactivateApplicationCheckForUnitTests() {
        isTest = true
    }

    @Synchronized
    fun isRunningTest(): Boolean {
        if (null == isRunningTest) {
            val istest: Boolean
            istest = try {
                // "android.support.test.espresso.Espresso" if you haven't migrated to androidx yet
                Class.forName("androidx.test.espresso.Espresso")
                true
            } catch (e: ClassNotFoundException) {
                false
            }
            isRunningTest = AtomicBoolean(istest)
        }
        return isRunningTest!!.get()
    }

    fun isDebugMode(): Boolean {
        if (isTest) {
            return true
        }
        // https://medium.com/mobile-app-development-publication/checking-debug-build-the-right-way-d12da1098120
        return ((app.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0)
    }
}