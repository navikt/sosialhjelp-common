package no.nav.sosialhjelp.digisos.hendelser.event

import no.nav.sosialhjelp.filformat.digisos.soker.Rammevedtak

/** Rammevedtak is a no-op — not shown to users. */
@Suppress("UnusedParameter")
internal fun FoldAccumulator.apply(hendelse: Rammevedtak) {
    // intentionally empty
}
