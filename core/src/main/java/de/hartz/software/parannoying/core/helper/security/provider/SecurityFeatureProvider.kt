package de.hartz.software.parannoying.core.helper.security.provider

abstract class SecurityFeatureProvider(
    val id: String, // might be an appended prefix to the data so we could decide for an algorithm
    val name: String, // NTRU
    val provider: String, // bouncycastle, java native, self implemented
    val version: String,
    val providerCategory: ProviderCategory,
    val quantumsafe: Boolean? = null,
    val securityLevel: Int = 5 // proven or self implemented..
) {

    fun hasMaxDataSize(): Boolean {
        return false
    }

    fun maxDataSize(): Long {
        return Long.MAX_VALUE
    }

}