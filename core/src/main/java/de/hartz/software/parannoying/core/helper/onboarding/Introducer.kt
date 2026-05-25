package de.hartz.software.parannoying.core.helper.onboarding

import android.app.Activity
import android.os.Handler
import android.os.HandlerThread
import android.view.View
import android.view.ViewTreeObserver
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.extensions.toast
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.util.concurrent.CountDownLatch


abstract class Introducer(val activity: Activity) {

    enum class SpecificContentForActivity {
        ScanMessage,
        ScanNotification,
        ScanUserId
    }

    var currentStep = 0
    val waitForFinalLayoutLatch: CountDownLatch = CountDownLatch(1)

    lateinit var currentMethod: () -> Unit

    var listener = MaterialTapTargetPrompt.PromptStateChangeListener { prompt, state ->
        if (state == MaterialTapTargetPrompt.STATE_DISMISSED || state == MaterialTapTargetPrompt.STATE_FINISHED) {
            ++currentStep
            currentMethod()
            if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                activity.toast("Action currently skipped")
            }
        }
    }

    init {
        activity.findViewById<View>(android.R.id.content).getRootView()?.getViewTreeObserver()?.addOnGlobalLayoutListener(ViewTreeObserver.OnGlobalLayoutListener {
            //At this point the layout is complete and the
            //dimensions of myView and any child views are known.
            waitForFinalLayoutLatch.countDown()
        })
    }

    fun startIntroduction() {
        startIntroduction(null)
    }

    fun startIntroduction(showForActivity: SpecificContentForActivity?) {
        val thread = HandlerThread("")
        thread.start()
        Handler(thread.looper).post {
            run(showForActivity)
        }
    }

    private fun run(showForActivity: SpecificContentForActivity?) {
        if (activity.Storage.DEVELOPER_MODE) {
            // TODO: activity.Storage.DEVELOPER_MODE not considered properly
            // return
        }
        // Need to fix bug on android 8 where are small gap is on the bottom.
        waitForFinalLayoutLatch.await()

        activity.runOnUiThread {
            init(showForActivity)
        }
    }

    abstract fun init (showForActivity: SpecificContentForActivity?)

}