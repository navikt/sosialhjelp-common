package no.nav.sosialhjelp.client.kommuneinfo

import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.api.fiks.exceptions.FiksClientException
import no.nav.sosialhjelp.api.fiks.exceptions.FiksServerException
import no.nav.sosialhjelp.client.utils.Constants.BEARER
import no.nav.sosialhjelp.client.utils.typeRef
import no.nav.sosialhjelp.kotlin.utils.logger
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
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
            .onStatus(HttpStatus::is4xxClientError) {
                it.createException()
                    .map { e ->
                        log.warn("Fiks - hentKommuneInfo feilet - ${messageUtenFnr(e)}", e)
                        FiksClientException(e.rawStatusCode, e.message?.feilmeldingUtenFnr, e)
                    }
            }
            .onStatus(HttpStatus::is5xxServerError) {
                it.createException()
                    .map { e ->
                        log.warn("Fiks - hentKommuneInfo feilet - ${messageUtenFnr(e)}", e)
                        throw FiksServerException(e.rawStatusCode, e.message?.feilmeldingUtenFnr, e)
                    }
            }
            .bodyToMono<KommuneInfo>()
            .block()

        return kommuneInfo!!
    }

    override fun getAll(token: String): List<KommuneInfo> {
        val list = webClient.get()
            .uri(fiksProperties.hentAlleKommuneInfoUrl)
            .headers { it.addAll(fiksHeaders(fiksProperties, token)) }
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError) {
                it.createException()
                    .map { e ->
                        log.warn("Fiks - hentKommuneInfoForAlle feilet - ${messageUtenFnr(e)}", e)
                        FiksClientException(e.rawStatusCode, e.message?.feilmeldingUtenFnr, e)
                    }
            }
            .onStatus(HttpStatus::is5xxServerError) {
                it.createException()
                    .map { e ->
                        log.warn("Fiks - hentKommuneInfoForAlle feilet - ${messageUtenFnr(e)}", e)
                        throw FiksServerException(e.rawStatusCode, e.message?.feilmeldingUtenFnr, e)
                    }
            }
            .bodyToMono(typeRef<List<KommuneInfo>>())
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
    }
}
