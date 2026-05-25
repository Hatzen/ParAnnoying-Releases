package de.hartz.software.parannoying.app.small

import androidx.test.core.app.ApplicationProvider
import de.hartz.software.parannoying.app.App
import de.hartz.software.parannoying.app.large.tests.utils.DeviceUtil
import de.hartz.software.parannoying.app.large.tests.utils.TestFileUtil
import de.hartz.software.parannoying.app.utils.DaggerTestComponent
import de.hartz.software.parannoying.core.helper.security.DataGeneratorHelper
import de.hartz.software.parannoying.core.helper.security.DataSecurityHelper
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.model.domain.DeviceRole
import de.hartz.software.parannoying.offline.helper.security.DialogCreationHelper
import de.hartz.software.parannoying.offline.helper.security.serializer.file.FileDeserializer
import de.hartz.software.parannoying.offline.helper.security.serializer.file.FileSerializer
import de.hartz.software.parannoying.offline.model.domain.messages.MetaData
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import javax.inject.Inject

@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
class FileMessageTest {

    @Inject
    lateinit var securityInterfaceHolder: SecurityInterfaceHolder

    @Test
    fun encryptAndDecryptFile() {
        val context = ApplicationProvider.getApplicationContext<App>()
        val testComponent = DaggerTestComponent.create()
        testComponent.inject(this)
        // val securityInterfaceHolder = getSecurityInterface()

        val deviceUtil = DeviceUtil(context)
        deviceUtil.addDevice("A")
        deviceUtil.switchTo("A", DeviceRole(DeviceRole.OFFLINE))
        DialogCreationHelper(context, securityInterfaceHolder).createCurrentUserData("name", DataGeneratorHelper(securityInterfaceHolder).createFakeOnlineId(), context)

        val fileToEncrypt = TestFileUtil.getTestFile(context, false)
        val testUser = DialogCreationHelper(context, securityInterfaceHolder).createOfflineGroup("test", context)

        val metaData = MetaData()

        val encryptedfile = FileSerializer(securityInterfaceHolder).encryptFile(fileToEncrypt.canonicalPath, testUser, metaData)
        val decryptedfile = FileDeserializer(securityInterfaceHolder, DataSecurityHelper(securityInterfaceHolder)).decryptFile(encryptedfile.canonicalPath, testUser, testUser.newestReceivedToken)
        assertEquals(fileToEncrypt.length(), File(decryptedfile.filePath).length())
    }

}
