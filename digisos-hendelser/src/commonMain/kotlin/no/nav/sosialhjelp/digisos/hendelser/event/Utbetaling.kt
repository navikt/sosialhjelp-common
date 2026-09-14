package no.nav.sosialhjelp.digisos.hendelser.event

import no.nav.sosialhjelp.digisos.hendelser.domain.Utbetaling
import no.nav.sosialhjelp.digisos.hendelser.domain.UtbetalingsStatus
import no.nav.sosialhjelp.digisos.hendelser.domain.hendelse.UtbetalingEndret
import no.nav.sosialhjelp.digisos.hendelser.domain.toBelopString
import no.nav.sosialhjelp.digisos.hendelser.domain.toInstant
import no.nav.sosialhjelp.digisos.hendelser.domain.toLocalDate
import no.nav.sosialhjelp.digisos.hendelser.domain.toLocalDateOslo
import no.nav.sosialhjelp.filformat.digisos.soker.Utbetaling as FilformatUtbetaling

internal fun FoldAccumulator.apply(hendelse: FilformatUtbetaling) {
    val prevUtbetaling = getUtbetaling(hendelse.utbetalingsreferanse)
    val annenMottaker = isAnnenMottaker(hendelse)
    val status =
        when (hendelse.status) {
            FilformatUtbetaling.Status.PLANLAGT_UTBETALING -> UtbetalingsStatus.PLANLAGT_UTBETALING
            FilformatUtbetaling.Status.UTBETALT -> UtbetalingsStatus.UTBETALT
            FilformatUtbetaling.Status.STOPPET -> UtbetalingsStatus.STOPPET
            FilformatUtbetaling.Status.ANNULLERT -> UtbetalingsStatus.ANNULLERT
            null -> UtbetalingsStatus.PLANLAGT_UTBETALING
        }

    val tidspunkt = hendelse.hendelsestidspunkt.toInstant()

    val utbetaling =
        Utbetaling(
            referanse = hendelse.utbetalingsreferanse,
            status = status,
            belopString = (hendelse.belop ?: 0.0).toBelopString(),
            beskrivelse = hendelse.beskrivelse,
            forfallsDato = hendelse.forfallsdato?.toLocalDate(),
            utbetalingsDato = hendelse.utbetalingsdato?.toLocalDate(),
            stoppetDato =
                if (hendelse.status == FilformatUtbetaling.Status.STOPPET) {
                    tidspunkt.toLocalDateOslo()
                } else {
                    prevUtbetaling?.stoppetDato
                },
            fom = hendelse.fom?.toLocalDate(),
            tom = hendelse.tom?.toLocalDate(),
            mottaker = hendelse.mottaker,
            annenMottaker = annenMottaker,
            kontonummer = hendelse.kontonummer.takeUnless { annenMottaker },
            utbetalingsmetode = hendelse.utbetalingsmetode,
            sistEndret = tidspunkt,
        )

    upsertUtbetaling(FlatUtbetaling(utbetaling = utbetaling, saksReferanse = hendelse.saksreferanse))

    hendelser.add(
        UtbetalingEndret(
            tidspunkt = tidspunkt,
            utbetalingsReferanse = hendelse.utbetalingsreferanse,
            status = status,
        ),
    )
}

/** annenMottaker == null counts as true — fail-safe default. */
private fun isAnnenMottaker(hendelse: FilformatUtbetaling) = hendelse.annenMottaker == null || hendelse.annenMottaker == true
