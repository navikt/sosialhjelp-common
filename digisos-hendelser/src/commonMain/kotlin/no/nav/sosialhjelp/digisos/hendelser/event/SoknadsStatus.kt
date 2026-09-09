package no.nav.sosialhjelp.digisos.hendelser.event

import no.nav.sosialhjelp.digisos.hendelser.domain.SoknadsStatus
import no.nav.sosialhjelp.digisos.hendelser.domain.hendelse.SoknadsStatusEndret
import no.nav.sosialhjelp.digisos.hendelser.domain.stripEnhetsnavnForKommune
import no.nav.sosialhjelp.digisos.hendelser.domain.toInstant
import no.nav.sosialhjelp.filformat.digisos.soker.SoknadsStatus as FilformatSoknadsStatus

internal fun FoldAccumulator.apply(hendelse: no.nav.sosialhjelp.filformat.digisos.soker.SoknadsStatus) {
    status =
        when (hendelse.status) {
            FilformatSoknadsStatus.Status.MOTTATT -> no.nav.sosialhjelp.digisos.hendelser.domain.SoknadsStatus.MOTTATT
            FilformatSoknadsStatus.Status.UNDER_BEHANDLING -> no.nav.sosialhjelp.digisos.hendelser.domain.SoknadsStatus.UNDER_BEHANDLING
            FilformatSoknadsStatus.Status.FERDIGBEHANDLET -> no.nav.sosialhjelp.digisos.hendelser.domain.SoknadsStatus.FERDIGBEHANDLET
            FilformatSoknadsStatus.Status.BEHANDLES_IKKE -> no.nav.sosialhjelp.digisos.hendelser.domain.SoknadsStatus.BEHANDLES_IKKE
            FilformatSoknadsStatus.Status.UKJENT -> status // ignore unknown status changes
        }

    val mottakerNavn: String? =
        if (hendelse.status == FilformatSoknadsStatus.Status.MOTTATT) {
            mottaker?.navn?.let { stripEnhetsnavnForKommune(it) }
        } else {
            null
        }

    hendelser.add(
        SoknadsStatusEndret(
            tidspunkt = hendelse.hendelsestidspunkt.toInstant(),
            status = status,
            mottakerNavn = mottakerNavn,
        ),
    )
}
