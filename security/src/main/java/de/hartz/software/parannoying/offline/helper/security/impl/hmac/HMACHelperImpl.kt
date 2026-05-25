package de.hartz.software.parannoying.offline.helper.security.impl.hmac

import android.annotation.SuppressLint
import de.hartz.software.parannoying.core.interfaces.di.security.DataConverter
import de.hartz.software.parannoying.core.interfaces.di.security.HMACHelper
import java.security.PrivateKey
import java.security.PublicKey
import java.util.BitSet
import javax.inject.Inject

@Deprecated("Use proper digest algorithms")
class HMACHelperImpl @Inject constructor(val dataConverter: DataConverter): HMACHelper {

    override fun hasKeys(): Boolean {
        return false
    }

    override fun getKeyForSigning(password: String): PublicKey = throw NotImplementedError()

    override fun getKeyForChecking(password: String): PrivateKey = throw NotImplementedError()

    // TODO Consider https://crypto.stackexchange.com/a/205
    override fun getHMACForMessage(message: String, key: String?): String {
        return dataConverter.intToString(getCrossSumOfByteArray(dataConverter.stringToByteArray(message)))
    }

    @SuppressLint("NewApi") // TODO: Find a way arount new BitSet...
    fun getCrossSumOfByteArray(byteArray: ByteArray) : Int {
        val bitSet = BitSet.valueOf(byteArray)
        var z = 0
        for ( i in 0..bitSet.length()) {
            // Else set this bit
            if (bitSet.get(i)) {
                z++
            }
        }
        return z
    }

}