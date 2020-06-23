package no.nav.sosialhjelp.client.kommuneinfo

import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.client.utils.Constants.BEARER
import no.nav.sosialhjelp.client.utils.typeRef
import no.nav.sosialhjelp.idporten.client.IdPortenClient
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
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
    }

    override fun getAll(): List<KommuneInfo> {
        val headers = fiksHeaders(fiksProperties, getToken())

        val response = restTemplate.exchange(
                fiksProperties.hentAlleKommuneInfoUrl,
                HttpMethod.GET,
                HttpEntity<Nothing>(headers),
                typeRef<List<KommuneInfo>>()
        )

        return response.body!!
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
        private const val HEADER_INTEGRASJON_ID = "IntegrasjonId"
        private const val HEADER_INTEGRASJON_PASSORD = "IntegrasjonPassord"
    }
}
