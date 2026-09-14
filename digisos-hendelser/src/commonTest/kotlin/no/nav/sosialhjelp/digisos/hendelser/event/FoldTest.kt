package no.nav.sosialhjelp.digisos.hendelser.event

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.serialization.SerializationException
import no.nav.sosialhjelp.digisos.hendelser.domain.SoknadsStatus
import no.nav.sosialhjelp.digisos.hendelser.fold.FoldResult
import no.nav.sosialhjelp.digisos.hendelser.fold.SoknadMetadata
import no.nav.sosialhjelp.digisos.hendelser.fold.fold
import no.nav.sosialhjelp.digisos.hendelser.fold.foldJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import no.nav.sosialhjelp.filformat.digisos.soker.SoknadsStatus as FilformatSoknadsStatus

class FoldTest {
    private val baseMetadata =
        SoknadMetadata(
            fiksDigisosId = "test-id",
            kommunenummer = "1234",
            erPapirsoknad = false,
            sistEndret = Instant.parse("2024-01-01T00:00:00Z"),
            timestampSendt = Instant.fromEpochMilliseconds(1_700_000_000_000L),
            navEksternRefId = "ref-1",
            originalSoknadDokumentlagerId = DOKUMENTLAGERID_1,
            vedleggMetadataDokumentlagerId = null,
            fagsystemNavn = null,
            fagsystemVersjon = null,
            mottakerEnhetsnummer = NAVKONTOR,
            mottakerEnhetsnavn = "Oslo",
        )

    @Test
    fun `fold produces both aggregate and event list`() =
        runTest {
            val digisosSoker =
                digisosSoker(
                    soknadsStatus(FilformatSoknadsStatus.Status.MOTTATT, tidspunkt_1),
                )

            val result: FoldResult = fold(digisosSoker, baseMetadata, emptyList())

            assertEquals(SoknadsStatus.MOTTATT, result.soknad.status)
            // SoknadSendt + SoknadsStatusEndret
            assertEquals(2, result.hendelser.size)
        }

    @Test
    fun `hendelser are sorted by tidspunkt ascending`() =
        runTest {
            val digisosSoker =
                digisosSoker(
                    soknadsStatus(FilformatSoknadsStatus.Status.FERDIGBEHANDLET, tidspunkt_3),
                    soknadsStatus(FilformatSoknadsStatus.Status.MOTTATT, tidspunkt_1),
                    soknadsStatus(FilformatSoknadsStatus.Status.UNDER_BEHANDLING, tidspunkt_2),
                )

            val result = fold(digisosSoker, baseMetadata, emptyList())

            // Result hendelser (excluding SoknadSendt) should be MOTTATT, UNDER_BEHANDLING, FERDIGBEHANDLET
            val statusHendelser =
                result.hendelser
                    .filterIsInstance<no.nav.sosialhjelp.digisos.hendelser.domain.hendelse.SoknadsStatusEndret>()
            assertEquals(SoknadsStatus.MOTTATT, statusHendelser[0].status)
            assertEquals(SoknadsStatus.UNDER_BEHANDLING, statusHendelser[1].status)
            assertEquals(SoknadsStatus.FERDIGBEHANDLET, statusHendelser[2].status)
        }

    @Test
    fun `aktive saker override - FERDIGBEHANDLET with active sak becomes UNDER_BEHANDLING`() =
        runTest {
            val digisosSoker =
                digisosSoker(
                    soknadsStatus(FilformatSoknadsStatus.Status.FERDIGBEHANDLET, tidspunkt_1),
                    saksStatus(
                        REFERANSE_1,
                        no.nav.sosialhjelp.filformat.digisos.soker.SaksStatus.Status
                            .UNDER_BEHANDLING,
                        tidspunkt = tidspunkt_2,
                    ),
                )

            val result = fold(digisosSoker, baseMetadata, emptyList())

            assertEquals(SoknadsStatus.FERDIGBEHANDLET, result.soknad.status)
            assertEquals(SoknadsStatus.UNDER_BEHANDLING, result.soknad.avledetStatus)
        }

    @Test
    fun `null digisosSoker produces empty aggregate`() =
        runTest {
            val result = fold(null, baseMetadata, emptyList())

            assertNotNull(result.soknad)
            // Only SoknadSendt from metadata seeding
            assertEquals(1, result.hendelser.size)
        }

    @Test
    fun `unknown hendelse type fails parsing`() =
        runTest {
            assertFailsWith<SerializationException> {
                foldJson(
                    digisosSokerJson =
                        """{"version":"1.0.0","avsender":{"systemnavn":"test","systemversjon":"1.0"},"hendelser":[{"type":"fremtidigHendelsestype","hendelsestidspunkt":"$tidspunkt_1"}]}""",
                    metadata = baseMetadata,
                    paakrevdeVedleggJson = null,
                )
            }
        }
}
