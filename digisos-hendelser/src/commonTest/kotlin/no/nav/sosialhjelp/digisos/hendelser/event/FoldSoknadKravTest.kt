package no.nav.sosialhjelp.digisos.hendelser.event

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import no.nav.sosialhjelp.digisos.hendelser.domain.DokumentasjonEtterspurt
import no.nav.sosialhjelp.digisos.hendelser.fold.SoknadMetadata
import no.nav.sosialhjelp.digisos.hendelser.fold.fold
import no.nav.sosialhjelp.filformat.vedlegg.Vedlegg
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import no.nav.sosialhjelp.filformat.digisos.soker.DokumentasjonEtterspurt as FilformatDokumentasjonEtterspurt

class FoldSoknadKravTest {
    private val clock = FixedClock(Instant.parse("2026-10-01T12:00:00Z"))
    private val paakrevdVedlegg = Json.decodeFromString<Vedlegg>("""{"type":"faktura","tilleggsinfo":"strom","status":"VedleggKreves"}""")

    @Test
    fun `empty DokumentasjonEtterspurt after one with documents does not restore soknad krav`() {
        val result =
            fold(
                digisosSoker(
                    dokumentasjonEtterspurt(tidspunkt = "2026-09-30T10:00:00Z"),
                    emptyDokumentasjonEtterspurt("2026-09-30T11:00:00Z"),
                ),
                metadata(sentAt = "2026-09-30T12:00:00Z"),
                clock,
            ) { listOf(paakrevdVedlegg) }

        assertTrue(result.soknad.dokumentasjonEtterspurt.isEmpty())
    }

    @Test
    fun `single empty DokumentasjonEtterspurt does not add soknad krav`() {
        val result =
            fold(
                digisosSoker(emptyDokumentasjonEtterspurt("2026-09-30T10:00:00Z")),
                metadata(sentAt = "2026-09-30T12:00:00Z"),
                clock,
            ) { listOf(paakrevdVedlegg) }

        assertTrue(result.soknad.dokumentasjonEtterspurt.isEmpty())
    }

    @Test
    fun `soknad krav are added when no DokumentasjonEtterspurt exists`() {
        val result = fold(null, metadata(sentAt = "2026-09-30T12:00:00Z"), clock) { listOf(paakrevdVedlegg) }

        assertEquals(
            listOf(DokumentasjonEtterspurt.Kilde.SOKNAD_VEDLEGG_KREVES),
            result.soknad.dokumentasjonEtterspurt.map { it.kilde },
        )
    }

    @Test
    fun `soknad krav use Oslo calendar day boundary`() {
        val sentTwentyNineDaysAgoEarly = fold(null, metadata(sentAt = "2026-09-01T22:01:00Z"), clock) { listOf(paakrevdVedlegg) }
        val sentTwentyNineDaysAgoLate = fold(null, metadata(sentAt = "2026-09-02T21:59:00Z"), clock) { listOf(paakrevdVedlegg) }
        val sentThirtyDaysAgo = fold(null, metadata(sentAt = "2026-09-01T21:59:00Z"), clock) { listOf(paakrevdVedlegg) }

        assertEquals(1, sentTwentyNineDaysAgoEarly.soknad.dokumentasjonEtterspurt.size)
        assertEquals(1, sentTwentyNineDaysAgoLate.soknad.dokumentasjonEtterspurt.size)
        assertTrue(sentThirtyDaysAgo.soknad.dokumentasjonEtterspurt.isEmpty())
    }

    @Test
    fun `provider is not called when DokumentasjonEtterspurt exists`() {
        var calls = 0

        fold(
            digisosSoker(emptyDokumentasjonEtterspurt("2026-09-30T10:00:00Z")),
            metadata(sentAt = "2026-09-30T12:00:00Z"),
            clock,
        ) {
            calls++
            listOf(paakrevdVedlegg)
        }

        assertEquals(0, calls)
    }

    private fun emptyDokumentasjonEtterspurt(tidspunkt: String) =
        FilformatDokumentasjonEtterspurt(
            hendelsestidspunkt = tidspunkt,
            dokumenter = emptyList(),
            forvaltningsbrev = null,
        )

    private fun metadata(sentAt: String) =
        SoknadMetadata(
            fiksDigisosId = "test-id",
            kommunenummer = "1234",
            erPapirsoknad = false,
            sistEndret = clock.now(),
            timestampSendt = Instant.parse(sentAt),
            navEksternRefId = null,
            originalSoknadDokumentlagerId = null,
            vedleggMetadataDokumentlagerId = null,
            fagsystemNavn = null,
            fagsystemVersjon = null,
            mottakerEnhetsnummer = null,
            mottakerEnhetsnavn = null,
        )

    private class FixedClock(
        private val instant: Instant,
    ) : Clock {
        override fun now(): Instant = instant
    }
}
