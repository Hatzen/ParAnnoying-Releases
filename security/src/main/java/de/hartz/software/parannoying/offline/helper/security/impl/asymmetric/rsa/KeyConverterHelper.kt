package de.hartz.software.parannoying.offline.helper.security.impl.asymmetric.rsa

import android.util.Base64
import java.security.Key
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec


// Converter to store object of Key
class KeyConverter {
    // https://stackoverflow.com/questions/5355466/converting-secret-key-into-a-string-and-vice-versa
    // https://www.novixys.com/blog/how-to-generate-rsa-keys-java/

    private val beginPublic = "-----BEGIN PUBLIC KEY-----\n"
    private val beginPrivate = "-----BEGIN PRIVATE KEY-----\n"

    fun convertToDatabaseValue(entityProperty: Key?): String? {
        if (entityProperty == null) {
            return null
        }
        var databaseval = ""
        if (entityProperty is PrivateKey) {
            databaseval += beginPrivate
        }
        if (entityProperty is PublicKey) {
            databaseval += beginPublic
        }
        databaseval += Base64.encodeToString(entityProperty!!.encoded, Base64.DEFAULT)
        return databaseval
    }

    fun convertToEntityProperty(databaseValue: String?): Key? {
        if( databaseValue == null )
            return null
        val keyBytes = Base64.decode( databaseValue.replace(beginPublic, "").replace(beginPrivate
                ,""), Base64.DEFAULT)
        val keyFactory = KeyFactory.getInstance(PlainRSAEncryptionHelper.GENERAL_ALGORITHM)

        if (databaseValue.contains(beginPublic)) {
            val spec = X509EncodedKeySpec(keyBytes)
            return keyFactory.generatePublic(spec)
        }
        if (databaseValue.contains(beginPrivate)) {
            val spec = PKCS8EncodedKeySpec(keyBytes)
            return keyFactory.generatePrivate(spec)
        }
        return null
    }
}