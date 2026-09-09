package no.nav.sosialhjelp.digisos.hendelser.event

import no.nav.sosialhjelp.digisos.hendelser.domain.DokumentRef
import no.nav.sosialhjelp.digisos.hendelser.domain.UtfallVedtak
import no.nav.sosialhjelp.digisos.hendelser.domain.Vedtak
import no.nav.sosialhjelp.digisos.hendelser.domain.hendelse.VedtakFattet
import no.nav.sosialhjelp.digisos.hendelser.domain.toInstant
import no.nav.sosialhjelp.digisos.hendelser.domain.toLocalDateOslo
import no.nav.sosialhjelp.filformat.digisos.soker.DokumentlagerFilreferanse
import no.nav.sosialhjelp.filformat.digisos.soker.SvarUtFilreferanse
import no.nav.sosialhjelp.filformat.digisos.soker.VedtakFattet as FilformatVedtakFattet

internal fun FoldAccumulator.apply(hendelse: FilformatVedtakFattet) {
    val dokumentRef: DokumentRef =
        when (val ref = hendelse.vedtaksfil.referanse) {
            is DokumentlagerFilreferanse -> DokumentRef.Dokumentlager(ref.id)
            is SvarUtFilreferanse -> DokumentRef.SvarUt(ref.id, ref.nr)
            else -> error("Ikke støttet filreferanse-type: ${ref.type}")
        }

    val utfall =
        when (hendelse.utfall) {
            FilformatVedtakFattet.Utfall.INNVILGET -> UtfallVedtak.INNVILGET
            FilformatVedtakFattet.Utfall.DELVIS_INNVILGET -> UtfallVedtak.DELVIS_INNVILGET
            FilformatVedtakFattet.Utfall.AVSLATT -> UtfallVedtak.AVSLATT
            FilformatVedtakFattet.Utfall.AVVIST -> UtfallVedtak.AVVIST
            FilformatVedtakFattet.Utfall.UKJENT, null -> null
        }

    val saksReferanse = hendelse.saksreferanse.takeIf { it.isNotBlank() }
    val sak = if (saksReferanse != null) getSak(saksReferanse) ?: upsertSak(saksReferanse, null, null) else null
    val tidspunkt = hendelse.hendelsestidspunkt.toInstant()

    vedtak.add(
        FlatVedtak(
            vedtak =
                Vedtak(
                    dokument = dokumentRef,
                    utfall = utfall,
                    dato = tidspunkt.toLocalDateOslo(),
                ),
            saksReferanse = saksReferanse,
        ),
    )

    hendelser.add(
        VedtakFattet(
            tidspunkt = tidspunkt,
            saksReferanse = saksReferanse,
            saksTittel = sak?.tittel,
            utfall = utfall,
            vedtakRef = dokumentRef,
        ),
    )
}
