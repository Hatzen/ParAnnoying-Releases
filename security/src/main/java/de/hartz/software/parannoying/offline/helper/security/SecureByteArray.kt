package de.hartz.software.parannoying.offline.helper.security

import java.security.SecureRandom

class SecureByteArray(private val data: ByteArray) {
    fun wipe() {
        SecureRandom().nextBytes(data) // Overwrite with random values
    }

    fun get(): ByteArray = data // Access data securely

    @Throws(Throwable::class)
    protected fun finalize() {
        wipe() // Auto-cleanup before GC
        // super.finalize()
    }
}