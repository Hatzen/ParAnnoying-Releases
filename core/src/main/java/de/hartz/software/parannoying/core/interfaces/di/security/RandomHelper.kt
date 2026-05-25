package de.hartz.software.parannoying.core.interfaces.di.security

interface RandomHelper {

    fun randomBoolean(): Boolean

    @Deprecated("Can be used but should not be used for generating specific " +
            "byte lengths usually need to create byte array and encode to base64")
    fun computeRandomHashWithSpecificLength(length: Int): String

    fun computeSecureRandomHashWithSpecificLength(length: Int): String

    fun getRandomPinCode () : String

    fun getRandomUUIDv4(): String
    fun getIdentifiableUUIDv1(): String
}