package de.hartz.software.parannoying.offline

import android.content.Context
import android.util.Base64
import de.hartz.software.parannoying.core.extensions.ExtensionPathAsString
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.helper.development.DevelopmentUtil
import de.hartz.software.parannoying.core.interfaces.AbstractApp
import de.hartz.software.parannoying.core.interfaces.di.ApplicationInfoComponent
import de.hartz.software.parannoying.core.interfaces.di.air.gap.AirGapAdapter
import de.hartz.software.parannoying.offline.businesslogic.CreateMessageProcessor
import de.hartz.software.parannoying.offline.businesslogic.ReceiveMessageProcessor
import de.hartz.software.parannoying.offline.businesslogic.TokenDeterminer
import de.hartz.software.parannoying.offline.helper.security.DialogCreationHelper
import de.hartz.software.parannoying.offline.model.OfflineStorage
import de.hartz.software.parannoying.offline.model.domain.dialogs.SimpleDialog
import de.hartz.software.parannoying.offline.model.domain.dialogs.User
import de.hartz.software.parannoying.offline.model.domain.messages.AbstractMessage
import de.hartz.software.parannoying.offline.model.domain.messages.MetaData
import de.hartz.software.parannoying.offline.model.domain.messages.UserMessage
import de.hartz.software.parannoying.offline.model.domain.settings.OfflineSettings
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.Base64 as JavaBase64

class FullEncryptDecryptTest {

    var securityInterfaceHolder = TestSecurityInterfaceHolderProvider.getSecurityInterface()
    lateinit var settings: OfflineSettings

    val airGapAdapterMock = mockk<AirGapAdapter>()
    val contextMock = mockk<Context>()
    val storageMock = mockk<OfflineStorage>(relaxed = true)
    val applicationInfoComponentMock = object : ApplicationInfoComponent {
        override fun getVersion(): String {
            return "-1"
        }
    }

    @Before
    fun init() {
        DevelopmentUtil.deactivateApplicationCheckForUnitTests()
        settings = OfflineSettings()

        mockkStatic(ExtensionPathAsString)
        val mockApp = mockk<AbstractApp>()
        every { contextMock.app } returns mockApp
        every { mockApp.Storage } returns storageMock
        every { storageMock.readSettings() } returns settings

        every { mockApp.airGapAdapter } returns airGapAdapterMock

        var counter = 0
        every { securityInterfaceHolder.randomHelper.computeRandomHashWithSpecificLength(any()) } answers {
            counter += 1
            "token$counter"
        }
        // Check mocking works as expected.
        Assert.assertEquals("token1", securityInterfaceHolder.randomHelper.computeRandomHashWithSpecificLength(12))

        every { securityInterfaceHolder.randomHelper.getRandomUUIDv4() } answers {
            counter += 1
            "uuid$counter"
        }

        mockkStatic(Base64::class)

        every {
            Base64.encodeToString(any(), any())
        } answers {
            val input = firstArg<ByteArray>()
            val flags = secondArg<Int>()

            val encoded = JavaBase64.getEncoder().encodeToString(input)

            // Simulate Android Base64 flags (simplified)
            when {
                flags and Base64.NO_WRAP != 0 -> encoded.replace("\n", "")
                else -> encoded // Java doesn't add line breaks by default
            }
        }
        every {
            Base64.decode(any<String>(), any())
        } answers {
            val input = firstArg<String>()
            val flags = secondArg<Int>()

            val cleanInput = if (flags == Base64.DEFAULT) {
                // remove all line breaks (Android DEFAULT inserts line breaks)
                input.replace("\n", "").replace("\r", "")
            } else {
                input
            }

            JavaBase64.getDecoder().decode(cleanInput)
        }

    }

    @After
    fun cleanUp() {
        unmockkStatic(Base64::class)
    }


    @Test
    fun testTokensGetProperlyRemovedWithMessagePersistingSimpleApprove() {
        // val fakeHash1 = DataGeneratorHelper(securityInterfaceHolder).createFakeOnlineId()
        // val fakeHash2 = DataGeneratorHelper(securityInterfaceHolder).createFakeOnlineId()
        val fakeHash2 = "XMOD876_TEST_f2707f53-e973-4176-9a24-3bd9849b8455@parannoying.deZ22U8cm/1odNeUlc/V1bWI=8lxbm243V" // ||__||

        val senderUser = User("dummyNicknameSender", "dummySenderHash")
        // val currentUser = CurrentUser("dummyNickname", "dummyHash")

        DialogCreationHelper(contextMock, securityInterfaceHolder)
            .createCurrentUserData("DeviceA", fakeHash2, contextMock)

        // every { storageMock.currentUser } answers {
        //     currentUser
        // }
        every { storageMock.users } answers {
            listOf(senderUser)
        }

        // TODO: Create Current user and 2 further users. Communicate in online group and between them.
        CreateMessageProcessor(securityInterfaceHolder, contextMock, applicationInfoComponentMock)
            .queue("test1", senderUser, {})
        val encryptedMessage = storageMock.readSendMessage().first().encryptedMessage

        ReceiveMessageProcessor(contextMock, securityInterfaceHolder)
            .addReceivedMessage(encryptedMessage)

        // TODO: Should we try it like this with storage mocking or just copy methods without storage?



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
        val tokens = TokenDeterminer(securityInterfaceHolder).getTokensForMessageMetaData(sender, listOf())
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
