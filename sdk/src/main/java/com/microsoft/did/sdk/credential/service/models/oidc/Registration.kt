/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.oidc

import com.microsoft.did.sdk.util.Constants.CLIENT_NAME
import com.microsoft.did.sdk.util.Constants.CLIENT_PURPOSE
import com.microsoft.did.sdk.util.Constants.LOGO_URI
import com.microsoft.did.sdk.util.Constants.TERMS_AND_SERVICES_URI
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Object for relying parties to give user more details about themselves.
 */
@Serializable
data class Registration(

    // name of the relying party.
    @SerialName(CLIENT_NAME)
    val clientName: String = "",

    // purpose of the presentation request.
    @SerialName(CLIENT_PURPOSE)
    val clientPurpose: String = "",

    // terms of service url that a user can press on to see terms and service in WebView.
    @SerialName(TERMS_AND_SERVICES_URI)
    val termsOfServiceUrl: String = "",

    // logo uri if relying party wants to display their logo in presentation prompt.
    @SerialName(LOGO_URI)
    val logoUri: String = ""
)