package de.hartz.software.parannoying.core.adapter

import android.content.Context
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.interfaces.di.air.gap.AirGapAdapter
import de.hartz.software.parannoying.core.model.domain.CrashLog
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test
import java.io.File

class CrashLogOverviewAdapterTest {

    val objectUnderTest: CrashLogOverviewAdapter
    val crashReport: String
    val formatted: String

    init {
        crashReport = File(getPath("CrashlogExample.txt")).readText()
        formatted = File(getPath("CrashlogExampleFormatted.txt")).readText()
        val list = listOf(CrashLog(crashReport, 1 , -1), CrashLog(formatted, 2, -1))
        objectUnderTest = CrashLogOverviewAdapter(
            list,
            mockk<Context>(),
            mockk<SecurityInterfaceHolder>(),
            mockk<AirGapAdapter>()
        )
    }

    @Test
    fun test() {

        // Mocking inflater is to hard..
        // val result = objectUnderTest.getView(0, null, mockk<ViewGroup>())
        // val crashlog = result.findViewById<TextView>(R.id.note).text
        val crashlog = objectUnderTest.extractLastStacktraceCausedByFromErrorJSON(formatted)

        // both seem ok.
        // val expected ="java.lang.NoClassDefFoundError: Failed resolution of: Landroidx\\/renderscript\\/RenderScript;\\n\\tat com.daimajia.androidviewhover.tools.Blur"
        val expected = "Caused by: java.lang.ClassNotFoundException: Didn't find class \\\"androidx.renderscript.RenderScript\\\" on path: DexPathList[[zip file \\\"\\/data\\/app\\/~ok5WxXc-yM6xpaa1KXwX4Q==\\/de.hartz.software.paranno"
        Assert.assertTrue("crashlog $crashlog \n did not contain \n expected $expected", crashlog.contains(expected))
    }


    private fun getPath(name: String): String {
        return File(javaClass.classLoader.getResource(name).toURI()).absolutePath
    }
}