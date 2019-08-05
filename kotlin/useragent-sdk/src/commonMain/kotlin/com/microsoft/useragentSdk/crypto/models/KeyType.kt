package com.microsoft.useragentSdk.crypto.models

/**
 * The type of a key.
 */
enum class KeyType (val keyType: String) {
    Public("public"),
    Private ("private"),
    /** Opaque keying material, including that used for symmetric algorithms */
    Secret("secret")
}