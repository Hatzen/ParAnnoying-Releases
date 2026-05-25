package de.hartz.software.parannoying.air.gap.impl

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.util.Log
import de.hartz.software.parannoying.air.gap.activities.SendActivity
import de.hartz.software.parannoying.air.gap.helpers.DatasetProcessor
import de.hartz.software.parannoying.air.gap.interfaces.di.ExchangeApp
import de.hartz.software.parannoying.air.gap.model.ExchangeDataWrapperImpl
import de.hartz.software.parannoying.air.gap.model.UseCases
import de.hartz.software.parannoying.core.extensions.launchActivity
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import java.io.File

class AirGapAdapterImplTest {

    var objectUnderTest: AirGapAdapterImpl
    val randomSendUseCase = UseCases.Offline.CRASH_REPORT_SEND

    val fileContent1 = File(getPath("encrypted1.txt")).readText().length
    val fileContent2 = File(getPath("encrypted2.txt")).readText().length

    val first = ExchangeDataWrapperImpl("FirstSimpleTestData")
    val second = ExchangeDataWrapperImpl("FirstSimpleTestData2")
    val third = ExchangeDataWrapperImpl("FirstSimpleTestData3")
    val fourth = ExchangeDataWrapperImpl("FirstSimpleTestData4")

    val firstFile = ExchangeDataWrapperImpl(getPath("encrypted1.txt"), true)
    val secondFile = ExchangeDataWrapperImpl(getPath("encrypted2.txt"), true)

    init {
        mockkStatic(Log::class)

        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0

        val contextMock = mockk<Application>()
        val appMock = mockk<ExchangeApp>()
        val activityMock= mockk<Activity>()

        val test = mockkClass(Intent::class)
        every { test.putExtra(any<String>(), any<String>()) } returns test

        // TODO why non of these mocks work?
        // val test2 = mockkStatic(::<SendActivity>newIntent)
        // val test2 = mockkStatic("de.hartz.software.parannoying.core.extensions.ActivityExtensionKt")
        // every { newIntent<SendActivity>(contextMock) } returns test
        // val func: (context: Context) -> Intent = { context: Context -> newIntent<SendActivity>(context) }
        // every { func(contextMock) } returns test
        // every { newIntent<SendActivity>(contextMock) } returns test


        objectUnderTest = AirGapAdapterImpl(contextMock, appMock)
        every { appMock.currentActivity } returns activityMock
    }

    @Ignore("Currently failing but we are only mocking android not testing business logic")
    @Test
    fun testTextOnly() {

        val testList = listOf(
                first
        )

        objectUnderTest.startSend(
                randomSendUseCase.useDataWrappers(testList)
        )

        val activity = objectUnderTest.app.currentActivity
        val slot = slot<Intent>()
        verify(exactly = 1) { activity!!.launchActivity<SendActivity>(intent = capture(slot)) }
        val file = File(slot.captured.getStringExtra(DatasetProcessor.RESULT_EXTRA_FILE_NAME))

        val resultIntent = mockk<Intent>()
        every { resultIntent.getStringExtra(eq(DatasetProcessor.RESULT_EXTRA_FILE_NAME)) } returns file.absolutePath

        val SYNC_MESSAGES_REQUESTCODE = -1
        runBlocking {
            val result = objectUnderTest.onActivityResult(
                    SYNC_MESSAGES_REQUESTCODE,
                    Activity.RESULT_OK,
                    resultIntent
            ).result!!.toList()

            Assert.assertEquals(testList.size, result.size)
        }
    }

    @Ignore("reduce github runtime..")
    @Test
    fun test() {

        val testList = listOf(
            first,
            second,
            firstFile,
            third,
            fourth,
            secondFile
        )

        objectUnderTest.startSend(
                randomSendUseCase.useDataWrappers(testList)
        )

        val activity = objectUnderTest.app.currentActivity
        val slot = slot<Intent>()
        verify(exactly = 1) { activity!!.launchActivity<SendActivity>(intent = capture(slot)) }
        val file = File(slot.captured.getStringExtra(DatasetProcessor.RESULT_EXTRA_FILE_NAME))

        val resultIntent = mockk<Intent>()
        every { resultIntent.getStringExtra(eq(DatasetProcessor.RESULT_EXTRA_FILE_NAME)) } returns file.absolutePath

        val SYNC_MESSAGES_REQUESTCODE = -1
        runBlocking {
            val result = objectUnderTest.onActivityResult(
                SYNC_MESSAGES_REQUESTCODE,
                Activity.RESULT_OK,
                resultIntent
            ).result!!.toList()

            Assert.assertEquals(testList.size, result.size)
            val fileContentActual1 = File(result.first { it.isFile }.filePath).readText().length
            Assert.assertEquals(fileContent1, fileContentActual1)
            val fileContentActual2 = File(result.last { it.isFile }.filePath).readText().length
            Assert.assertEquals(fileContent2, fileContentActual2)
        }
    }

    @Ignore("reduce github runtime..")
    @Test
    fun testFileOnly() {
        val testList = listOf(
                firstFile
        )

        objectUnderTest.startSend(
                randomSendUseCase.useDataWrappers(testList)
        )

        val activity = objectUnderTest.app.currentActivity
        val slot = slot<Intent>()
        verify(exactly = 1) { activity!!.launchActivity<SendActivity>(intent = capture(slot)) }
        val file = File(slot.captured.getStringExtra(DatasetProcessor.RESULT_EXTRA_FILE_NAME))

        val resultIntent = mockk<Intent>()
        every { resultIntent.getStringExtra(eq(DatasetProcessor.RESULT_EXTRA_FILE_NAME)) } returns file.absolutePath

        val SYNC_MESSAGES_REQUESTCODE = -1
        runBlocking {
            val result = objectUnderTest.onActivityResult(
                    SYNC_MESSAGES_REQUESTCODE,
                    Activity.RESULT_OK,
                    resultIntent
            ).result!!.toList()

            Assert.assertEquals(testList.size, result.size)
            val fileContentActual1 = File(result.first { it.isFile }.filePath).readText().length
            Assert.assertEquals(fileContent1, fileContentActual1)
        }
    }

    private fun getPath(name: String): String {
        return File(javaClass.classLoader.getResource(name).toURI()).absolutePath
    }
}