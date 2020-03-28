package com.microsoft.portableIdentity.sdk.crypto.protocols.jose

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keys.PublicKey
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.resolvers.IResolver
import com.microsoft.portableIdentity.sdk.utilities.SdkLog

object DidKeyResolver {
    suspend fun resolveIdentiferFromKid(kid: String, crypto: CryptoOperations, resolver: IResolver): Identifier {
        val did = Regex("^([^#]+)#.+$").matchEntire(kid) ?: throw SdkLog.error("No identifier found in key id")
        return resolver.resolve(did.groupValues[1], crypto)
    }

    suspend fun resolveKeyFromKid(kid: String, crypto: CryptoOperations, resolver: IResolver): PublicKey {
        val identifier = resolveIdentiferFromKid(kid, crypto, resolver)
        val did = Regex("^[^#]+(#.+)$").matchEntire(kid)!!
        return identifier.document.publicKeys.filter {
            it.publicKeyJwk.kid?.endsWith(did.groupValues[1]) ?: false ||
                    it.id.endsWith(did.groupValues[1])
        }.firstOrNull()?.toPublicKey() ?: throw SdkLog.error("Could not find key $kid")
    }
    
    suspend fun verifyJws(jws: JwsToken, crypto: CryptoOperations, forDid: Identifier) {
        val keys = forDid.document.publicKeys.map {
            it.toPublicKey()
        }
        jws.verify(crypto, keys)
    }

    suspend fun verifyJws(jws: JwsToken, crypto: CryptoOperations, resolver: IResolver, forDid: String? = null) {
        if (forDid.isNullOrBlank()) {
            val sender = resolver.resolve(forDid!!, crypto)
            // verify the request
            verifyJws(jws, crypto, sender)
        } else {
            val keys = mutableListOf<PublicKey>()
            jws.signatures.forEachIndexed { index, signature ->
                val kid = signature.getKid() ?: throw SdkLog.error("Could not find kid in signature $index")
                keys.add(resolveKeyFromKid(kid, crypto, resolver))
            }
            jws.verify(crypto, keys)
        }
    }
}