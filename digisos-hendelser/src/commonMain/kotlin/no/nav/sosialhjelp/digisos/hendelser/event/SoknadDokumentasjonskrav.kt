package no.nav.sosialhjelp.digisos.hendelser.event

import kotlinx.datetime.Instant
import no.nav.sosialhjelp.digisos.hendelser.domain.DokumentasjonEtterspurt
import no.nav.sosialhjelp.digisos.hendelser.domain.Oppgavestatus
import no.nav.sosialhjelp.digisos.hendelser.domain.gruppeIdForFrist
import no.nav.sosialhjelp.digisos.hendelser.domain.hendelse.SoknadKravLagtTil
import no.nav.sosialhjelp.digisos.hendelser.domain.sha256
import no.nav.sosialhjelp.filformat.vedlegg.Vedlegg

internal fun FoldAccumulator.applySoknadKrav(
    paakrevdeVedlegg: List<Vedlegg>,
    timestampSendt: Instant,
) {
    val tidspunkt = timestampSendt

    val nyeKrav =
        paakrevdeVedlegg
            .filterNot { it.type == "annet" && it.tilleggsinfo == "annet" }
            .map {
                DokumentasjonEtterspurt(
                    referanse = sha256(timestampSendt.toEpochMilliseconds().toString()),
                    tittel = it.type,
                    beskrivelse = it.tilleggsinfo,
                    status = Oppgavestatus.RELEVANT,
                    frist = null,
                    gruppeId = gruppeIdForFrist(null),
                    tidspunktForKrav = tidspunkt,
                    forvaltningsbrevRef = null,
                    kilde = DokumentasjonEtterspurt.Kilde.SOKNAD_VEDLEGG_KREVES,
                )
            }

    dokumentasjonEtterspurt.addAll(nyeKrav)

    if (nyeKrav.isNotEmpty()) {
        hendelser.add(SoknadKravLagtTil(tidspunkt = tidspunkt, antallKrav = nyeKrav.size))
    }
}
