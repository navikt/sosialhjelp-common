package no.nav.sosialhjelp.maskinporten.client

import com.fasterxml.jackson.module.kotlin.readValue
import io.jsonwebtoken.JwsHeader
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.JacksonSerializer
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.client.utils.objectMapper
import no.nav.sosialhjelp.kotlin.utils.logger
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Clock
import java.util.Base64
import java.util.Date
import java.util.UUID

class MaskinportenClient(
    private val restTemplate: RestTemplate,
    private val maskinportenProperties: MaskinportenProperties

) {

    private var maskinportenOidcConfiguration: MaskinportenOidcConfiguration

    init {
        maskinportenOidcConfiguration = runBlocking {
            val configUrl = maskinportenProperties.configuration
            log.debug("Forsøker å hente idporten-config fra $configUrl")
            val response = restTemplate.exchange(configUrl, HttpMethod.GET, HttpEntity<Nothing>(HttpHeaders()), MaskinportenOidcConfiguration::class.java)
            log.info("Hentet idporten-config fra $configUrl")
            response.body!!
        }
    }

    fun getKeys(keys: String) = objectMapper.readValue<Keys>(keys)

    // Denne kalles for å anskaffe token
    fun requestToken(): AccessToken {
        val jws = generatePrivateJWT()
        val uriComponents = UriComponentsBuilder.fromHttpUrl(maskinportenProperties.tokenUrl).build()
        val body = LinkedMultiValueMap<String, String>()
        body.add(GRANT_TYPE_PARAM, GRANT_TYPE)
        body.add(ASSERTION_PARAM, jws.token)
        val response = restTemplate.exchange(uriComponents.toUriString(), HttpMethod.POST, HttpEntity(body, HttpHeaders()), MaskinportenAccessTokenResponse::class.java)
        return AccessToken(response.body!!.accessToken)
    }

    internal fun base64ToPrivateKey(privateBase64: String): PrivateKey? {
        val keyBytes: ByteArray = Base64.getDecoder().decode(privateBase64)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val fact: KeyFactory = KeyFactory.getInstance("RSA")
        return fact.generatePrivate(keySpec)
    }

    fun generatePrivateJWT(): Jws {
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

    companion object {
        private val log by logger()

        private const val GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer"
        private const val GRANT_TYPE_PARAM = "grant_type"
        private const val ASSERTION_PARAM = "assertion"
    }
}
