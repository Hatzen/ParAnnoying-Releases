package de.hartz.software.parannoying.offline.helper.crash


import android.content.Context
import android.content.Intent
import android.util.Log
import de.hartz.software.parannoying.air.gap.model.UseCases
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.helper.development.DevelopmentUtil
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.helper.ui.NotificationHelper
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.core.model.domain.CrashLog
import de.hartz.software.parannoying.core.model.exceptions.DebuggingPurposeException
import org.acra.ACRA
import org.acra.ReportField
import org.acra.data.CrashReportData
import org.acra.sender.ReportSender
import org.acra.sender.ReportSenderException
import org.json.JSONObject


class CrashReportHandler : ReportSender {

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

        Log.e("CrashReportHandler", "CrashReportHandler start: " + errorString)
        // If the app is in debugging mode and the error wasnt forced to debug this handler then return
        if (DevelopmentUtil.isRunningTest()
            && !errorContent.getString(ReportField.STACK_TRACE)!!
                .contains(DebuggingPurposeException::class.java.simpleName)) {
            Log.e("CrashReportHandler", "Exception not handled.")
            return
        }

        val deviceRole = context.app.Storage.isOfflineDevice()
        try {
            Log.v("CrashReportHandler", "Exception start handling. Is offline device " + deviceRole)
            transmitErrorOffline(context, errorContent)
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

    private fun transmitErrorOffline(context: Context, errorContent: CrashReportData){
        val minimalisticData = getMinimalErrorReportData(errorContent)

        val it = context.app.airGapAdapter.getSendIntent(
            UseCases.Offline.CRASH_REPORT_SEND.useText(minimalisticData)
        )
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK // Needed as started from context

        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.P) {
            // https://developer.android.com/guide/components/activities/background-starts#display-notification
            NotificationHelper.showCrashReportNotification(context, it)
        } else {
            context.startActivity(it) // TODO: Crashes within the activity wont be recognized.
        }
    }

    private fun getMinimalErrorReportData(errorContent: CrashReportData) : String {
        val fieldMap = HashMap<String, String>()

        putField(fieldMap, errorContent, ReportField.ANDROID_VERSION)
        // putField(fieldMap, errorContent, ReportField.APPLICATION_LOG) // Would be perfect...
        putField(fieldMap, errorContent, ReportField.STACK_TRACE)
        putField(fieldMap, errorContent, ReportField.APP_VERSION_CODE)
        putField(fieldMap, errorContent, ReportField.AVAILABLE_MEM_SIZE)
        putField(fieldMap, errorContent, ReportField.DEVICE_ID)
        putField(fieldMap, errorContent, ReportField.PHONE_MODEL)
        putField(fieldMap, errorContent, ReportField.USER_CRASH_DATE)

        return JSONObject(fieldMap as Map<*, *>).toString()
    }

    private fun putField(fieldMap: HashMap<String, String>, errorContent: CrashReportData, field: ReportField) {
        fieldMap[field.toString()] = errorContent.getString(field)!!
    }

}