package de.hartz.software.parannoying.core.helper.security.provider

enum class ProviderCategory {
    SECURE_RANDOM,
    SYMMETRIC_ENCRYPTION, // TODO: Might need parameters for key and iv, bit size etc?
    ASYMMETRIC_ENCRYPTION,
    STATIC_ENCRYPTION, // Salt and Pepper or "doMagic()"
    HASH, // userId => "Xyz"
    PASSWORD_BASED_KEY_DERIVATION, // ABC => "A2y#ayYah81.1l"
    BYTE_TO_STRING_ENCODER, // Base64, Hex, Just bytes to String etc

    // Future?
    KEY_GENERATOR, // custom one just random and use derivation to make them match, or just use providers one (just in case if one generator got )
    MESSAGE_SERIALIZER, // current Custom one, JSON, CSV, GRPC etc?
    PADDING_SCHEMES, // only needed for public?
    IV_DERIVATION_FUNCTION, // Get IV from message and some additional IV or something
    AUTHENTICATION, // HMAC, etc
    DATA_CHUNKER,  // If data is to big split it into different chunks and add seperator handling etc?
    DATA_COMPRESSION // Gzip etc. might be incompatible with BYTE_TO_STRING_ENCODER etc

}