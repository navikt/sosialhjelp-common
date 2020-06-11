package no.nav.sosialhjelp.kommuneinfo

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class KommuneInfoClientTest {

    private val client = KommuneInfoClient()

    @Test
    fun test() {
        val kommuneInfo = client.hentKommuneInfo("1234")

        assertNull(kommuneInfo)
    }
}

