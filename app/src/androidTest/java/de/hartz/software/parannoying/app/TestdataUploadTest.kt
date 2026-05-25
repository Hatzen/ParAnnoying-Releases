package de.hartz.software.parannoying.app

import android.content.Context
import androidx.test.InstrumentationRegistry
import de.hartz.software.parannoying.core.interfaces.DevelopmentOnly
import de.hartz.software.parannoying.app.large.tests.utils.DevelopmentFileUploader
import org.junit.Assume
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

@DevelopmentOnly
class TestdataUploadTest {

    lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getTargetContext()
    }

    @Test
    fun connectToLocalhostServer() {
        val isExportServerRunning = androidx.test.platform.app.InstrumentationRegistry.getArguments().getBoolean("uploadTestFiles")
        Assume.assumeTrue("Only needed when running manually with python server", isExportServerRunning )
        DevelopmentFileUploader().test()
    }
}
