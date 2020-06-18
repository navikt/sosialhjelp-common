package no.nav.sosialhjelp.client.kommuneinfo

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.client.utils.typeRef
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

internal class KommuneInfoClientTest {

    private val restTemplate: RestTemplate = mockk()
    private val fiksProperties = FiksProperties("a", "b")

    private class Client(
            override val restTemplate: RestTemplate,
            override val fiksProperties: FiksProperties
    ) : KommuneInfoClient

    private val client = Client(restTemplate, fiksProperties)

    private val mockKommuneInfo: KommuneInfo = mockk()

    @Test
    fun `skal hente kommuneinfo`() {
        every {
            restTemplate.exchange(
                    any(),
                    HttpMethod.GET,
                    any(),
                    KommuneInfo::class.java,
                    any()
            )
        } returns ResponseEntity.ok(mockKommuneInfo)

        val kommuneInfo = client.hentKommuneInfo("1234", HttpHeaders.EMPTY)

        assertNotNull(kommuneInfo)
    }

    @Test
    fun `404 not found - kaster feil`() {
        every {
            restTemplate.exchange(
                    any(),
                    HttpMethod.GET,
                    any(),
                    KommuneInfo::class.java,
                    any()
            )
        } throws HttpClientErrorException(HttpStatus.NOT_FOUND, "not found")

        assertThrows<HttpClientErrorException> { client.hentKommuneInfo("1234", HttpHeaders.EMPTY) }
    }

    @Test
    fun `skal hente alle kommuneinfoer`() {
        every {
            restTemplate.exchange(
                    any<String>(),
                    HttpMethod.GET,
                    any(),
                    typeRef<List<KommuneInfo>>()
            )
        } returns ResponseEntity.ok(listOf(mockKommuneInfo))

        val list = client.hentAlleKommuneInfo(HttpHeaders.EMPTY)

        assertEquals(1, list.size)
    }
}

