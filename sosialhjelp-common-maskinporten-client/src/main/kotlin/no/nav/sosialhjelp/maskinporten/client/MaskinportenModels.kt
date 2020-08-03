package com.sosialhjelp.adminpanelapi.client.maskinporten

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class MaskinportenAccessTokenResponse(
    @JsonProperty(value = "access_token", required = true) val accessToken: String,
    @JsonProperty(value = "expires_in", required = true) val expiresIn: Int,
    @JsonProperty(value = "scope", required = true) val scope: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MaskinportenOidcConfiguration(
    @JsonProperty(value = "issuer", required = true) val issuer: String,
    @JsonProperty(value = "token_endpoint", required = true) val tokenEndpoint: String
)

data class AccessToken(
    val token: String
)

data class Jws(
    val token: String
)

data class Keys(
    val keys: List<Jwks>
)

data class Jwks(
    val kty: String,
    val e: String,
    val use: String,
    val kid: String,
    val alg: String,
    val n: String
)
