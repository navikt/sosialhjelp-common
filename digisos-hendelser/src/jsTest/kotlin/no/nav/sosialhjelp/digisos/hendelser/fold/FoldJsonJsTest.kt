package no.nav.sosialhjelp.digisos.hendelser.fold

import no.nav.sosialhjelp.digisos.hendelser.domain.DokumentRef
import no.nav.sosialhjelp.digisos.hendelser.domain.SoknadsStatus
import no.nav.sosialhjelp.digisos.hendelser.domain.hendelse.SoknadSendt
import no.nav.sosialhjelp.digisos.hendelser.domain.hendelse.SoknadsStatusEndret
import no.nav.sosialhjelp.digisos.hendelser.domain.hendelse.VedtakFattet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Covers [foldJson] on Node — the entrypoint the npm package exposes.
 *
 * The npm package previously shipped with no exported declarations at all (an empty `.d.ts` and a
 * zero-export bundle) because nothing was annotated `@JsExport`. These tests, plus the exported-API
 * assertions below, are here so that regression can't happen silently again.
 */
class FoldJsonJsTest {
    private val metadata =
        SoknadMetadata(
            fiksDigisosId = "test-id",
            kommunenummer = "1234",
            erPapirsoknad = false,
            sistEndretEpochMillis = 1_704_067_200_000.0,
            timestampSendtEpochMillis = 1_700_000_000_000.0,
            navEksternRefId = "ref-1",
            originalSoknadDokumentlagerId = "dok-1",
            vedleggMetadataDokumentlagerId = null,
            fagsystemNavn = null,
            fagsystemVersjon = null,
            mottakerEnhetsnummer = "1337",
            mottakerEnhetsnavn = "Oslo",
        )

    @Test
    fun `null hendelse-stream still folds SoknadSendt from metadata`() {
        val result = foldJson(null, metadata, null)

        assertEquals(SoknadsStatus.SENDT, result.soknad.status)
        assertEquals(1, result.hendelser.size)
        val sendt = assertIs<SoknadSendt>(result.hendelser[0])
        assertEquals("1337", sendt.mottaker?.enhetsnummer)
    }

    @Test
    fun `parses raw hendelse-stream JSON and folds it`() {
        val json =
            """
            {
              "version": "1.0.0",
              "avsender": { "systemnavn": "testSystem", "systemversjon": "1.0" },
              "hendelser": [
                {
                  "type": "soknadsStatus",
                  "hendelsestidspunkt": "2024-01-02T10:00:00.000Z",
                  "status": "UNDER_BEHANDLING"
                }
              ]
            }
            """.trimIndent()

        val result = foldJson(json, metadata, null)

        assertEquals(SoknadsStatus.UNDER_BEHANDLING, result.soknad.status)
        assertEquals("testSystem", result.soknad.fagsystem?.systemnavn)
        assertEquals(2, result.hendelser.size)
        val statusEndret = result.hendelser.filterIsInstance<SoknadsStatusEndret>()
        assertEquals(listOf(SoknadsStatus.UNDER_BEHANDLING), statusEndret.map { it.status })
    }

    @Test
    fun `maps DokumentRef variants`() {
        val json =
            """
            {
              "version": "1.0.0",
              "avsender": { "systemnavn": "testSystem", "systemversjon": "1.0" },
              "hendelser": [
                {
                  "type": "vedtakFattet",
                  "hendelsestidspunkt": "2024-01-02T10:00:00.000Z",
                  "saksreferanse": "sak1",
                  "utfall": "INNVILGET",
                  "vedtaksfil": { "referanse": { "type": "dokumentlager", "id": "dok-2" } }
                }
              ]
            }
            """.trimIndent()

        val result = foldJson(json, metadata, null)

        val vedtak = result.hendelser.filterIsInstance<VedtakFattet>().single()
        val ref = assertIs<DokumentRef.Dokumentlager>(vedtak.vedtakRef)
        assertEquals("dok-2", ref.id)
    }

    @Test
    fun `parses paakrevde vedlegg JSON`() {
        val vedleggJson = """[{"type":"faktura","tilleggsinfo":"strom","status":"VedleggKreves"}]"""

        val result = foldJson(null, metadata, vedleggJson)

        // timestampSendt is well over 30 days old, so the søknadskrav fallback must not fire.
        assertTrue(result.soknad.dokumentasjonEtterspurt.isEmpty())
    }

    @Test
    fun `exposes JS-friendly timestamp accessors instead of Instant`() {
        val result = foldJson(null, metadata, null)

        assertEquals(1_704_067_200_000.0, result.soknad.sistEndretEpochMillis)
        assertEquals(1_700_000_000_000.0, result.soknad.tidspunktSendtEpochMillis)
        assertNotNull(result.hendelser[0].tidspunktEpochMillis)
    }
}
