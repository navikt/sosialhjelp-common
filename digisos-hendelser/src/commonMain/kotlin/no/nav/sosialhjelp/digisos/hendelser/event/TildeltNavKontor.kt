package no.nav.sosialhjelp.digisos.hendelser.event

import no.nav.sosialhjelp.digisos.hendelser.domain.NavEnhet
import no.nav.sosialhjelp.digisos.hendelser.domain.NavKontorTildeling
import no.nav.sosialhjelp.digisos.hendelser.domain.hendelse.TildeltNavKontor
import no.nav.sosialhjelp.digisos.hendelser.domain.toInstant
import no.nav.sosialhjelp.filformat.digisos.soker.TildeltNavKontor as FilformatTildeltNavKontor

/**
 * The fold never resolves NORG enhet names — that is the consumer's responsibility post-fold.
 * We record the enhetsnummer only; consumers resolve navn via NORG and build their own display text.
 */
internal fun FoldAccumulator.apply(hendelse: FilformatTildeltNavKontor) {
    // Idempotent: ignore if same kontor was already set
    if (hendelse.navKontor == tildeltNavKontor) return

    // First tildeling matching the original mottaker — record tracking var, no event emitted
    if (hendelse.navKontor == mottaker?.enhetsnummer) {
        tildeltNavKontor = hendelse.navKontor
        return
    }

    val fraEnhetsnummer = mottaker?.enhetsnummer
    val erForsteTildeling = navKontorHistorikk.isEmpty()
    tildeltNavKontor = hendelse.navKontor
    val tidspunkt = hendelse.hendelsestidspunkt.toInstant()

    // Update mottaker with enhetsnummer only; consumer resolves the name
    mottaker = NavEnhet(enhetsnummer = hendelse.navKontor, navn = null)

    navKontorHistorikk.add(
        NavKontorTildeling(
            tidspunkt = tidspunkt,
            enhetsnummer = hendelse.navKontor,
            erForsteTildeling = erForsteTildeling,
        ),
    )

    hendelser.add(
        TildeltNavKontor(
            tidspunkt = tidspunkt,
            fraEnhetsnummer = fraEnhetsnummer,
            tilEnhetsnummer = hendelse.navKontor,
            erForsteTildeling = erForsteTildeling,
        ),
    )
}
