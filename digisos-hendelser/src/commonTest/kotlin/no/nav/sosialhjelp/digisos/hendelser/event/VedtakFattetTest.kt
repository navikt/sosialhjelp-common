package no.nav.sosialhjelp.digisos.hendelser.event

import no.nav.sosialhjelp.digisos.hendelser.domain.DokumentRef
import no.nav.sosialhjelp.digisos.hendelser.domain.UtfallVedtak
import no.nav.sosialhjelp.digisos.hendelser.domain.hendelse.VedtakFattet
import no.nav.sosialhjelp.filformat.digisos.soker.DokumentlagerFilreferanse
import no.nav.sosialhjelp.filformat.digisos.soker.SvarUtFilreferanse
import no.nav.sosialhjelp.filformat.digisos.soker.VedtakFattet.Utfall
import no.nav.sosialhjelp.filformat.digisos.soker.VedtakFattet.Vedtaksfil
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import no.nav.sosialhjelp.filformat.digisos.soker.VedtakFattet as FilformatVedtakFattet

class VedtakFattetTest {
    @Test
    fun `vedtak is added to aggregate with dokumentlager ref`() {
        val acc = testAccumulator()
        acc.apply(vedtakFattet(REFERANSE_1, Utfall.INNVILGET))

        assertEquals(1, acc.vedtak.size)
        val ref = acc.vedtak[0].vedtak.dokument
        assertTrue(ref is DokumentRef.Dokumentlager)
        assertEquals(DOKUMENTLAGERID_1, ref.id)
        assertEquals(UtfallVedtak.INNVILGET, acc.vedtak[0].vedtak.utfall)
    }

    @Test
    fun `vedtak with svarut ref is handled`() {
        val acc = testAccumulator()
        acc.apply(
            FilformatVedtakFattet(
                hendelsestidspunkt = tidspunkt_1,
                saksreferanse = REFERANSE_1,
                vedtaksfil = Vedtaksfil(referanse = SvarUtFilreferanse(id = SVARUTID, nr = SVARUT_NR)),
                utfall = Utfall.AVSLATT,
            ),
        )

        val ref = acc.vedtak[0].vedtak.dokument
        assertTrue(ref is DokumentRef.SvarUt)
        assertEquals(SVARUTID, ref.id)
        assertEquals(SVARUT_NR, ref.nr)
    }

    @Test
    fun `sak is created when saksreferanse is not yet in accumulator`() {
        val acc = testAccumulator()
        acc.apply(vedtakFattet(REFERANSE_1))

        assertEquals(1, acc.saker.size)
        assertEquals(REFERANSE_1, acc.saker[0].referanse)
    }

    @Test
    fun `saksTittel is included in event when sak has tittel`() {
        val acc = testAccumulator()
        acc.apply(saksStatus(REFERANSE_1, tittel = TITTEL_1))
        acc.apply(vedtakFattet(REFERANSE_1, tidspunkt = tidspunkt_2))

        val h = acc.hendelser.filterIsInstance<VedtakFattet>().first()
        assertEquals(TITTEL_1, h.saksTittel)
    }

    @Test
    fun `empty saksreferanse does not create a sak`() {
        val acc = testAccumulator()
        acc.apply(
            FilformatVedtakFattet(
                hendelsestidspunkt = tidspunkt_1,
                saksreferanse = "",
                vedtaksfil = Vedtaksfil(referanse = DokumentlagerFilreferanse(id = DOKUMENTLAGERID_1)),
            ),
        )

        assertEquals(1, acc.vedtak.size)
        // Empty saksreferanse: getSak returns null, no sak created
        assertTrue(acc.saker.isEmpty())

        // At the FoldResult boundary, this vedtak is exposed as an explicit orphan rather than
        // a synthesized "default" sak (unlike innsyn-api / modia-api historically did).
        val soknad = acc.toFoldResult().soknad
        assertTrue(soknad.saker.isEmpty())
        assertEquals(1, soknad.vedtakUtenSak.size)
    }
}
