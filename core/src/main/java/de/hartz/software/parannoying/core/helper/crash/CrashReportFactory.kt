package de.hartz.software.parannoying.core.helper.crash

import android.content.Context
import com.google.auto.service.AutoService
import de.hartz.software.parannoying.core.extensions.app
import org.acra.config.CoreConfiguration
import org.acra.sender.ReportSender
import org.acra.sender.ReportSenderFactory
import javax.inject.Inject

@AutoService(ReportSenderFactory::class)
class CrashReportFactory : ReportSenderFactory {

    @Inject lateinit var reportSender: ReportSender

    override fun create(context: Context, config: CoreConfiguration): ReportSender {
        context.app.coreComponents.inject(this)
        return reportSender
    }

    override fun enabled(config: CoreConfiguration): Boolean {
        return true
    }
}