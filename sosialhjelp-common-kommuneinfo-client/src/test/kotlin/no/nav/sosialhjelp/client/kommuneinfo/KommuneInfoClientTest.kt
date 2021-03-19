package no.nav.sosialhjelp.client.kommuneinfo

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.api.fiks.exceptions.FiksClientException
import no.nav.sosialhjelp.api.fiks.exceptions.FiksServerException
import no.nav.sosialhjelp.client.utils.typeRef
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

internal class KommuneInfoClientTest {

    private val webClient: WebClient = mockk()
    private val fiksProperties = FiksProperties("a", "b", "id", "pw")

    private val kommuneInfoClient = KommuneInfoClientImpl(webClient, fiksProperties)

    private val mockKommuneInfo: KommuneInfo = mockk()

    private val token = "token"

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
    }

    @Test
    fun `skal hente kommuneinfo`() {
        every {
            webClient.get()
                .uri(any(), any<String>())
                .headers(any())
                .retrieve()
                .bodyToMono<KommuneInfo>()
                .onErrorMap(WebClientResponseException::class.java, any())
                .block()
        } returns mockKommuneInfo

        val kommuneInfo = kommuneInfoClient.get("1234", token)

        assertNotNull(kommuneInfo)
    }

    @Test
    fun `404 not found - kaster feil`() {
        every {
            webClient.get()
                .uri(any(), any<String>())
                .headers(any())
                .retrieve()
                .bodyToMono<KommuneInfo>()
                .onErrorMap(WebClientResponseException::class.java, any())
                .block()
        } throws FiksClientException(HttpStatus.NOT_FOUND.value(), "not found", null)

        assertThrows<FiksClientException> { kommuneInfoClient.get("1234", token) }
    }

    @Test
    fun `500 internal server error - kaster feil`() {
        every {
            webClient.get()
                .uri(any(), any<String>())
                .headers(any())
                .retrieve()
                .bodyToMono<KommuneInfo>()
                .onErrorMap(WebClientResponseException::class.java, any())
                .block()
        } throws FiksServerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "error", null)

        assertThrows<FiksServerException> { kommuneInfoClient.get("1234", token) }
    }

    @Test
    fun `skal hente alle kommuneinfoer`() {
        every {
            webClient.get()
                .uri(any<String>())
                .headers(any())
                .retrieve()
                .bodyToMono(typeRef<List<KommuneInfo>>())
                .onErrorMap(WebClientResponseException::class.java, any())
                .block()
        } returns listOf(mockKommuneInfo)

        val list = kommuneInfoClient.getAll(token)

        assertEquals(1, list.size)
    }
}

