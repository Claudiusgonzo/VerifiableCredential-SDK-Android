package com.microsoft.portableIdentity.sdk.crypto.plugins.subtleCrypto

import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.*
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.portableIdentity.sdk.utilities.SdkLog
import com.microsoft.portableIdentity.sdk.utilities.Serializer

/**
 * sourced from https://github.com/PeculiarVentures/webcrypto-core/blob/master/src/subtle.ts
 */
open class Subtle(providers: Set<Provider> = emptySet()): SubtleCrypto {
    val provider = providers.map({
        Pair<String, Provider>(it.name.toLowerCase(), it)
    }).toMap()

    private fun getProvider(algorithm: String): Provider {
        return provider[algorithm.toLowerCase()] ?: throw SdkLog.error("Unknown algorithm $algorithm")
    }

    override fun encrypt(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        val provider = getProvider(algorithm.name)
        return provider.encrypt(algorithm, key, data)
    }

    override fun decrypt(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        val provider = getProvider(algorithm.name)
        return provider.decrypt(algorithm, key, data)
    }

    override fun sign(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        val provider = getProvider(algorithm.name)
        return provider.sign(algorithm, key, data)
    }

    override fun verify(algorithm: Algorithm, key: CryptoKey, signature: ByteArray, data: ByteArray): Boolean {
        val provider = getProvider(algorithm.name)
        return provider.verify(algorithm, key, signature, data)
    }

    override fun digest(algorithm: Algorithm, data: ByteArray): ByteArray {
        val provider = getProvider(algorithm.name)
        return provider.digest(algorithm, data)
    }

    override fun generateKey(algorithm: Algorithm, extractable: Boolean, keyUsages: List<KeyUsage>): CryptoKey {
        val provider = getProvider(algorithm.name)
        return provider.generateKey(algorithm, extractable, keyUsages.toSet())
    }
    override fun generateKeyPair(algorithm: Algorithm, extractable: Boolean, keyUsages: List<KeyUsage>): CryptoKeyPair {
        val provider = getProvider(algorithm.name)
        return provider.generateKeyPair(algorithm, extractable, keyUsages.toSet())
    }

    override fun deriveKey(
        algorithm: Algorithm,
        baseKey: CryptoKey,
        derivedKeyType: Algorithm,
        extractable: Boolean,
        keyUsages: List<KeyUsage>
    ): CryptoKey {
        // check derivedKeyType
        val importProvider = this.getProvider(derivedKeyType.name);
        importProvider.checkDerivedKeyParams(derivedKeyType)
        val deriveKeyLength = (derivedKeyType.additionalParams["length"] as ULong? ?: throw SdkLog.error("DerivedKeyType must include a length parameter"))

        // derive bits
        val provider = this.getProvider(algorithm.name);
        provider.checkCryptoKey(baseKey, KeyUsage.DeriveKey);
        val derivedBits = provider.deriveBits(algorithm, baseKey, deriveKeyLength)

        // import derived key
        return this.importKey(KeyFormat.Raw, derivedBits, derivedKeyType, extractable, keyUsages);
    }

    override fun deriveBits(algorithm: Algorithm, baseKey: CryptoKey, length: ULong): ByteArray {
        val provider = getProvider(algorithm.name)
        return provider.deriveBits(algorithm, baseKey, length)
    }

    override fun importKey(
        format: KeyFormat,
        keyData: ByteArray,
        algorithm: Algorithm,
        extractable: Boolean,
        keyUsages: List<KeyUsage>
    ): CryptoKey {
        val provider = getProvider(algorithm.name)
        return provider.importKey(format, keyData, algorithm, extractable, keyUsages.toSet())
    }

    override fun importKey(
        format: KeyFormat,
        keyData: JsonWebKey,
        algorithm: Algorithm,
        extractable: Boolean,
        keyUsages: List<KeyUsage>
    ): CryptoKey {
        val provider = getProvider(algorithm.name)
        return provider.importKey(format, keyData, algorithm, extractable, keyUsages.toSet())
    }

    override fun exportKey(format: KeyFormat, key: CryptoKey): ByteArray {
        val provider = getProvider(key.algorithm.name)
        return provider.exportKey(format, key)
    }

    override fun exportKeyJwk(key: CryptoKey): JsonWebKey {
        val provider = getProvider(key.algorithm.name)
        return provider.exportKeyJwk(key)
    }

    override fun wrapKey(
        format: KeyFormat,
        key: CryptoKey,
        wrappingKey: CryptoKey,
        wrapAlgorithm: Algorithm
    ): ByteArray {
        var keyData: ByteArray
        if (format == KeyFormat.Jwk) {
            val keyJwk = this.exportKeyJwk(key)
            val jwkSequence = Serializer.stringify(JsonWebKey.serializer(), keyJwk).asSequence()
            keyData = ByteArray(jwkSequence.count())
            jwkSequence.forEachIndexed { index, character ->
                keyData[index] = character.toByte()
            }
        } else {
            keyData = this.exportKey(format, key);
        }

        // encrypt key data
        val provider = this.getProvider(wrapAlgorithm.name);
        return provider.encrypt(wrapAlgorithm, wrappingKey, keyData);
    }

    override fun unwrapKey(
        format: KeyFormat,
        wrappedKey: ByteArray,
        unwrappingKey: CryptoKey,
        unwrapAlgorithm: Algorithm,
        unwrappedKeyAlgorithm: Algorithm,
        extractable: Boolean,
        keyUsages: List<KeyUsage>
    ): CryptoKey {
        // decrypt wrapped key
        val provider = this.getProvider(unwrapAlgorithm.name);
        val keyData = provider.decrypt(unwrapAlgorithm, unwrappingKey, wrappedKey);
        if (format == KeyFormat.Jwk) {
            try {
                val jwk = JsonWebKey(keyData.toList().joinToString());
                // import key
                return this.importKey(format, jwk, unwrappedKeyAlgorithm, extractable, keyUsages)
            } catch (error: Error) {
                throw SdkLog.error("wrappedKey is not a JSON web key")
            }
        }

        // import key
        return this.importKey(format, keyData, unwrappedKeyAlgorithm, extractable, keyUsages)
    }
}