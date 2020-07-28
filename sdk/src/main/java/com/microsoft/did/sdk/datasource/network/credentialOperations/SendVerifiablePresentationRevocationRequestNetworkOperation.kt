// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.network.credentialOperations

import com.microsoft.did.sdk.credential.models.RevocationReceipt
import com.microsoft.did.sdk.credential.service.models.serviceResponses.RevocationServiceResponse
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.datasource.network.PostNetworkOperation
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.RevocationException
import com.microsoft.did.sdk.util.serializer.Serializer
import retrofit2.Response

class SendVerifiablePresentationRevocationRequestNetworkOperation (
    url: String,
    serializedResponse: String,
    apiProvider: ApiProvider,
    private val serializer: Serializer
) : PostNetworkOperation<RevocationServiceResponse, RevocationReceipt>() {
    override val call: suspend () -> Response<RevocationServiceResponse> = { apiProvider.revocationApis.sendResponse(url, serializedResponse) }

    override fun onSuccess(response: Response<RevocationServiceResponse>): Result<RevocationReceipt> {
        val receipts = response.body()?.receipt?.entries
        val serializedReceipt = receipts?.first()?.value ?: throw RevocationException("No Receipt in revocation response body")
        val revocationReceipt = unwrapRevocationReceipt(serializedReceipt, serializer)
        return Result.Success(revocationReceipt)
    }

    fun unwrapRevocationReceipt(signedReceipt: String, serializer: Serializer): RevocationReceipt {
        val token = JwsToken.deserialize(signedReceipt, serializer)
        return serializer.parse(RevocationReceipt.serializer(), token.content())
    }
}