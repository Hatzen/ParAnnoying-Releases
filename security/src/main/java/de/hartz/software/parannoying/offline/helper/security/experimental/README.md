Some old comment collection, mostly outdated

    // TODO: Use different encryption based on symmetric key: https://developer.android.com/guide/topics/security/cryptography
    // TODO: Do some checks: https://rules.sonarsource.com/kotlin/RSPEC-5542/
    // TODO: RSA 2048 has max length of 256 Bytes (? at least 1024 has 128 Bytes):
    // So use chunked rsa may be an option also it is not recommended (arguments are invalid here): https://stackoverflow.com/a/23723450/8524651


    // TODO: Update to at least 4096 (might fail userId via QrCode.. At all than use VideoQr?) https://en.wikipedia.org/wiki/Key_size#Symmetric_algorithm_key_lengths
    // TODO: We might get around the size problem by only storing information like here https://www.javainterviewpoint.com/rsa-encryption-and-decryption/
    const val ASYMMETRIC_KEY_SIZE = 1024
