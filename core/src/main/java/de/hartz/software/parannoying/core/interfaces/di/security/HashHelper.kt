package de.hartz.software.parannoying.core.interfaces.di.security

interface HashHelper {

    fun getStaticSaltList(): Array<String>

    fun getStringHashForUi(text: String): String

    fun hashWithSpecificLength (vararg seeds: String, length: Int): String

    fun hashWithMaxLength (vararg seeds: String, length: Int): String

    fun hash (vararg seeds: String): String

    fun hash(seed: String): String
}