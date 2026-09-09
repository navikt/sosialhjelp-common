package no.nav.sosialhjelp.digisos.hendelser.event

import no.nav.sosialhjelp.digisos.hendelser.domain.SaksStatus
import no.nav.sosialhjelp.digisos.hendelser.domain.hendelse.SaksStatusEndret
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import no.nav.sosialhjelp.filformat.digisos.soker.SaksStatus as FilformatSaksStatus

class SaksStatusTest {
    @Test
    fun `new sak is created and event emitted`() {
        val acc = testAccumulator()
        acc.apply(saksStatus(referanse = REFERANSE_1, status = FilformatSaksStatus.Status.UNDER_BEHANDLING, tittel = TITTEL_1))

        assertEquals(1, acc.saker.size)
        assertEquals(SaksStatus.UNDER_BEHANDLING, acc.saker[0].saksStatus)
        assertEquals(TITTEL_1, acc.saker[0].tittel)
        assertEquals(1, acc.hendelser.size)
        val hendelse = acc.hendelser[0] as SaksStatusEndret
        assertTrue(hendelse.erNyeSak)
        assertEquals(SaksStatus.UNDER_BEHANDLING, hendelse.status)
    }

    @Test
    fun `duplicate update with same status does not emit additional event`() {
        val acc = testAccumulator()
        acc.apply(saksStatus(REFERANSE_1, FilformatSaksStatus.Status.UNDER_BEHANDLING))
        acc.apply(saksStatus(REFERANSE_1, FilformatSaksStatus.Status.UNDER_BEHANDLING))

        // second apply updates the sak but shouldEmit = false since no meaningful transition
        assertEquals(1, acc.saker.size)
        assertEquals(1, acc.hendelser.size)
    }

    @Test
    fun `transition to IKKE_INNSYN emits event`() {
        val acc = testAccumulator()
        acc.apply(saksStatus(REFERANSE_1, FilformatSaksStatus.Status.UNDER_BEHANDLING))
        acc.apply(saksStatus(REFERANSE_1, FilformatSaksStatus.Status.IKKE_INNSYN, tidspunkt = tidspunkt_2))

        val hendelser = acc.hendelser.filterIsInstance<SaksStatusEndret>()
        assertEquals(2, hendelser.size)
        assertEquals(SaksStatus.IKKE_INNSYN, hendelser[1].status)
    }

    @Test
    fun `multiple saker can coexist`() {
        val acc = testAccumulator()
        acc.apply(saksStatus(REFERANSE_1, FilformatSaksStatus.Status.UNDER_BEHANDLING))
        acc.apply(saksStatus(REFERANSE_2, FilformatSaksStatus.Status.BEHANDLES_IKKE, tittel = TITTEL_2, tidspunkt = tidspunkt_2))

        assertEquals(2, acc.saker.size)
    }
}
