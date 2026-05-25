package de.hartz.software.parannoying.app.small

import android.content.Context
import androidx.test.InstrumentationRegistry
import de.hartz.software.parannoying.app.utils.TestSecurityInterfaceHolderProvider
import de.hartz.software.parannoying.offline.helper.security.ConstantKeys
import de.hartz.software.parannoying.offline.helper.security.impl.asymmetric.rsa.KeyConverter
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class ConstantKeysTest {

    lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getTargetContext()
    }

    @Ignore("Do not generate files every time. It takes 5 min")
    @Test
    fun generateKeysAndStoreTestfiles() {
        val securityInterfaceHolder = TestSecurityInterfaceHolderProvider.getSecurityInterface()
        val constantKeys = ConstantKeys(context, securityInterfaceHolder.asymmetricEncryptionHelper)
        constantKeys.createKeyPairs(ConstantKeys.numberOfKeyPairs)
        val keyMap1 = constantKeys.getKeyMap(ConstantKeys.numberOfKeyPairs)
        val randomKey = keyMap1.keys.toMutableList()[(0..keyMap1.size).random()]
        val expected = keyMap1[randomKey]

        val keyMap2 = constantKeys.getKeyMap(ConstantKeys.numberOfKeyPairs)
        val actual = keyMap2[randomKey]
        assertEquals(expected, actual)
    }


    @Ignore("Currently failing, but working on device. Need Instrumentedtest or date update?")
    @Test
    fun loadConstantKeysWorks () {
        val securityInterfaceHolder = TestSecurityInterfaceHolderProvider.getSecurityInterface()
        val constantKeys = ConstantKeys(context, securityInterfaceHolder.asymmetricEncryptionHelper)
        val keyMap = constantKeys.getKeyMap(ConstantKeys.numberOfKeyPairs)
        assertEquals(ConstantKeys.numberOfKeyPairs + 1, keyMap.size)

        val randomIndex = (0..keyMap.size).random()
        val randomKey = keyMap.values.toMutableList()[randomIndex]
        val actual = keyMap.keys.toMutableList()[randomIndex]
        assertEquals(KeyConverter().convertToDatabaseValue(randomKey.public), actual)
    }

}
