package no.nav.sosialhjelp.digisos.hendelser.event

import no.nav.sosialhjelp.digisos.hendelser.domain.SoknadsStatus
import no.nav.sosialhjelp.digisos.hendelser.domain.hendelse.SoknadsStatusEndret
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import no.nav.sosialhjelp.filformat.digisos.soker.SoknadsStatus as FilformatSoknadsStatus

class SoknadsStatusTest {
    @Test
    fun `MOTTATT sets status and includes mottakerNavn when mottaker present`() {
        val acc = testAccumulator()
        acc.mottaker =
            no.nav.sosialhjelp.digisos.hendelser.domain
                .NavEnhet("1234", "Oslo kommune")
        acc.apply(soknadsStatus(FilformatSoknadsStatus.Status.MOTTATT))

        assertEquals(SoknadsStatus.MOTTATT, acc.status)
        val h = acc.hendelser[0] as SoknadsStatusEndret
        // stripEnhetsnavnForKommune removes " kommune"
        assertEquals("Oslo", h.mottakerNavn)
    }

    @Test
    fun `MOTTATT with no mottaker has null mottakerNavn`() {
        val acc = testAccumulator()
        acc.apply(soknadsStatus(FilformatSoknadsStatus.Status.MOTTATT))

        val h = acc.hendelser[0] as SoknadsStatusEndret
        assertNull(h.mottakerNavn)
    }

    @Test
    fun `UNDER_BEHANDLING emits event with null mottakerNavn`() {
        val acc = testAccumulator()
        acc.apply(soknadsStatus(FilformatSoknadsStatus.Status.UNDER_BEHANDLING))

        assertEquals(SoknadsStatus.UNDER_BEHANDLING, acc.status)
        val h = acc.hendelser[0] as SoknadsStatusEndret
        assertNull(h.mottakerNavn)
    }

    @Test
    fun `FERDIGBEHANDLET sets status`() {
        val acc = testAccumulator()
        acc.apply(soknadsStatus(FilformatSoknadsStatus.Status.FERDIGBEHANDLET))
        assertEquals(SoknadsStatus.FERDIGBEHANDLET, acc.status)
    }

    @Test
    fun `UKJENT status is ignored (status unchanged)`() {
        val acc = testAccumulator()
        acc.status = SoknadsStatus.MOTTATT
        acc.apply(soknadsStatus(FilformatSoknadsStatus.Status.UKJENT))
        assertEquals(SoknadsStatus.MOTTATT, acc.status)
    }
}
