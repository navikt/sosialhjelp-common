package no.nav.sosialhjelp.maskinporten.client

import com.fasterxml.jackson.module.kotlin.readValue
import io.jsonwebtoken.JwsHeader
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.jackson.io.JacksonSerializer
import no.nav.sosialhjelp.client.utils.objectMapper
import no.nav.sosialhjelp.kotlin.utils.logger
import org.springframework.http.HttpHeaders
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Clock
import java.util.Base64
import java.util.Date
import java.util.UUID

class MaskinportenClient(
    private val webClient: WebClient,
    private val maskinportenProperties: MaskinportenProperties
) {

    private var maskinportenOidcConfiguration: MaskinportenOidcConfiguration? = null

    // Denne kalles for å anskaffe token
    suspend fun requestToken(): AccessToken {
        if (maskinportenOidcConfiguration == null) {
            maskinportenOidcConfiguration = hentMaskinportenOidcConfiguration()
        }
        val jws = generatePrivateJWT()
        val body = LinkedMultiValueMap<String, String>()
        body.add(GRANT_TYPE_PARAM, GRANT_TYPE)
        body.add(ASSERTION_PARAM, jws.token)
        val response = webClient.post()
            .uri(maskinportenProperties.tokenUrl)
            .body(BodyInserters.fromFormData(body))
            .headers { HttpHeaders() }
            .retrieve()
            .awaitBody<MaskinportenAccessTokenResponse>()
        return AccessToken(response.accessToken)
    }

    private suspend fun hentMaskinportenOidcConfiguration(): MaskinportenOidcConfiguration {
        log.debug("Forsøker å hente maskinporten-config fra ${maskinportenProperties.configuration}")
        return webClient.get()
            .uri(maskinportenProperties.configuration)
            .retrieve()
            .awaitBody<MaskinportenOidcConfiguration>()
            .also {
                log.info("Hentet maskinporten-config fra ${maskinportenProperties.configuration}")
            }
    }

    private fun base64ToPrivateKey(privateBase64: String): PrivateKey? {
        val keyBytes: ByteArray = Base64.getDecoder().decode(privateBase64)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val fact: KeyFactory = KeyFactory.getInstance("RSA")
        return fact.generatePrivate(keySpec)
    }

    private fun generatePrivateJWT(): Jws {
        val keys = getKeys(maskinportenProperties.public_jwk).keys
        return Jws(
            Jwts.builder()
                .setHeaderParams(
                    mapOf(
                        JwsHeader.KEY_ID to keys.map { it.kid }.single(),
                        JwsHeader.ALGORITHM to keys.map { it.alg }.single()
                    )
                )
                .setAudience(maskinportenProperties.delegation_audience)
                .claim("scope", maskinportenProperties.scope)
                .setIssuer(maskinportenProperties.clientId)
                .setExpiration(Date(Clock.systemUTC().millis() + 120000))
                .setIssuedAt(Date(Clock.systemUTC().millis()))
                .setId(UUID.randomUUID().toString())
                .serializeToJsonWith(JacksonSerializer<Map<String, Any?>>(objectMapper))
                .signWith(base64ToPrivateKey(maskinportenProperties.private_key_base64) as PrivateKey, SignatureAlgorithm.RS256)
                .compact()
        )
    }

    private fun getKeys(keys: String) = objectMapper.readValue<Keys>(keys)

    companion object {
        private val log by logger()

        private const val GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer"
        private const val GRANT_TYPE_PARAM = "grant_type"
        private const val ASSERTION_PARAM = "assertion"
    }
}
