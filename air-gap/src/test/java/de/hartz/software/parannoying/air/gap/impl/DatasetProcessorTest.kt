package de.hartz.software.parannoying.air.gap.impl

import android.util.Log
import de.hartz.software.parannoying.air.gap.helpers.DatasetProcessor
import de.hartz.software.parannoying.air.gap.model.ExchangeDataWrapperImpl
import io.mockk.every
import io.mockk.mockkStatic
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert
import org.junit.Test
import java.io.File

class DatasetProcessorTest {

    lateinit var objectUnderTest: DatasetProcessor

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
    }

    @Test
    fun testTextOnly() {
        objectUnderTest = DatasetProcessor()

        val testList = listOf(
                first
        )

        val file = objectUnderTest.writeFile(testList)

        MatcherAssert.assertThat("processed item", objectUnderTest.textChunkCount == 1L)

        objectUnderTest = DatasetProcessor()
        val sequence = objectUnderTest.readFile(file.absolutePath)

        val list = sequence.toList()
        MatcherAssert.assertThat("processed item", objectUnderTest.textChunkCount, CoreMatchers.equalTo(1L))
        MatcherAssert.assertThat("read item", list, CoreMatchers.equalTo(testList))


        objectUnderTest = DatasetProcessor()
        val sequenceStrings = objectUnderTest.readFileChunks(file.absolutePath).toList()
        MatcherAssert.assertThat("processed item", objectUnderTest.textChunkCount,CoreMatchers.equalTo(1))
        MatcherAssert.assertThat("result size", sequenceStrings.size, CoreMatchers.equalTo(1))

        MatcherAssert.assertThat("read item", sequenceStrings[0], expect( first.exchangeData))
    }

    @Test
    fun testMixedData() {
        objectUnderTest = DatasetProcessor()

        val testList = listOf(
                first,
                firstFile,
                second,
                secondFile,
                third,
                fourth
        )


        val file = objectUnderTest.writeFile(testList)

        MatcherAssert.assertThat("processed item", objectUnderTest.textChunkCount, CoreMatchers.equalTo(120619L))

        objectUnderTest = DatasetProcessor()
        val sequence = objectUnderTest.readFile(file.absolutePath)

        val list = sequence.toList()
        // Takes ca. 30sec until here..
        MatcherAssert.assertThat("processed item", objectUnderTest.textChunkCount, CoreMatchers.equalTo(11616L))

        // Cannot equal as files differ
        //MatcherAssert.assertThat("read item", list, CoreMatchers.equalTo(testList))
        MatcherAssert.assertThat("read item", list.size, CoreMatchers.equalTo(testList.size))


        objectUnderTest = DatasetProcessor()
        val sequenceStrings = objectUnderTest.readFileChunks(file.absolutePath).toList()
        MatcherAssert.assertThat("processed item", objectUnderTest.textChunkCount, CoreMatchers.equalTo(11616L))
        MatcherAssert.assertThat("result size", sequenceStrings.size, CoreMatchers.equalTo(11616))

        MatcherAssert.assertThat("read item", sequenceStrings[0], expect(first.exchangeData))
        MatcherAssert.assertThat("read item", sequenceStrings.last(), expect(fourth.exchangeData))

    }

    private fun expect(data: String): Matcher<String> {
        return CoreMatchers.equalTo( data + DatasetProcessor.DATASET_SEPERATOR)
    }

    private fun getPath(name: String): String {
        return File(javaClass.classLoader.getResource(name).toURI()).absolutePath
    }
}