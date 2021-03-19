package no.nav.sosialhjelp.client.kommuneinfo

import no.nav.sosialhjelp.api.fiks.ErrorMessage
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.api.fiks.exceptions.FiksClientException
import no.nav.sosialhjelp.api.fiks.exceptions.FiksServerException
import no.nav.sosialhjelp.client.utils.Constants.BEARER
import no.nav.sosialhjelp.client.utils.objectMapper
import no.nav.sosialhjelp.client.utils.typeRef
import no.nav.sosialhjelp.kotlin.utils.logger
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import java.io.IOException
import java.util.Collections

interface KommuneInfoClient {

    fun get(kommunenummer: String, token: String): KommuneInfo

    fun getAll(token: String): List<KommuneInfo>
}

class KommuneInfoClientImpl(
    private val webClient: WebClient,
    private val fiksProperties: FiksProperties
) : KommuneInfoClient {

    override fun get(kommunenummer: String, token: String): KommuneInfo {
        val kommuneInfo = webClient.get()
            .uri(fiksProperties.hentKommuneInfoUrl, kommunenummer)
            .headers { it.addAll(fiksHeaders(fiksProperties, token)) }
            .retrieve()
            .bodyToMono<KommuneInfo>()
            .onErrorMap(WebClientResponseException::class.java) { e ->
                log.warn("Fiks - hentKommuneInfo feilet - ${messageUtenFnr(e)}", e)
                when {
                    e.statusCode.is4xxClientError -> FiksClientException(e.rawStatusCode, e.message?.feilmeldingUtenFnr, e)
                    else -> FiksServerException(e.rawStatusCode, e.message?.feilmeldingUtenFnr, e)
                }
            }
            .block()

        return kommuneInfo!!
    }

    override fun getAll(token: String): List<KommuneInfo> {
        val list = webClient.get()
            .uri(fiksProperties.hentAlleKommuneInfoUrl)
            .headers { it.addAll(fiksHeaders(fiksProperties, token)) }
            .retrieve()
            .bodyToMono(typeRef<List<KommuneInfo>>())
            .onErrorMap(WebClientResponseException::class.java) { e ->
                log.warn("Fiks - hentKommuneInfoForAlle feilet - ${messageUtenFnr(e)}", e)
                when {
                    e.statusCode.is4xxClientError -> FiksClientException(e.rawStatusCode, e.message?.feilmeldingUtenFnr, e)
                    else -> FiksServerException(e.rawStatusCode, e.message?.feilmeldingUtenFnr, e)
                }
            }
            .block()

        return list!!
    }

    private fun fiksHeaders(fiksProperties: FiksProperties, token: String): HttpHeaders {
        val headers = HttpHeaders()
        headers.accept = Collections.singletonList(MediaType.APPLICATION_JSON)
        headers.set(HttpHeaders.AUTHORIZATION, BEARER + token)
        headers.set(HEADER_INTEGRASJON_ID, fiksProperties.fiksIntegrasjonId)
        headers.set(HEADER_INTEGRASJON_PASSORD, fiksProperties.fiksIntegrasjonPassord)
        return headers
    }

    companion object {
        private val log by logger()

        private const val HEADER_INTEGRASJON_ID = "IntegrasjonId"
        private const val HEADER_INTEGRASJON_PASSORD = "IntegrasjonPassord"

        private fun messageUtenFnr(e: WebClientResponseException): String {
            val fiksErrorMessage = e.toFiksErrorMessage()?.feilmeldingUtenFnr
            val message = e.message?.feilmeldingUtenFnr
            return "$message - $fiksErrorMessage"
        }

        private fun <T : WebClientResponseException> T.toFiksErrorMessage(): ErrorMessage? {
            return try {
                objectMapper.readValue(this.responseBodyAsByteArray, ErrorMessage::class.java)
            } catch (e: IOException) {
                null
            }
        }

        private val ErrorMessage.feilmeldingUtenFnr: String?
            get() {
                return this.message?.feilmeldingUtenFnr
            }

        private val String.feilmeldingUtenFnr: String?
            get() {
                return this.replace(Regex("""\b[0-9]{11}\b"""), "[FNR]")
            }
    }
}
