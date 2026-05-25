package de.hartz.software.parannoying.core.helper.security.provider

import de.hartz.software.parannoying.core.interfaces.di.security.AsymmetricEncryptionHelper
import de.hartz.software.parannoying.core.interfaces.di.security.CompressionHelper
import de.hartz.software.parannoying.core.interfaces.di.security.DataConverter
import de.hartz.software.parannoying.core.interfaces.di.security.HMACHelper
import de.hartz.software.parannoying.core.interfaces.di.security.HardcodedEncryptionHelper
import de.hartz.software.parannoying.core.interfaces.di.security.HashHelper
import de.hartz.software.parannoying.core.interfaces.di.security.RandomHelper
import de.hartz.software.parannoying.core.interfaces.di.security.SymmetricEncryptionHelper
import javax.inject.Inject

class SecurityInterfaceHolder @Inject constructor(
    val asymmetricEncryptionHelper: AsymmetricEncryptionHelper,
    val CompressionHelper: CompressionHelper,
    val dataConverter: DataConverter,
    val hardcodedEncryptionHelper: HardcodedEncryptionHelper,
    val hashHelper: HashHelper,
    val hmacHelper: HMACHelper,
    val randomHelper: RandomHelper,
    val symmetricEncryptionHelper: SymmetricEncryptionHelper
)