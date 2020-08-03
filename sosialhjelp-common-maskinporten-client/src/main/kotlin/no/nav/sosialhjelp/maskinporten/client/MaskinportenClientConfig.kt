package com.sosialhjelp.adminpanelapi.client.maskinporten

import no.nav.sosialhjelp.maskinporten.client.getenv
import no.nav.sosialhjelp.maskinporten.client.readFile
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.client.RestTemplate

@Profile("!mock-alt")
@Configuration
class MaskinportenClientConfig(
    @Value("\${no.nav.sosialhjelp.maskinporten.token_url}") private val tokenUrl: String,
    @Value("\${no.nav.sosialhjelp.maskinporten.scope}") private val scope: String,
    @Value("\${no.nav.sosialhjelp.maskinporten.configuration}") private val configuration: String,
    @Value("\${no.nav.sosialhjelp.maskinporten.configuration_apikey}") private val configurationApikey: String,
    @Value("\${no.nav.sosialhjelp.maskinporten.token_apikey}") private val tokenApikey: String,
    @Value("\${no.nav.sosialhjelp.maskinporten.delegation_audience}") private val delegationAudience: String
) {

    @Bean
    fun maskinportenClient(restTemplate: RestTemplate): MaskinportenClient {
        return MaskinportenClient(
                restTemplate = restTemplate,
                maskinportenProperties = maskinportenProperties()
        )
    }

    fun maskinportenProperties(): MaskinportenProperties {
        return MaskinportenProperties(
                tokenUrl = tokenUrl,
                clientId = getenv("clientID", "/secrets/maskinporten/client_id").readFile() ?: "client_Id",
                scope = scope,
                configuration = configuration,
                configuration_apikey = configurationApikey,
                token_apikey = tokenApikey,
                delegation_audience = delegationAudience,
                public_jwk = getenv("public_jwk", "/secrets/maskinporten/jwk_public").readFile() ?: "public_jwk",
                private_key_base64 = getenv("private_key_base64", "/secrets/maskinporten/private_key_base64").readFile()
                        ?: "private_key_base64"
        )
    }
}
