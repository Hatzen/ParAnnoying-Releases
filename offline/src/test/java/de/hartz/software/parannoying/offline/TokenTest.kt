package de.hartz.software.parannoying.offline

import android.content.Context
import de.hartz.software.parannoying.core.helper.development.DevelopmentUtil
import de.hartz.software.parannoying.core.interfaces.di.ApplicationInfoComponent
import de.hartz.software.parannoying.offline.businesslogic.TokenDeterminer
import de.hartz.software.parannoying.offline.model.domain.dialogs.CurrentUser
import de.hartz.software.parannoying.offline.model.domain.dialogs.SimpleDialog
import de.hartz.software.parannoying.offline.model.domain.dialogs.User
import de.hartz.software.parannoying.offline.model.domain.messages.AbstractMessage
import de.hartz.software.parannoying.offline.model.domain.messages.MetaData
import de.hartz.software.parannoying.offline.model.domain.messages.UserMessage
import de.hartz.software.parannoying.offline.model.domain.settings.OfflineSettings
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class TokenTest {

    var securityInterfaceHolder = TestSecurityInterfaceHolderProvider.getSecurityInterface()
    lateinit var settings: OfflineSettings
    lateinit var messages: MutableList<AbstractMessage>
    val contextMock = mockk<Context>()
    val applicationInfoComponentMock = object : ApplicationInfoComponent {
        override fun getVersion(): String {
            return "-1"
        }
    }

    @Before
    fun init() {
        DevelopmentUtil.deactivateApplicationCheckForUnitTests()
        settings = OfflineSettings()

        var counter = 0
        every { securityInterfaceHolder.randomHelper.computeRandomHashWithSpecificLength(any()) } answers {
            counter += 1
            "token$counter"
        }
        // Check mocking works as expected.
        Assert.assertEquals("token1", securityInterfaceHolder.randomHelper.computeRandomHashWithSpecificLength(12))
    }


    @Test
    fun testTokensGetProperlyRemovedWithMessagePersistingSimpleApprove() {
        val senderUser = User("dummyNicknameSender", "dummySenderHash")
        val currentUser = CurrentUser("dummyNickname", "dummyHash")
        val initialToken = "initialToken"
        senderUser.pushGeneratedToken(initialToken)

        val objectUnderTest = TokenDeterminer(securityInterfaceHolder)
        currentUser.pushGeneratedToken(initialToken)

        messages = mutableListOf()
        messages.add(getMessage(currentUser))
        messages.add(getMessage(currentUser))
        messages.add(getMessage(currentUser))

        messages.forEach {
            Assert.assertTrue("message ${it.metaData.newToken}",
                it.messageTokenSkipped == it.messageConfirmed && it.messageConfirmed == false)
        }

        val tokens = listOf("token4", "token3", "token2", "initialToken") // no message with initial token

        Assert.assertEquals(tokens, currentUser.unconfirmedGeneratedSendTokensForDecryption)

        val expectedCombinedToken = getExpectedToken(listOf("token4", "token3", "token2"))

        val combinedToken = objectUnderTest
            .generateCombinedTokenForUserAndConfirmTokens(currentUser, messages, expectedCombinedToken)

        val skippedMessages = messages.filter {
            it.messageTokenSkipped
        }
        val confirmedMessages = messages.filter {
            it.messageConfirmed
        }
        Assert.assertEquals(0, skippedMessages.size)
        Assert.assertEquals(3, confirmedMessages.size)

        Assert.assertEquals(expectedCombinedToken, combinedToken)
        Assert.assertEquals(listOf("token4"), currentUser.unconfirmedGeneratedSendTokensForDecryption)
    }

    @Test
    fun testTokensGetProperlyRemovedWithMessagePersisting() {
        val senderUser = User("dummyNicknameSender", "dummySenderHash")
        val currentUser = CurrentUser("dummyNickname", "dummyHash")

        val initialToken = "initialToken"
        senderUser.pushGeneratedToken(initialToken)
        currentUser.pushGeneratedToken(initialToken)

        val objectUnderTest = TokenDeterminer(securityInterfaceHolder)
        // currentUser.pushGeneratedToken(initialToken)

        messages = mutableListOf()
        messages.add(getMessage(senderUser))
        messages.add(getMessage(currentUser))
        messages.add(getMessage(senderUser))
        messages.add(getMessage(currentUser))
        messages.add(getMessage(senderUser))
        messages.add(getMessage(senderUser))
        messages.add(getMessage(senderUser))
        messages.add(getMessage(currentUser))

        messages.forEach {
            Assert.assertTrue("message ${it.toString()}", it.messageTokenSkipped == it.messageConfirmed && it.messageConfirmed == false)
        }

        Assert.assertEquals(listOf("token9", "token5", "token3", "initialToken"), currentUser.unconfirmedGeneratedSendTokensForDecryption)
        Assert.assertEquals(listOf("token8", "token7", "token6", "token4", "token2", "initialToken"), senderUser.unconfirmedGeneratedSendTokensForDecryption)

        val expectedCombinedToken = getExpectedToken(listOf("token9"))
        val combinedToken = objectUnderTest
            .generateCombinedTokenForUserAndConfirmTokens(
                currentUser, messages, expectedCombinedToken)

        val skippedMessages = messages.filter {
            it.messageTokenSkipped
        }
        val confirmedMessages = messages.filter {
            it.messageConfirmed
        }
        Assert.assertEquals(0, skippedMessages.size)
        Assert.assertEquals(1, confirmedMessages.size)

        // filter vs takewhile
        Assert.assertEquals("WKzcBTk6O8d5+6FY", combinedToken)
        // Assert.assertEquals("+S0dnx7+ze0IrYln", combinedToken)
        Assert.assertEquals(listOf("token9"),  currentUser.unconfirmedGeneratedSendTokensForDecryption) // , "token5", "token3", "initialToken"
        Assert.assertEquals(listOf("token8", "token7", "token6", "token4", "token2", "initialToken"), senderUser.unconfirmedGeneratedSendTokensForDecryption)

        Assert.assertEquals("token10", securityInterfaceHolder.randomHelper.computeRandomHashWithSpecificLength(12))

        // Assert.assertEquals(combinedToken, combinedToken2)
    }


    /**
     * EDGE CASES
     */



    @Test
    fun testTokensGetProperlyRemovedWithoutMessagePersisting() {
        val currentUser = CurrentUser("dummyNickname", "dummyHash")
        messages = mutableListOf()
        messages.add(UserMessage())

        currentUser.pushGeneratedToken("token1")
        currentUser.pushGeneratedToken("token2")
        currentUser.pushGeneratedToken("token3")
        currentUser.pushGeneratedToken("token4")

        val objectUnderTest = TokenDeterminer(securityInterfaceHolder)
        objectUnderTest.confirmGeneratedToken(currentUser, "token2", messages)

        Assert.assertEquals(listOf("token4", "token3", "token2"), currentUser.unconfirmedGeneratedSendTokensForDecryption)

        val combinedToken2 = objectUnderTest
            .generateCombinedTokenForUserAndConfirmTokens(currentUser,messages, "token4")

        Assert.assertEquals(listOf("token4", "token3", "token2"), currentUser.unconfirmedGeneratedSendTokensForDecryption)
        Assert.assertEquals(null, combinedToken2) // No token can be found.
    }

    @Test
    fun testUnknownTokenDoesntBreakAnything() {
        // This test is kinda useless, it just tests no exception is thrown, but tokens get marked as skipped.
        val currentUser = CurrentUser("dummyNickname", "dummyHash")

        messages = mutableListOf()
        messages.add(UserMessage())

        val objectUnderTest = TokenDeterminer(securityInterfaceHolder)
        objectUnderTest.confirmGeneratedToken(currentUser, "token2", messages)
    }

    @Ignore // first assumptions but the algorithm doesnt work like that.
    @Test
    fun testTokensGetProperlyRemovedExactOrder() {
        val currentUser = CurrentUser("dummyNickname", "dummyHash")

        messages = mutableListOf()
        messages.add(UserMessage())

        currentUser.pushGeneratedToken("token1")
        currentUser.pushGeneratedToken("token2")
        currentUser.pushGeneratedToken("token3")
        currentUser.pushGeneratedToken("token4")

        val objectUnderTest = TokenDeterminer(securityInterfaceHolder)
        objectUnderTest.confirmGeneratedToken(currentUser, "token2", messages)

        Assert.assertEquals(currentUser.unconfirmedGeneratedSendTokensForDecryption, listOf("token4", "token3", "token1"))

        val token1 = "token1"
        val combinedToken = objectUnderTest
            .generateCombinedTokenForUserAndConfirmTokens(currentUser, messages, token1)

        Assert.assertEquals(currentUser.unconfirmedGeneratedSendTokensForDecryption, listOf("token4", "token3"))

        val combinedToken2 = objectUnderTest
            .generateCombinedTokenForUserAndConfirmTokens(currentUser, messages, "token4")

        Assert.assertEquals(currentUser.unconfirmedGeneratedSendTokensForDecryption.size, 0)
        Assert.assertEquals(combinedToken, combinedToken2)
    }

    private fun getExpectedToken(tokens: List<String>): String {
        return securityInterfaceHolder.hashHelper
            .hashWithMaxLength(
                *tokens.toTypedArray(),
                length = securityInterfaceHolder.symmetricEncryptionHelper.SEED_SIZE
            )
    }

    private fun getMessage(sender: SimpleDialog): AbstractMessage {
        val result = UserMessage()
        val tokens = TokenDeterminer(securityInterfaceHolder).getTokensForMessageMetaData(sender, messages)
        result.metaData = MetaData().init(
            contextMock,
            applicationInfoComponentMock,
            settings,
            tokens
        )
        result.sender = sender

        return result
    }
}