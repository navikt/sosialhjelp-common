package no.nav.sosialhjelp.digisos.hendelser.event

import no.nav.sosialhjelp.digisos.hendelser.domain.hendelse.TildeltNavKontor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TildeltNavKontorTest {
    @Test
    fun `first tildeling matching mottaker is recorded but no event emitted`() {
        val acc = testAccumulator()
        acc.mottaker =
            no.nav.sosialhjelp.digisos.hendelser.domain
                .NavEnhet(NAVKONTOR, "Kontor 1")
        acc.apply(tildeltNavKontor(NAVKONTOR))

        assertEquals(NAVKONTOR, acc.tildeltNavKontor)
        assertTrue(acc.hendelser.isEmpty(), "No event should be emitted for initial mottaker match")
        assertTrue(acc.navKontorHistorikk.isEmpty())
    }

    @Test
    fun `new kontor emits TildeltNavKontor event`() {
        val acc = testAccumulator()
        acc.apply(tildeltNavKontor(NAVKONTOR))

        assertEquals(1, acc.hendelser.size)
        val h = acc.hendelser[0] as TildeltNavKontor
        assertEquals(NAVKONTOR, h.tilEnhetsnummer)
        assertNull(h.fraEnhetsnummer)
        assertTrue(h.erForsteTildeling)
    }

    @Test
    fun `second tildeling updates mottaker and records history`() {
        val acc = testAccumulator()
        acc.apply(tildeltNavKontor(NAVKONTOR, tidspunkt_1))
        acc.apply(tildeltNavKontor(NAVKONTOR2, tidspunkt_2))

        assertEquals(2, acc.navKontorHistorikk.size)
        assertEquals(2, acc.hendelser.size)
        val h = acc.hendelser[1] as TildeltNavKontor
        assertEquals(NAVKONTOR, h.fraEnhetsnummer)
        assertEquals(NAVKONTOR2, h.tilEnhetsnummer)
        assertEquals(false, h.erForsteTildeling)
    }

    @Test
    fun `duplicate tildeling is idempotent`() {
        val acc = testAccumulator()
        acc.apply(tildeltNavKontor(NAVKONTOR))
        acc.apply(tildeltNavKontor(NAVKONTOR))

        assertEquals(1, acc.hendelser.size)
        assertEquals(1, acc.navKontorHistorikk.size)
    }

    @Test
    fun `mottaker enhet has no navn - consumer resolves via NORG`() {
        val acc = testAccumulator()
        acc.apply(tildeltNavKontor(NAVKONTOR))

        // The fold emits enhetsnummer only; navn is null (consumer's responsibility)
        assertNull(acc.mottaker?.navn)
        assertEquals(NAVKONTOR, acc.mottaker?.enhetsnummer)
    }
}
