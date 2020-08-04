package no.nav.sosialhjelp.maskinporten.client

data class MaskinportenProperties(
    val tokenUrl: String,
    val clientId: String,
    val scope: String,
    val configuration: String,
    val configuration_apikey: String,
    val token_apikey: String,
    val delegation_audience: String,
    val public_jwk: String,
    val private_key_base64: String
)
