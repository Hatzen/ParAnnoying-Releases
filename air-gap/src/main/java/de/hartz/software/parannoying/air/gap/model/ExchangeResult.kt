package de.hartz.software.parannoying.air.gap.model

import de.hartz.software.parannoying.core.interfaces.di.air.gap.ExchangeDataWrapper
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ExchangeResult
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ILaunchOptions
import de.hartz.software.parannoying.core.interfaces.di.air.gap.SyncLaunchOptions
import kotlin.reflect.full.memberProperties

class ExchangeResultImpl(
        override val requestCode: Int,
        override val result: Sequence<ExchangeDataWrapper>?,
        override val success: Boolean): ExchangeResult {


    override fun getSingleResult(): ExchangeDataWrapper {
        return result!!.single()
    }

    override fun matchesUseCase(options: ILaunchOptions): Boolean {
        return options.requestCode == requestCode
    }

    override fun getUseCase(): ILaunchOptions? {
        if (!success) {
            return null
        }
        // TODO: this sort of reflection probably lead to issues with proguard so replace with simple list.
        //    as well as split up usage of Usecases for online and offline..
        return listOf(
            UseCases,
            UseCases.Offline,
            UseCases.Online
        ).flatMap { container ->
            container::class.memberProperties
                .mapNotNull { prop ->
                    val sync = prop.getter.call(container) as? SyncLaunchOptions
                    if (sync != null) {
                        if (matchesUseCase(sync.receiveLaunchOptions)) {
                            return@mapNotNull sync.receiveLaunchOptions
                        }
                        if (matchesUseCase(sync.sendLaunchOptions)) {
                            return@mapNotNull sync.sendLaunchOptions
                        }
                    }

                    val result = prop.getter.call(container) as? ILaunchOptions
                    if (result != null && matchesUseCase(result)) {
                        return@mapNotNull result
                    }

                    return@mapNotNull null
                }
        }.firstOrNull()
    }
}
