package no.nav.sosialhjelp.digisos.hendelser.event

import no.nav.sosialhjelp.digisos.hendelser.domain.SaksStatus
import no.nav.sosialhjelp.digisos.hendelser.domain.gruppeIdForFrist
import no.nav.sosialhjelp.digisos.hendelser.domain.sha256
import no.nav.sosialhjelp.digisos.hendelser.domain.toLocalDate
import no.nav.sosialhjelp.filformat.digisos.soker.VedtakFattet.Utfall
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Assembly tests for the nested [no.nav.sosialhjelp.digisos.hendelser.domain.Soknad] tree,
 * exercised via [FoldAccumulator.toFoldResult] rather than the individual per-hendelse handlers.
 */
class SoknadAssemblyTest {
    @Test
    fun `vedtak, utbetaling og krav nestes under riktig sak`() {
        val acc = testAccumulator()
        acc.apply(saksStatus(REFERANSE_1, tittel = TITTEL_1))
        acc.apply(vedtakFattet(REFERANSE_1, Utfall.INNVILGET, tidspunkt = tidspunkt_2))
        acc.apply(utbetaling(referanse = UTBETALING_REF_1, saksreferanse = REFERANSE_1, tidspunkt = tidspunkt_2))
        acc.apply(
            dokumentasjonkrav(
                referanse = DOKUMENTASJONKRAV_REF_1,
                saksreferanse = REFERANSE_1,
                tidspunkt = tidspunkt_2,
            ),
        )
        acc.apply(vilkar(referanse = VILKAR_REF_1, saksreferanse = REFERANSE_1, tidspunkt = tidspunkt_2))

        val sak =
            acc
                .toFoldResult()
                .soknad.saker
                .single()
        assertEquals(REFERANSE_1, sak.referanse)
        assertEquals(1, sak.vedtak.size)
        assertEquals(1, sak.utbetalinger.size)
        assertEquals(1, sak.dokumentasjonkrav.size)
        assertEquals(1, sak.vilkar.size)
    }

    @Test
    fun `utbetaling med ukjent saksreferanse havner i utbetalingerUtenSak`() {
        val acc = testAccumulator()
        acc.apply(utbetaling(referanse = UTBETALING_REF_1, saksreferanse = "ukjent-sak"))

        val soknad = acc.toFoldResult().soknad
        assertTrue(soknad.saker.isEmpty())
        assertEquals(1, soknad.utbetalingerUtenSak.size)
    }

    @Test
    fun `dokumentasjonkrav med ukjent saksreferanse gir syntetisk sak`() {
        val acc = testAccumulator()
        acc.apply(dokumentasjonkrav(referanse = DOKUMENTASJONKRAV_REF_1, saksreferanse = "ukjent-sak"))

        val sak =
            acc
                .toFoldResult()
                .soknad.saker
                .single()
        assertEquals("ukjent-sak", sak.referanse)
        assertEquals(null, sak.saksStatus)
        assertEquals(null, sak.tittel)
        assertEquals(1, sak.dokumentasjonkrav.size)
    }

    @Test
    fun `vilkar med ukjent saksreferanse gir syntetisk sak`() {
        val acc = testAccumulator()
        acc.apply(vilkar(referanse = VILKAR_REF_1, saksreferanse = "ukjent-sak-2"))

        val sak =
            acc
                .toFoldResult()
                .soknad.saker
                .single()
        assertEquals("ukjent-sak-2", sak.referanse)
        assertEquals(null, sak.saksStatus)
        assertEquals(1, sak.vilkar.size)
    }

    @Test
    fun `gruppeId er sha256 av frist, eller av strengen null naar frist mangler`() {
        assertEquals(sha256("null"), gruppeIdForFrist(null))
        val frist = innsendelsesfrist.toLocalDate()
        assertEquals(sha256(frist.toString()), gruppeIdForFrist(frist))
    }

    @Test
    fun `avledetStatus overstyres naar en sak mangler vedtak og er under behandling`() {
        val acc = testAccumulator()
        acc.status = no.nav.sosialhjelp.digisos.hendelser.domain.SoknadsStatus.FERDIGBEHANDLET
        acc.apply(saksStatus(REFERANSE_1, status = no.nav.sosialhjelp.filformat.digisos.soker.SaksStatus.Status.UNDER_BEHANDLING))

        val soknad = acc.toFoldResult().soknad
        assertEquals(SaksStatus.UNDER_BEHANDLING, soknad.saker.single().saksStatus)
        assertEquals(
            no.nav.sosialhjelp.digisos.hendelser.domain.SoknadsStatus.UNDER_BEHANDLING,
            soknad.avledetStatus,
        )
    }
}
