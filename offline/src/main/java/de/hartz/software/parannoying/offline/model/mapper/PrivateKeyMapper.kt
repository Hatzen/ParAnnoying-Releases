package de.hartz.software.parannoying.offline.model.mapper

import de.hartz.software.parannoying.offline.helper.security.impl.asymmetric.rsa.KeyConverter
import java.security.PrivateKey

// We need one mapper per key type.
class PrivateKeyMapper {

    fun asString(key: PrivateKey): String? {
        return KeyConverter().convertToDatabaseValue(key)
    }

    fun asKey(key: String): PrivateKey? {
        return KeyConverter().convertToEntityProperty(key) as PrivateKey?
    }
}
