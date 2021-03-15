package no.nav.sosialhjelp.idporten.client

import com.fasterxml.jackson.module.kotlin.readValue
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.util.Base64
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.client.utils.objectMapper
import no.nav.sosialhjelp.kotlin.utils.logger
import no.nav.sosialhjelp.kotlin.utils.retry
import org.springframework.http.HttpHeaders
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException.BadGateway
import org.springframework.web.reactive.function.client.WebClientResponseException.GatewayTimeout
import org.springframework.web.reactive.function.client.WebClientResponseException.InternalServerError
import org.springframework.web.reactive.function.client.WebClientResponseException.NotImplemented
import org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable
import org.springframework.web.reactive.function.client.awaitBody
import java.io.File
import java.security.KeyPair
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.Calendar
import java.util.Date
import java.util.UUID
import kotlin.reflect.KClass

class IdPortenClient(
    private val webClient: WebClient,
    private val idPortenProperties: IdPortenProperties
) {

    private val idPortenOidcConfiguration: IdPortenOidcConfiguration = runBlocking {
        log.debug("Forsøker å hente idporten-config fra ${idPortenProperties.configUrl}")
        webClient.get()
            .uri(idPortenProperties.configUrl)
            .retrieve()
            .awaitBody<IdPortenOidcConfiguration>()
            .also {
                log.info("idporten-config: OIDC configuration initialized")
            }
    }

    suspend fun requestToken(attempts: Int = 10, headers: HttpHeaders = HttpHeaders()): AccessToken =
        retry(attempts = attempts, retryableExceptions = serverErrors) {
            val jws = createJws()
            log.debug("Got jws, getting token (virksomhetssertifikat)")

            val body = LinkedMultiValueMap<String, String>()
            body.add(GRANT_TYPE_PARAM, GRANT_TYPE)
            body.add(ASSERTION_PARAM, jws.token)

            val response = webClient.post()
                .uri(idPortenProperties.tokenUrl)
                .body(BodyInserters.fromFormData(body))
                .headers { it.addAll(headers) }
                .retrieve()
                .awaitBody<IdPortenAccessTokenResponse>()

            AccessToken(response.accessToken, response.expiresIn)
        }

    private fun createJws(
        expirySeconds: Int = 100,
        issuer: String = idPortenProperties.clientId,
        scope: String = idPortenProperties.scope
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
            File("${idPortenProperties.virksomhetSertifikatPath}/credentials.json").readText(Charsets.UTF_8)
        )

        val pair = KeyStore.getInstance(idPortenProperties.truststoreType).let { keyStore ->
            keyStore.load(
                java.util.Base64.getDecoder().decode(
                    File("${idPortenProperties.virksomhetSertifikatPath}/${idPortenProperties.truststoreFilepath}").readText(
                        Charsets.UTF_8
                    )
                ).inputStream(),
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

        log.debug("Public certificate length ${pair.first.public.encoded.size} (virksomhetssertifikat)")

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
            log.debug("Serialized jws (virksomhetssertifikat)")
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

        private val serverErrors: Array<KClass<out Throwable>> = arrayOf(
            InternalServerError::class,
            NotImplemented::class,
            BadGateway::class,
            ServiceUnavailable::class,
            GatewayTimeout::class
        )
    }

    private data class VirksertCredentials(
        val alias: String,
        val password: String,
        val type: String
    )

}
