package de.hartz.software.parannoying.app

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.Log
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.github.anrwatchdog.ANRError
import com.github.anrwatchdog.ANRWatchDog
import com.github.omadahealth.lollipin.lib.managers.LockManager
import de.hartz.software.parannoying.air.gap.interfaces.di.ActivitySubComponents
import de.hartz.software.parannoying.app.helper.ShortcutHelper
import de.hartz.software.parannoying.app.helper.provider.ActivityModules
import de.hartz.software.parannoying.app.helper.provider.AppModules
import de.hartz.software.parannoying.app.interfaces.ApplicationComponent
import de.hartz.software.parannoying.app.interfaces.DaggerActivityComponent
import de.hartz.software.parannoying.app.interfaces.DaggerApplicationComponent
import de.hartz.software.parannoying.core.activities.CustomPinActivity
import de.hartz.software.parannoying.core.activities.insecured.SplashActivity
import de.hartz.software.parannoying.core.helper.InitializationHelper
import de.hartz.software.parannoying.core.helper.development.DevelopmentUtil
import de.hartz.software.parannoying.core.helper.io.IOHelper.startDisabledService
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.NotificationHelper
import de.hartz.software.parannoying.core.interfaces.di.CoreComponents
import de.hartz.software.parannoying.core.interfaces.di.StorageInterface
import de.hartz.software.parannoying.core.interfaces.di.air.gap.AirGapAdapter
import de.hartz.software.parannoying.core.model.domain.DeviceRole
import de.hartz.software.parannoying.offline.helper.guard.internet.NetworkSchedulerService
import de.hartz.software.parannoying.offline.helper.guard.usb.UsbSchedulerService
import de.hartz.software.parannoying.offline.helper.worker.CleartextMessageProcessingWorkerFactory
import de.hartz.software.parannoying.offline.interfaces.OfflineApplication
import de.hartz.software.parannoying.offline.interfaces.OfflineComponents
import de.hartz.software.parannoying.online.activities.online.OnlineSettingsActivity
import de.hartz.software.parannoying.online.interfaces.OnlineApplication
import de.hartz.software.parannoying.online.interfaces.OnlineComponents
import org.acra.ACRA
import org.acra.config.CoreConfigurationBuilder
import javax.inject.Inject

// https://developer.android.com/studio/build/multidex.html
class App : MultiDexApplication(), Application.ActivityLifecycleCallbacks, OnlineApplication, OfflineApplication {

    //  https://developer.android.com/training/dependency-injection/dagger-multi-module
    lateinit var appComponent: ApplicationComponent
    override lateinit var coreComponents: CoreComponents
    override lateinit var offlineComponents: OfflineComponents
    override var currentActivity: Activity? = null
    override lateinit var onlineComponents: OnlineComponents

    @Inject override lateinit var Storage: StorageInterface<*, *>
    @Inject override lateinit var airGapAdapter: AirGapAdapter
    @Inject lateinit var securityInterfaceHolder: SecurityInterfaceHolder

