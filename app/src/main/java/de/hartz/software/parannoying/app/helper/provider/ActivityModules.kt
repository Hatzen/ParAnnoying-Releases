package de.hartz.software.parannoying.app.helper.provider

import android.app.Activity
import android.util.Log
import dagger.Module
import dagger.Provides
import de.hartz.software.parannoying.core.helper.onboarding.Introducer
import de.hartz.software.parannoying.core.interfaces.di.StorageInterface
import de.hartz.software.parannoying.offline.helper.onboarding.OfflineIntroducer
import de.hartz.software.parannoying.online.helper.onboarding.OnlineIntroducer

// https://stackoverflow.com/a/45774582/8524651
@Module(includes = [AppModules::class, SecurityModule::class])
class ActivityModules(private val activity: Activity) {

    @Provides
    fun introducer(storage : StorageInterface<*, *>): Introducer {
        if (storage.isOnlineDevice()) {
            return OnlineIntroducer(activity)
        } else if (storage.isOfflineDevice()) {
            return OfflineIntroducer(activity)
        }
        return object : Introducer(activity) {
            override fun init(showForActivity: SpecificContentForActivity?) {
                Log.e(javaClass.simpleName, "Devicerole not set but introducer is required.")
            }
        }
    }

}