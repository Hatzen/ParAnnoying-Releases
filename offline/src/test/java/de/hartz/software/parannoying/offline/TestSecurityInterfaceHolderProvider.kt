package de.hartz.software.parannoying.offline

import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.offline.helper.security.impl.asymmetric.rsa.PlainRSAEncryptionHelper
import de.hartz.software.parannoying.offline.helper.security.impl.compression.CompressionHelperImpl
import de.hartz.software.parannoying.offline.helper.security.impl.converter.DataConverterImpl
import de.hartz.software.parannoying.offline.helper.security.impl.hardcoded.HardcodedEncryptionHelperImpl
import de.hartz.software.parannoying.offline.helper.security.impl.hash.HashHelperImpl
import de.hartz.software.parannoying.offline.helper.security.impl.hmac.HMACHelperImpl
import de.hartz.software.parannoying.offline.helper.security.impl.random.RandomHelperImpl
import de.hartz.software.parannoying.offline.helper.security.impl.symmetric.AESEncryptor
import io.mockk.spyk

object TestSecurityInterfaceHolderProvider {
    fun getSecurityInterface(): SecurityInterfaceHolder {
        val dataConverterImpl = DataConverterImpl()
        val compressionHelperImpl = CompressionHelperImpl()
        val randomHelperImpl = spyk(RandomHelperImpl(dataConverterImpl))
        return spyk(SecurityInterfaceHolder(
            PlainRSAEncryptionHelper(dataConverterImpl),
            compressionHelperImpl,
            dataConverterImpl,
            HardcodedEncryptionHelperImpl(dataConverterImpl),
            HashHelperImpl(dataConverterImpl),
            HMACHelperImpl(dataConverterImpl),
            randomHelperImpl,
            AESEncryptor(dataConverterImpl, compressionHelperImpl, randomHelperImpl)
        ))
    }
}