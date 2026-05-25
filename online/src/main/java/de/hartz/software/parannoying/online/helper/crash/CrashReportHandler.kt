package de.hartz.software.parannoying.online.helper.crash


import android.content.Context
import android.content.Intent
import android.util.Log
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.core.model.domain.CrashLog
import de.hartz.software.parannoying.core.model.exceptions.DebuggingPurposeException
import de.hartz.software.parannoying.online.model.OnlineStorage
import org.acra.ACRA
import org.acra.ReportField
import org.acra.config.CoreConfigurationBuilder
import org.acra.config.HttpSenderConfigurationBuilder
import org.acra.data.CrashReportData
import org.acra.data.StringFormat
import org.acra.sender.HttpSender
import org.acra.sender.HttpSenderFactory
import org.acra.sender.ReportSender
import org.acra.sender.ReportSenderException


class CrashReportHandler(val myBuildConfigClass: Class<*>) : ReportSender {

    /*
     * https://stackoverflow.com/a/52377031/8524651
     */
    @Throws(ReportSenderException::class)
    override fun send(context: Context, errorContent: CrashReportData) {
        ACRA.errorReporter.putCustomData("CrashReportHandlerStarted ${System.currentTimeMillis()}", "")

        val errorString = errorContent.toJSON()

        try {
            context.app.Storage.addCrashlog(CrashLog(errorString, IOHelper.getCurrentDateAsUnixTimestamp()))
        } catch (e: UnsupportedOperationException) {
            Log.e(this.javaClass.simpleName, "Crashlog storing not supported so far")
        }


        Log.e("CrashReportHandler", "" + errorContent.getString(ReportField.STACK_TRACE))
        // If the app is in debugging mode and the error wasnt forced to debug this handler then return
        val settings = (context.app.Storage as OnlineStorage).readSettings()
        if (settings.hiddenSettings.developerMode
            && !errorContent.getString(ReportField.STACK_TRACE)!!
                .contains(DebuggingPurposeException::class.java.simpleName)) {
            Log.e("CrashReportHandler", "Exception not handled.")
            return
        }

        try {
            Log.v("CrashReportHandler", "Exception start handling. Is offline device " + context.app.Storage.isOfflineDevice())
            if (settings.reportErrorOnline) {
                transmitErrorOnline(context, errorContent)
            }
            Log.v("CrashReportHandler", "Exception successfully handled.")
        } catch (reportSendingError: ReportSenderException) {
            Log.e("CrashReportHandler", "Exception handling throwed reportSendingError.", reportSendingError)
            throw reportSendingError
        } catch (e: Exception) {
            UiHelper.showToastFromBackgroundTask(context, "It looks like the crash report handling crashed. Please provide feedback in an other way!")
            Log.e("CrashReportHandler", "Exception loop happend", e)
            ACRA.errorReporter.putCustomData("CrashReportHandler Looped ${System.currentTimeMillis()}", "")
        }
    }

    private fun transmitErrorOnline(context: Context, errorContent: CrashReportData){
        val builder = CoreConfigurationBuilder().withReportFormat(StringFormat.JSON).withBuildConfigClass(myBuildConfigClass)
        val configration = HttpSenderConfigurationBuilder().withUri("https://collector.tracepot.com/6b11f1cb")
            .withHttpMethod(HttpSender.Method.PUT)
            .withEnabled(true)
            .build()

        builder.withPluginConfigurations(configration)

        /* // TODO: Might be useful as well.
        builder.getPluginConfigurationBuilder(SchedulerConfigurationBuilder::class.java)
                .setRequiresNetworkType(JobRequest.NetworkType.UNMETERED)
                .setRequiresBatteryNotLow(true)
                .setEnabled(true)
        builder.getPluginConfigurationBuilder(LimiterConfigurationBuilder::class.java)
                .setEnabled(true)
        */
        HttpSenderFactory().create(context, builder.build()).send(context, errorContent)
    }

    /*private fun putField(fieldMap: HashMap<String, String>, errorContent: CrashReportData, field: ReportField) {
        fieldMap[field.toString()] = errorContent.getString(field)
    }*/

    // TODO: Maybe use email as an option?
    private fun sendViaEmail(context: Context, errorString: String) {
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.setType("plain/text")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("hartzkai@googlemail.com"))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                "ParAnnoying Crashreport")
        emailIntent.putExtra(Intent.EXTRA_TEXT, errorString)

        context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    /*private fun buildSubjectBody(context: Context, errorContent: CrashReportData): Array<String> {
        var fields = this.config.getReportFields()
        if (fields.isEmpty()) {
            fields = ImmutableSet<ReportField>(ACRAConstants.DEFAULT_MAIL_REPORT_FIELDS)
        }

        var subject = context.packageName + " Crash Report"
        val builder = StringBuilder()
        val var4 = fields.iterator()

        while (var4.hasNext()) {
            val field = var4.next() as ReportField
            builder.append(field.toString()).append('=')
            builder.append(errorContent.get(field))
            builder.append('\n')
            if ("STACK_TRACE" == field.toString()) {
                val stackTrace = errorContent.get(field)
                if (stackTrace != null) {
                    subject = (context.packageName + ": "
                            + stackTrace.substring(0, stackTrace.indexOf('\n')))
                    if (subject.length > 72) {
                        subject = subject.substring(0, 72)
                    }
                }
            }
        }

        return arrayOf(subject, builder.toString())
    }*/
}