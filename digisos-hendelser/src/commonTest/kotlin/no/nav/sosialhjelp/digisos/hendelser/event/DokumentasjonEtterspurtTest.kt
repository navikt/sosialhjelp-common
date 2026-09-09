package no.nav.sosialhjelp.digisos.hendelser.event

import no.nav.sosialhjelp.digisos.hendelser.domain.DokumentasjonEtterspurt
import no.nav.sosialhjelp.digisos.hendelser.domain.Oppgavestatus
import no.nav.sosialhjelp.digisos.hendelser.domain.SoknadsStatus
import no.nav.sosialhjelp.digisos.hendelser.domain.hendelse.OppgaverTrukket
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import no.nav.sosialhjelp.digisos.hendelser.domain.hendelse.DokumentasjonEtterspurt as DokumentasjonEtterspurtHendelse

class DokumentasjonEtterspurtTest {
    @Test
    fun `krav are added from dokumenter list`() {
        val acc = testAccumulator()
        acc.apply(dokumentasjonEtterspurt(dokumenttype = DOKUMENTTYPE, forvaltningsbrevId = DOKUMENTLAGERID_1))

        val oppgaver = acc.dokumentasjonEtterspurt.filter { it.kilde == DokumentasjonEtterspurt.Kilde.DOKUMENTASJON_ETTERSPURT }
        assertEquals(1, oppgaver.size)
        assertEquals(DOKUMENTTYPE, oppgaver[0].tittel)
        assertEquals(Oppgavestatus.RELEVANT, oppgaver[0].status)
    }

    @Test
    fun `second call replaces all existing DokumentasjonEtterspurt krav`() {
        val acc = testAccumulator()
        acc.apply(dokumentasjonEtterspurt(dokumenttype = "type1"))
        acc.apply(dokumentasjonEtterspurt(dokumenttype = "type2", tidspunkt = tidspunkt_2))

        val oppgaver = acc.dokumentasjonEtterspurt.filter { it.kilde == DokumentasjonEtterspurt.Kilde.DOKUMENTASJON_ETTERSPURT }
        assertEquals(1, oppgaver.size)
        assertEquals("type2", oppgaver[0].tittel)
    }

    @Test
    fun `OppgaverTrukket emitted when previous krav emptied and status not BEHANDLES_IKKE`() {
        val acc = testAccumulator()
        acc.apply(dokumentasjonEtterspurt(dokumenttype = DOKUMENTTYPE))
        // Now send an empty dokumentasjonEtterspurt (no dokumenter), no forvaltningsbrev
        acc.apply(
            no.nav.sosialhjelp.filformat.digisos.soker.DokumentasjonEtterspurt(
                hendelsestidspunkt = tidspunkt_2,
                dokumenter = emptyList(),
            ),
        )

        assertTrue(acc.hendelser.any { it is OppgaverTrukket })
    }

    @Test
    fun `OppgaverTrukket NOT emitted when status is BEHANDLES_IKKE`() {
        val acc = testAccumulator()
        acc.status = SoknadsStatus.BEHANDLES_IKKE
        acc.apply(dokumentasjonEtterspurt(dokumenttype = DOKUMENTTYPE))
        acc.apply(
            no.nav.sosialhjelp.filformat.digisos.soker.DokumentasjonEtterspurt(
                hendelsestidspunkt = tidspunkt_2,
                dokumenter = emptyList(),
            ),
        )

        assertTrue(acc.hendelser.none { it is OppgaverTrukket })
    }

    @Test
    fun `forvaltningsbrev is registered on aggregate`() {
        val acc = testAccumulator()
        acc.apply(dokumentasjonEtterspurt(forvaltningsbrevId = DOKUMENTLAGERID_1))

        assertEquals(1, acc.forvaltningsbrev.size)
    }

    @Test
    fun `DokumentasjonEtterspurt event is emitted when documents and forvaltningsbrev present`() {
        val acc = testAccumulator()
        acc.apply(dokumentasjonEtterspurt(forvaltningsbrevId = DOKUMENTLAGERID_1))

        assertTrue(acc.hendelser.any { it is DokumentasjonEtterspurtHendelse })
    }
}
