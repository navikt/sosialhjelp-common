package no.nav.sosialhjelp.client.kommuneinfo

import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.client.utils.typeRef
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate


interface KommuneInfoClient {

    val restTemplate: RestTemplate
    val fiksProperties: FiksProperties

    fun hentKommuneInfo(kommunenummer: String, headers: HttpHeaders): KommuneInfo {
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

    fun hentAlleKommuneInfo(headers: HttpHeaders): List<KommuneInfo> {
        val response = restTemplate.exchange(
                fiksProperties.hentAlleKommuneInfoUrl,
                HttpMethod.GET,
                HttpEntity<Nothing>(headers),
                typeRef<List<KommuneInfo>>()
        )

        return response.body!!
    }
}