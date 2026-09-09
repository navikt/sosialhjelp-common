package no.nav.sosialhjelp.digisos.hendelser.event

import no.nav.sosialhjelp.digisos.hendelser.domain.SaksStatus
import no.nav.sosialhjelp.digisos.hendelser.domain.hendelse.SaksStatusEndret
import no.nav.sosialhjelp.digisos.hendelser.domain.toInstant
import no.nav.sosialhjelp.filformat.digisos.soker.SaksStatus as FilformatSaksStatus

internal fun FoldAccumulator.apply(hendelse: FilformatSaksStatus) {
    val referanse = hendelse.referanse
    val nySaksStatus =
        when (hendelse.status) {
            FilformatSaksStatus.Status.UNDER_BEHANDLING -> SaksStatus.UNDER_BEHANDLING
            FilformatSaksStatus.Status.IKKE_INNSYN -> SaksStatus.IKKE_INNSYN
            FilformatSaksStatus.Status.BEHANDLES_IKKE -> SaksStatus.BEHANDLES_IKKE
            FilformatSaksStatus.Status.FEILREGISTRERT -> SaksStatus.FEILREGISTRERT
            FilformatSaksStatus.Status.UKJENT, null -> SaksStatus.UNDER_BEHANDLING
        }
    val tittel = hendelse.tittel
    val existing = getSak(referanse)
    val prevStatus = existing?.saksStatus
    val erNySak = existing == null

    upsertSak(referanse, nySaksStatus, tittel)

    // Emit only on meaningful transitions (same logic as innsyn-api)
    val shouldEmit =
        erNySak ||
            (nySaksStatus == SaksStatus.IKKE_INNSYN && prevStatus != SaksStatus.IKKE_INNSYN) ||
            (nySaksStatus == SaksStatus.BEHANDLES_IKKE && prevStatus != SaksStatus.BEHANDLES_IKKE) ||
            (nySaksStatus == SaksStatus.FERDIGBEHANDLET && prevStatus != SaksStatus.FERDIGBEHANDLET)

    if (shouldEmit) {
        hendelser.add(
            SaksStatusEndret(
                tidspunkt = hendelse.hendelsestidspunkt.toInstant(),
                saksReferanse = referanse,
                tittel = tittel,
                status = nySaksStatus,
                erNyeSak = erNySak,
            ),
        )
    }
}
