package no.nav.sosialhjelp.idporten.client

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.util.Base64
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.kotlin.utils.logger
import no.nav.sosialhjelp.kotlin.utils.retry
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.io.File
import java.security.KeyPair
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.*

class IdPortenClient(
        private val restTemplate: RestTemplate,
        properties: IdPortenProperties
) {

    private val tokenUrl = properties.idPortenTokenUrl
    private val clientId = properties.idPortenClientId
    private val idPortenScope = properties.idPortenScope
    private val configUrl = properties.idPortenConfigUrl
    private val virksomhetSertifikatPath: String = properties.virksomhetSertifikatPath

    private val idPortenOidcConfiguration: IdPortenOidcConfiguration

    private val objectMapper = ObjectMapper()
            .registerModules(KotlinModule())
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    init {
        idPortenOidcConfiguration = runBlocking {
            log.debug("Forsøker å hente idporten-config fra $configUrl")
            val response = restTemplate.exchange(configUrl, HttpMethod.GET, HttpEntity<Nothing>(HttpHeaders()), IdPortenOidcConfiguration::class.java)
            log.info("Hentet idporten-config fra $configUrl")
            response.body!!
        }.also {
            log.info("idporten-config: OIDC configuration initialized")
        }
    }

    suspend fun requestToken(attempts: Int = 10): AccessToken =
            retry(attempts = attempts, retryableExceptions = *arrayOf(HttpServerErrorException::class)) {
                val jws = createJws()
                log.info("Got jws, getting token (virksomhetssertifikat)")
                val uriComponents = UriComponentsBuilder.fromHttpUrl(tokenUrl).build()
                val body = LinkedMultiValueMap<String, String>()
                body.add(GRANT_TYPE_PARAM, GRANT_TYPE)
                body.add(ASSERTION_PARAM, jws.token)
                val response = restTemplate.exchange(uriComponents.toUriString(), HttpMethod.POST, HttpEntity(body, HttpHeaders()), IdPortenAccessTokenResponse::class.java)
                AccessToken(response.body!!.accessToken)
            }

    fun createJws(
            expirySeconds: Int = 100,
            issuer: String = clientId,
            scope: String = idPortenScope
    ): Jws {
        require(expirySeconds <= MAX_EXPIRY_SECONDS) {
            "IdPorten: JWT expiry cannot be greater than $MAX_EXPIRY_SECONDS seconds (was $expirySeconds)"
        }


        val date = Date()
        val expDate: Date = Calendar.getInstance().let {
            it.time = date
            it.add(Calendar.SECOND, expirySeconds)
            it.time
        }
        val virksertCredentials = objectMapper.readValue<VirksertCredentials>(
                File("$virksomhetSertifikatPath/credentials.json").readText(Charsets.UTF_8)
        )

        val pair = KeyStore.getInstance("PKCS12").let { keyStore ->
            keyStore.load(
                    java.util.Base64.getDecoder().decode(File("$virksomhetSertifikatPath/key.p12.b64").readText(Charsets.UTF_8)).inputStream(),
                    virksertCredentials.password.toCharArray()
            )
            val cert = keyStore.getCertificate(virksertCredentials.alias) as X509Certificate

            KeyPair(
                    cert.publicKey,
                    keyStore.getKey(
                            virksertCredentials.alias,
                            virksertCredentials.password.toCharArray()
                    ) as PrivateKey
            ) to cert.encoded
        }


        log.info("Public certificate length ${pair.first.public.encoded.size} (virksomhetssertifikat)")

        return SignedJWT(
                JWSHeader.Builder(JWSAlgorithm.RS256).x509CertChain(mutableListOf(Base64.encode(pair.second))).build(),
                JWTClaimsSet.Builder()
                        .audience(idPortenOidcConfiguration.issuer)
                        .issuer(issuer)
                        .issueTime(date)
                        .jwtID(UUID.randomUUID().toString())
                        .expirationTime(expDate)
                        .claim(CLAIMS_SCOPE, scope)
                        .build()
        ).run {
            sign(RSASSASigner(pair.first.private))
            val jws = Jws(serialize())
            log.info("Serialized jws (virksomhetssertifikat)")
            jws
        }
    }

    companion object {
        private const val MAX_EXPIRY_SECONDS = 120
        private const val CLAIMS_SCOPE = "scope"
        private const val GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer"

        private const val GRANT_TYPE_PARAM = "grant_type"
        private const val ASSERTION_PARAM = "assertion"

        private val log by logger()
    }

    private data class VirksertCredentials(
            val alias: String,
            val password: String,
            val type: String
    )

}