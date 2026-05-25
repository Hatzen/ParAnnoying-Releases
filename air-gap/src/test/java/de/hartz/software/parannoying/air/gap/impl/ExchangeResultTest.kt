package de.hartz.software.parannoying.air.gap.impl

import de.hartz.software.parannoying.air.gap.model.ExchangeResultImpl
import de.hartz.software.parannoying.air.gap.model.UseCases
import de.hartz.software.parannoying.core.interfaces.di.air.gap.ExchangeDataWrapper
import org.junit.Assert
import org.junit.Test

class ExchangeResultTest {

    @Test
    fun testGetUseCase() {
        val useCase = UseCases.CLEARTEXT_FILE_RECEIVE

        var objectUnderTest = ExchangeResultImpl(useCase.requestCode, listOf<ExchangeDataWrapper>().asSequence(), false)

        val result = objectUnderTest.getUseCase()

        Assert.assertEquals(result, useCase)
    }

    @Test
    fun testGetSyncReceive() {
        val useCase = UseCases.Offline.MESSAGES_SYNC

        var objectUnderTest = ExchangeResultImpl(useCase.receiveLaunchOptions.requestCode, null, true)

        val result = objectUnderTest.getUseCase()

        Assert.assertEquals(result, useCase.receiveLaunchOptions)
    }

}