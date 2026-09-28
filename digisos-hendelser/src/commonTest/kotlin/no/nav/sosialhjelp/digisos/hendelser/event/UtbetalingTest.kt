package no.nav.sosialhjelp.digisos.hendelser.event

import kotlinx.datetime.LocalDate
import no.nav.sosialhjelp.digisos.hendelser.domain.UtbetalingsStatus
import no.nav.sosialhjelp.digisos.hendelser.domain.hendelse.UtbetalingEndret
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import no.nav.sosialhjelp.filformat.digisos.soker.Utbetaling as FilformatUtbetaling

class UtbetalingTest {
    @Test
    fun `new utbetaling is added`() {
        val acc = testAccumulator()
        acc.apply(utbetaling(UTBETALING_REF_1, FilformatUtbetaling.Status.UTBETALT, belop = 5000.0))

        assertEquals(1, acc.utbetalinger.size)
        assertEquals("5000.0", acc.utbetalinger[0].utbetaling.belopString)
        assertEquals(UtbetalingsStatus.UTBETALT, acc.utbetalinger[0].utbetaling.status)
        assertEquals(1, acc.hendelser.filterIsInstance<UtbetalingEndret>().size)
    }

    @Test
    fun `upsert replaces existing utbetaling with same referanse`() {
        val acc = testAccumulator()
        acc.apply(utbetaling(UTBETALING_REF_1, FilformatUtbetaling.Status.PLANLAGT_UTBETALING, belop = 1000.0))
        acc.apply(utbetaling(UTBETALING_REF_1, FilformatUtbetaling.Status.UTBETALT, belop = 1000.0, tidspunkt = tidspunkt_2))

        assertEquals(1, acc.utbetalinger.size)
        assertEquals(UtbetalingsStatus.UTBETALT, acc.utbetalinger[0].utbetaling.status)
        assertEquals(2, acc.hendelser.filterIsInstance<UtbetalingEndret>().size)
    }

    @Test
    fun `annenMottaker null counts as true - kontonummer is nulled`() {
        val acc = testAccumulator()
        acc.apply(
            FilformatUtbetaling(
                hendelsestidspunkt = tidspunkt_1,
                utbetalingsreferanse = UTBETALING_REF_1,
                annenMottaker = null,
                kontonummer = "12345678901",
            ),
        )

        assertTrue(acc.utbetalinger[0].utbetaling.annenMottaker)
        assertNull(acc.utbetalinger[0].utbetaling.kontonummer)
    }

    @Test
    fun `annenMottaker false - kontonummer is preserved`() {
        val acc = testAccumulator()
        acc.apply(
            FilformatUtbetaling(
                hendelsestidspunkt = tidspunkt_1,
                utbetalingsreferanse = UTBETALING_REF_1,
                annenMottaker = false,
                kontonummer = "12345678901",
            ),
        )

        assertEquals(false, acc.utbetalinger[0].utbetaling.annenMottaker)
        assertEquals("12345678901", acc.utbetalinger[0].utbetaling.kontonummer)
    }

    @Test
    fun `stoppetDato is set to most recent STOPPET and carried forward otherwise`() {
        val acc = testAccumulator()
        acc.apply(
            utbetaling(
                UTBETALING_REF_1,
                FilformatUtbetaling.Status.PLANLAGT_UTBETALING,
                tidspunkt = "2026-01-10T10:00:00Z",
            ),
        )
        assertNull(acc.utbetalinger[0].utbetaling.stoppetDato)

        acc.apply(utbetaling(UTBETALING_REF_1, FilformatUtbetaling.Status.STOPPET, tidspunkt = "2026-01-11T10:00:00Z"))
        assertEquals(LocalDate(2026, 1, 11), acc.utbetalinger[0].utbetaling.stoppetDato)

        // A new STOPPET event updates stoppetDato to the most recent date.
        acc.apply(utbetaling(UTBETALING_REF_1, FilformatUtbetaling.Status.STOPPET, tidspunkt = "2026-01-12T10:00:00Z"))
        assertEquals(LocalDate(2026, 1, 12), acc.utbetalinger[0].utbetaling.stoppetDato)

        // A non-STOPPET status carries stoppetDato forward.
        acc.apply(utbetaling(UTBETALING_REF_1, FilformatUtbetaling.Status.UTBETALT, tidspunkt = "2026-01-13T10:00:00Z"))
        assertEquals(LocalDate(2026, 1, 12), acc.utbetalinger[0].utbetaling.stoppetDato)
    }
}
