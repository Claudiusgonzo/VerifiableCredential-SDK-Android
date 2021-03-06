// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.util

import android.util.Base64
import com.microsoft.did.sdk.crypto.models.AndroidConstants
import com.microsoft.did.sdk.crypto.plugins.Secp256k1Provider
import com.microsoft.did.sdk.util.controlflow.KeyFormatException
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import org.spongycastle.jce.ECNamedCurveTable
import org.spongycastle.jce.spec.ECPublicKeySpec
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.KeyFactory

/**
 *  Converts the public key returned by library from byte array to x and y co-ordinates to be used in JWK
 */
fun publicToXY(keyData: ByteArray): Pair<String, String> {
    return when {
        // Convert uncompressed hex and hybrid hex formats of public key to x and y co-ordinates to be used in JWK format
        isPublicKeyUncompressedOrHybridHex(keyData) -> publicKeyToXYForUncompressedOrHybridHex(keyData)
        else -> throw KeyFormatException("Public key improperly formatted")
    }
}

fun convertToBigEndian(keyBytes: ByteArray): ByteBuffer {
    val keyBytesInBigEndian = ByteBuffer.allocate(32)
    keyBytesInBigEndian.put(keyBytes)
    keyBytesInBigEndian.order(ByteOrder.BIG_ENDIAN)
    keyBytesInBigEndian.array()
    return keyBytesInBigEndian
}

/**
 * Return X and Y coordinates of public key in Big Endian form from uncompressed hex
 */
fun publicKeyToXYForUncompressedOrHybridHex(keyData: ByteArray): Pair<String, String> {
    // uncompressed, bytes 1-32, and 33-end are x and y
    val x = convertToBigEndian(keyData.sliceArray(1..32))
    val y = convertToBigEndian(keyData.sliceArray(33..64))
    return Pair(
        Base64.encodeToString(x.array(), Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP),
        Base64.encodeToString(y.array(), Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    )
}

fun isPublicKeyUncompressedOrHybridHex(keyData: ByteArray): Boolean {
    return keyData.size == 65
        && (keyData[0] == Secp256k1Provider.Secp256k1Tag.UNCOMPRESSED.byte
        || keyData[0] == Secp256k1Provider.Secp256k1Tag.HYBRID_EVEN.byte
        || keyData[0] == Secp256k1Provider.Secp256k1Tag.HYBRID_ODD.byte)
}

/**
 * Generate public key in uncompressed Hex format from private key
 */
fun generatePublicKeyFromPrivateKey(privateKey: ByteArray): ByteArray {
    val keyFactory = KeyFactory.getInstance(AndroidConstants.Ec.value)
    val ecSpec = ECNamedCurveTable.getParameterSpec(Constants.SECP256K1_CURVE_NAME_EC)
    val ecPoint = ecSpec.g.multiply(BigInteger(1, privateKey))
    val pubKeySpec = ECPublicKeySpec(ecPoint, ecSpec)
    val publicKey = keyFactory.generatePublic(pubKeySpec)
    return byteArrayOf(Secp256k1Provider.Secp256k1Tag.UNCOMPRESSED.byte) + (publicKey as BCECPublicKey).q.normalize().xCoord.encoded +
        publicKey.q.normalize().yCoord.encoded
}