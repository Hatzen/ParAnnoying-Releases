package de.hartz.software.parannoying.app.helper.provider

import dagger.Binds
import dagger.Module
import de.hartz.software.parannoying.core.interfaces.di.security.AsymmetricEncryptionHelper
import de.hartz.software.parannoying.core.interfaces.di.security.CompressionHelper
import de.hartz.software.parannoying.core.interfaces.di.security.DataConverter
import de.hartz.software.parannoying.core.interfaces.di.security.FileAnalyzerHelper
import de.hartz.software.parannoying.core.interfaces.di.security.HMACHelper
import de.hartz.software.parannoying.core.interfaces.di.security.HardcodedEncryptionHelper
import de.hartz.software.parannoying.core.interfaces.di.security.HashHelper
import de.hartz.software.parannoying.core.interfaces.di.security.RandomHelper
import de.hartz.software.parannoying.core.interfaces.di.security.SymmetricEncryptionHelper
import de.hartz.software.parannoying.offline.helper.security.file.FileAnalyzerHelperImpl
import de.hartz.software.parannoying.offline.helper.security.impl.asymmetric.rsa.PlainRSAEncryptionHelper
import de.hartz.software.parannoying.offline.helper.security.impl.compression.CompressionHelperImpl
import de.hartz.software.parannoying.offline.helper.security.impl.converter.DataConverterImpl
import de.hartz.software.parannoying.offline.helper.security.impl.hardcoded.HardcodedEncryptionHelperImpl
import de.hartz.software.parannoying.offline.helper.security.impl.hash.HashHelperImpl
import de.hartz.software.parannoying.offline.helper.security.impl.hmac.HMACHelperImpl
import de.hartz.software.parannoying.offline.helper.security.impl.random.RandomHelperImpl
import de.hartz.software.parannoying.offline.helper.security.impl.symmetric.AESEncryptor

@Module
abstract class SecurityModule {
    @Binds
    abstract fun bindAsymmetricEncryptionHelper(impl: PlainRSAEncryptionHelper): AsymmetricEncryptionHelper
    @Binds
    abstract fun bindCompressionHelper(impl: CompressionHelperImpl): CompressionHelper
    @Binds
    abstract fun bindDataConverter(impl: DataConverterImpl): DataConverter
    @Binds
    abstract fun bindHardcodedEncryptionHelper(impl: HardcodedEncryptionHelperImpl): HardcodedEncryptionHelper
    @Binds
    abstract fun bindHMACHelper(impl: HMACHelperImpl): HMACHelper
    @Binds
    abstract fun bindRandomHelper(impl: RandomHelperImpl): RandomHelper
    @Binds
    abstract fun bindHashHelper(impl: HashHelperImpl): HashHelper
    @Binds
    abstract fun bindSymmetricEncryptionHelper(impl: AESEncryptor): SymmetricEncryptionHelper
    @Binds
    abstract fun bindFileAnalyzerHelper(impl: FileAnalyzerHelperImpl): FileAnalyzerHelper
}