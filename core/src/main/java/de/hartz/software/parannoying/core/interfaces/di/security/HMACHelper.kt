package de.hartz.software.parannoying.core.interfaces.di.security

import java.security.PrivateKey
import java.security.PublicKey

interface HMACHelper {

    fun hasKeys(): Boolean

    fun getKeyForSigning(password: String): PublicKey

    fun getKeyForChecking(password: String): PrivateKey

    fun getHMACForMessage(message: String, key: String? = null): String

}