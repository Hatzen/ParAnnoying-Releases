package de.hartz.software.parannoying.app.helper.provider

import android.util.Log
import dagger.Module
import dagger.Provides
import de.hartz.software.parannoying.air.gap.impl.AirGapAdapterImpl
import de.hartz.software.parannoying.air.gap.impl.ChannelSupportAdapterImpl
import de.hartz.software.parannoying.app.App
import de.hartz.software.parannoying.core.BuildConfig
import de.hartz.software.parannoying.core.activities.insecured.welcome.WelcomeActivity
import de.hartz.software.parannoying.core.helper.InitializationHelper
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.interfaces.di.ActivityProvider
import de.hartz.software.parannoying.core.interfaces.di.ApplicationInfoComponent
import de.hartz.software.parannoying.core.interfaces.di.StorageInterface
import de.hartz.software.parannoying.core.interfaces.di.air.gap.AirGapAdapter
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ChannelSupportAdapter
import de.hartz.software.parannoying.core.interfaces.di.security.AsymmetricEncryptionHelper
import de.hartz.software.parannoying.core.interfaces.di.security.CompressionHelper
import de.hartz.software.parannoying.core.interfaces.di.security.DataConverter
import de.hartz.software.parannoying.core.interfaces.di.security.HMACHelper
import de.hartz.software.parannoying.core.interfaces.di.security.HardcodedEncryptionHelper
import de.hartz.software.parannoying.core.interfaces.di.security.HashHelper
import de.hartz.software.parannoying.core.interfaces.di.security.RandomHelper
import de.hartz.software.parannoying.core.interfaces.di.security.SymmetricEncryptionHelper
import de.hartz.software.parannoying.core.model.VolatileStorage
import de.hartz.software.parannoying.core.model.persistence.realm.RealmHelper
import de.hartz.software.parannoying.fake.entry.FakeEntryActivity
import de.hartz.software.parannoying.offline.helper.OfflineActivityProvider
import de.hartz.software.parannoying.offline.helper.crash.CrashReportHandler
import de.hartz.software.parannoying.offline.interfaces.OfflineComponents
import de.hartz.software.parannoying.offline.model.OfflineStorage
import de.hartz.software.parannoying.offline.model.RealmOfflinePersistenceConfiguration
import de.hartz.software.parannoying.online.interfaces.OnlineComponents
import de.hartz.software.parannoying.online.model.OnlineStorage
import de.hartz.software.parannoying.online.model.RealmOnlinePersistenceConfiguration
import org.acra.sender.ReportSender
import javax.inject.Singleton

@Module(subcomponents = [OfflineComponents::class, OnlineComponents::class])
class AppModules(private val application: App) {

    @Provides
    @Singleton
    fun providesApplication(): App = application

    @Provides
    @Singleton
    fun storage(realmhelper: RealmHelper): StorageInterface<*, *> {
        if (InitializationHelper.isOfflineDevice(application)) {
            return OfflineStorage(realmhelper)
        } else if (InitializationHelper.isOnlineDevice(application)) {
            return OnlineStorage(realmhelper)
        }
        return VolatileStorage
    }

    @Provides
    fun realmhelper(context: App, securityInterfaceHolder: SecurityInterfaceHolder): RealmHelper {
        if (InitializationHelper.isOfflineDevice(application)) {
            return RealmHelper(securityInterfaceHolder, context, RealmOfflinePersistenceConfiguration())
        } else if (InitializationHelper.isOnlineDevice(application)) {
            return RealmHelper(securityInterfaceHolder, context, RealmOnlinePersistenceConfiguration())
        }
        return RealmHelper(securityInterfaceHolder, context, RealmOnlinePersistenceConfiguration())
    }

    @Provides
    public fun activityProvider(): ActivityProvider {
        val onboardingActivityProvider = object: ActivityProvider(FakeEntryActivity::class.java) {
            override fun getStartActivityClass(): Class<*> {
                return WelcomeActivity::class.java
            }

            override fun getWelcomeActivityClass(): Class<*> {
                return WelcomeActivity::class.java
            }

            override fun getHiddenSettingsActivityClass(): Class<*> {
                throw RuntimeException("Dummy should not have any hiddensettings")
            }
        }
        if (InitializationHelper.isOfflineDevice(application)) {
            return OfflineActivityProvider(FakeEntryActivity::class.java)
        } else if (InitializationHelper.isOnlineDevice(application)) {
            return de.hartz.software.parannoying.online.helper.OnlineActivityProvider(FakeEntryActivity::class.java)
        }
        return onboardingActivityProvider
    }

    @Provides
    fun reportSender(storage :StorageInterface<*, *>): ReportSender {
        if (storage.isOnlineDevice()) {
            // TODO: Why does ...app.BuildConfig does not exist, can we use core without any problems?
            return de.hartz.software.parannoying.online.helper.crash.CrashReportHandler(BuildConfig::class.java)
        }
        return CrashReportHandler()
    }

    @Provides
    fun applicationInfoComponent(context: App): ApplicationInfoComponent {
        return object: ApplicationInfoComponent  {
            override fun getVersion(): String {
                // Try catch needed since updating gradle and kotlin version
                try {
                    val versionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName
                    val versionCode = context.packageManager.getPackageInfo(context.packageName, 0).versionCode
                    return "$versionName-$versionCode"
                } catch(e: Exception) {
                    // https://github.com/robolectric/robolectric/issues/4921
                    Log.v("ApplicationInfoComponen", "failed getting app version. Caused by test environment...")
                    return "-1"
                }
            }

        }
    }

    @Provides
    fun airGapAdapter(context: App): AirGapAdapter {
        return AirGapAdapterImpl(context, context)
    }

    @Provides
    fun channelSupportChecker(context: App): ChannelSupportAdapter {
        return ChannelSupportAdapterImpl(context)
    }

    @Provides
    fun securityHolder(asymmetricEncryptionHelper: AsymmetricEncryptionHelper,
                       CompressionHelper: CompressionHelper,
                       dataConverter: DataConverter,
                       hardcodedEncryptionHelper: HardcodedEncryptionHelper,
                       hashHelper: HashHelper,
                       hmacHelper: HMACHelper,
                       randomHelper: RandomHelper,
                       symmetricEncryptionHelper: SymmetricEncryptionHelper
    ): SecurityInterfaceHolder {
        return SecurityInterfaceHolder(asymmetricEncryptionHelper,
            CompressionHelper,
            dataConverter,
            hardcodedEncryptionHelper,
            hashHelper,
            hmacHelper,
            randomHelper,
            symmetricEncryptionHelper)
    }
}