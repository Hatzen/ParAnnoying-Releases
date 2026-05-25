package de.hartz.software.parannoying.offline.model.mapper

import de.hartz.software.parannoying.offline.helper.security.impl.asymmetric.rsa.KeyConverter
import java.security.PublicKey

// We need one mapper per key type.
class PublicKeyMapper {
    // Maybe we can remove optional from input param.
    fun asString(key: PublicKey?): String? {
        return KeyConverter().convertToDatabaseValue(key)
    }

    fun asKey(key: String?): PublicKey? {
        return KeyConverter().convertToEntityProperty(key) as PublicKey?
    }
}