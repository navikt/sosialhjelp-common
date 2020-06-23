package no.nav.sosialhjelp.client.kommuneinfo

import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.api.fiks.FiksClientException
import no.nav.sosialhjelp.api.fiks.FiksException
import no.nav.sosialhjelp.api.fiks.FiksServerException
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.client.utils.Constants.BEARER
import no.nav.sosialhjelp.client.utils.typeRef
import no.nav.sosialhjelp.idporten.client.IdPortenClient
import no.nav.sosialhjelp.kotlin.utils.logger
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import java.util.*

interface KommuneInfoClient {

    fun get(kommunenummer: String): KommuneInfo

    fun getAll(): List<KommuneInfo>
}

class KommuneInfoClientImpl(
        private val restTemplate: RestTemplate,
        private val fiksProperties: FiksProperties,
        private val idPortenClient: IdPortenClient
) : KommuneInfoClient {

    override fun get(kommunenummer: String): KommuneInfo {
        try {
            val headers = fiksHeaders(fiksProperties, getToken())
            val vars = mapOf("kommunenummer" to kommunenummer)
            val response = restTemplate.exchange(
                    fiksProperties.hentKommuneInfoUrl,
                    HttpMethod.GET,
                    HttpEntity<Nothing>(headers),
                    KommuneInfo::class.java,
                    vars
            )

            return response.body!!
        } catch (e: HttpClientErrorException) {
            val fiksErrorMessage = e.toFiksErrorMessage()?.feilmeldingUtenFnr
            val message = e.message?.feilmeldingUtenFnr
            log.warn("Fiks - hentKommuneInfo feilet - $message - $fiksErrorMessage", e)
            throw FiksClientException(e.rawStatusCode, message, e)
        } catch (e: HttpServerErrorException) {
            val fiksErrorMessage = e.toFiksErrorMessage()?.feilmeldingUtenFnr
            val message = e.message?.feilmeldingUtenFnr
            log.warn("Fiks - hentKommuneInfo feilet - $message - $fiksErrorMessage", e)
            throw FiksServerException(e.rawStatusCode, message, e)
        } catch (e: Exception) {
            log.warn("Fiks - hentKommuneInfo feilet", e)
            throw FiksException(e.message?.feilmeldingUtenFnr, e)
        }
    }

    override fun getAll(): List<KommuneInfo> {
        try {
            val headers = fiksHeaders(fiksProperties, getToken())
            val response = restTemplate.exchange(
                    fiksProperties.hentAlleKommuneInfoUrl,
                    HttpMethod.GET,
                    HttpEntity<Nothing>(headers),
                    typeRef<List<KommuneInfo>>()
            )

            return response.body!!

        } catch (e: HttpClientErrorException) {
            val fiksErrorMessage = e.toFiksErrorMessage()?.feilmeldingUtenFnr
            val message = e.message?.feilmeldingUtenFnr
            log.warn("Fiks - hentKommuneInfoForAlle feilet - $message - $fiksErrorMessage", e)
            throw FiksClientException(e.rawStatusCode, message, e)
        } catch (e: HttpServerErrorException) {
            val fiksErrorMessage = e.toFiksErrorMessage()?.feilmeldingUtenFnr
            val message = e.message?.feilmeldingUtenFnr
            log.warn("Fiks - hentKommuneInfoForAlle feilet - $message - $fiksErrorMessage", e)
            throw FiksServerException(e.rawStatusCode, message, e)
        } catch (e: Exception) {
            log.warn("Fiks - hentKommuneInfoForAlle feilet", e)
            throw FiksException(e.message?.feilmeldingUtenFnr, e)
        }
    }

    private fun fiksHeaders(fiksProperties: FiksProperties, token: String): HttpHeaders {
        val headers = HttpHeaders()
        headers.accept = Collections.singletonList(MediaType.APPLICATION_JSON)
        headers.set(HttpHeaders.AUTHORIZATION, token)
        headers.set(HEADER_INTEGRASJON_ID, fiksProperties.fiksIntegrasjonId)
        headers.set(HEADER_INTEGRASJON_PASSORD, fiksProperties.fiksIntegrasjonPassord)
        return headers
    }

    private fun getToken(): String {
        val virksomhetstoken = runBlocking { idPortenClient.requestToken() }
        return BEARER + virksomhetstoken.token
    }

    companion object {
        private val log by logger()

        private const val HEADER_INTEGRASJON_ID = "IntegrasjonId"
        private const val HEADER_INTEGRASJON_PASSORD = "IntegrasjonPassord"
    }
}
