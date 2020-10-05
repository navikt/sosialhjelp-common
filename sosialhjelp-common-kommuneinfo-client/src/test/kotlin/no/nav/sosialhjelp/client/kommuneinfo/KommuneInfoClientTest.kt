package no.nav.sosialhjelp.client.kommuneinfo

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.api.fiks.exceptions.FiksClientException
import no.nav.sosialhjelp.api.fiks.exceptions.FiksServerException
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.client.utils.typeRef
import no.nav.sosialhjelp.idporten.client.IdPortenClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate

internal class KommuneInfoClientTest {

    private val restTemplate: RestTemplate = mockk()
    private val fiksProperties = FiksProperties("a", "b", "id", "pw")

    private val kommuneInfoClient = KommuneInfoClientImpl(restTemplate, fiksProperties)

    private val mockKommuneInfo: KommuneInfo = mockk()

    private val token = "token"

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
    }

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

        val kommuneInfo = kommuneInfoClient.get("1234", token)

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

        assertThrows<FiksClientException> { kommuneInfoClient.get("1234", token) }
    }

    @Test
    fun `500 internal server error - kaster feil`() {
        every {
            restTemplate.exchange(
                    any(),
                    HttpMethod.GET,
                    any(),
                    KommuneInfo::class.java,
                    any()
            )
        } throws HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "error")

        assertThrows<FiksServerException> { kommuneInfoClient.get("1234", token) }
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

        val list = kommuneInfoClient.getAll(token)

        assertEquals(1, list.size)
    }
}