    private lateinit var appModules: AppModules


    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // Set as early as possible to not have mixed results..
        DevelopmentUtil.init(this)
        MultiDex.install(this)
        initApp()
    }

    override fun onCreate() {
        super.onCreate()
        CleartextMessageProcessingWorkerFactory.initializeWorkManager(this, securityInterfaceHolder)
    }

    @Synchronized override fun initApp() {
        initDagger()
        initialize()
    }

    override fun invalidateComponents() {
        appModules = AppModules(this)
        appComponent = DaggerApplicationComponent.builder()
                .appModules(appModules)
                .build()
        appComponent.inject(this)
        coreComponents = appComponent.coreComponents().create()
        offlineComponents = appComponent.offlineComponent().create()
        onlineComponents = appComponent.onlineComponents().create()
        initDeviceListeners()
    }

    override fun isDebugMode(): Boolean {
        // https://medium.com/mobile-app-development-publication/checking-debug-build-the-right-way-d12da1098120
        return ((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0)
    }

    override fun getActivityComponents(activity: Activity): ActivitySubComponents {
        val activityComponents = DaggerActivityComponent.builder()
                .appModules(appModules)
                .activityModules(ActivityModules(activity))
                .build()
        return activityComponents.activityComponents().create(activity)
    }

    private fun initDeviceListeners() {
        if (Storage.isOfflineDevice()) {
            initOffline()
        } else if (Storage.isOnlineDevice()) {
            initOnline()
        } else {
            ShortcutHelper.init(null, this)
        }
    }

    private fun initDagger () {
        // Reset storage to volatile storage when onboarding didnt finished.
        if (!InitializationHelper.isInitialized(this)) {
            InitializationHelper.resetDeviceRole(this)
        }
        invalidateComponents()
    }

    private fun initOffline () {
        // When running in test avoid starting services to avoid concurrent fails with realm.
        if (DevelopmentUtil.isRunningTest()) {
            return
        }
        // Init Network checker.
        startDisabledService(UsbSchedulerService::class.java, this)
        startDisabledService(NetworkSchedulerService::class.java, this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            NetworkSchedulerService.scheduleJob(this)
            UsbSchedulerService.scheduleJob(this)
        }
        ShortcutHelper.init(DeviceRole(DeviceRole.OFFLINE), this)
    }

    private fun initOnline () {
        ShortcutHelper.init(DeviceRole(DeviceRole.ONLINE), this)
    }

    private fun initialize () {
        IconHelper.initIcons(this)

        showDeveloperNotifications()

        initCrashHandler()

        // Enable pin protection.
        val lockManager = LockManager.getInstance()
        lockManager.enableAppLock(this, CustomPinActivity::class.java)

        // Currently only needed for lollipin.
        registerActivityLifecycleCallbacks(this)
    }

    private fun initCrashHandler () {
        // Init error handling.
        if (Storage.DEVELOPER_MODE) {
            ACRA.DEV_LOGGING = true
        }

        // reportSenderFactoryClasses = [CrashReportFactory::class]
        ACRA.init(this, CoreConfigurationBuilder()
            .withBuildConfigClass(BuildConfig::class.java)
            .withSendReportsInDevMode(true)
            .build())

        // TODO: We need such an instance for every PROCESS, do Services run in the same process?
        ANRWatchDog(2000)
                .setIgnoreDebugger(Storage.DEVELOPER_MODE) // TODO: This does interrupt the debugger..
                // .setReportMainThreadOnly() // TODO: Do we need to filter for specific threads?
                .setANRInterceptor(object: ANRWatchDog.ANRInterceptor {
                    override fun intercept(duration: Long): Long {
                        val ret = 5000 - duration
                        if (ret > 0) {
                            Log.w(javaClass.simpleName, "Intercepted ANR that is too short (" + duration + " ms), postponing for " + ret + " ms.");
                        }
                        return ret
                    }
                })
                .setANRListener(object : ANRWatchDog.ANRListener {
                    override fun onAppNotResponding(error: ANRError?) {
                        // TODO: Handle the error. For example, log it to Acra:
                        // ExceptionHandler.saveException(error, CrashManager())
                        Log.e("App", "Logged ANR" + error?.localizedMessage)
                    }
                })
                .start()
    }

    private fun showDeveloperNotifications () {
        // Developer Mode Hints:
        if (Storage.readSettings().hiddenSettings.developerMode) {
            // https://stackoverflow.com/a/64929520/8524651
            StrictMode.setVmPolicy(VmPolicy.Builder()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build())
            NotificationHelper.showPermanentNotification(this, "Developer Mode is enabled. The device might be connected to the internet and observed.", 2, OnlineSettingsActivity::class.java)
        }

    }

    override fun onLowMemory() {
        super.onLowMemory()
        ACRA.errorReporter.putCustomData("App onLowMemory ${System.currentTimeMillis()}", "")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        ACRA.errorReporter.putCustomData("App onTrimMemory ${System.currentTimeMillis()}", "Level: " + level)
    }

    // https://www.brightec.co.uk/blog/pin-protection-your-app-using-lollipin
    override fun onActivityPaused(activity: Activity) {
        if (activity is SplashActivity) {
            return
        }
        // Avoid showing the pinactivity to often.
        LockManager.getInstance().appLock.setLastActiveMillis()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
        // must be called in on resume to call on activity result in proper activity.
        currentActivity = activity
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }
}
