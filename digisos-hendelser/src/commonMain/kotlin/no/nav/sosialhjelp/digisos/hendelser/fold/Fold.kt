@file:OptIn(ExperimentalJsExport::class)

package no.nav.sosialhjelp.digisos.hendelser.fold

import kotlinx.datetime.Instant
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import no.nav.sosialhjelp.digisos.hendelser.domain.DokumentRef
import no.nav.sosialhjelp.digisos.hendelser.domain.Fagsystem
import no.nav.sosialhjelp.digisos.hendelser.domain.NavEnhet
import no.nav.sosialhjelp.digisos.hendelser.domain.Soknad
import no.nav.sosialhjelp.digisos.hendelser.domain.hendelse.SoknadHendelse
import no.nav.sosialhjelp.digisos.hendelser.domain.hendelse.SoknadSendt
import no.nav.sosialhjelp.digisos.hendelser.domain.toInstant
import no.nav.sosialhjelp.digisos.hendelser.event.FoldAccumulator
import no.nav.sosialhjelp.digisos.hendelser.event.apply
import no.nav.sosialhjelp.digisos.hendelser.event.applySoknadKrav
import no.nav.sosialhjelp.filformat.digisos.soker.DigisosSoker
import no.nav.sosialhjelp.filformat.digisos.soker.DokumentasjonEtterspurt
import no.nav.sosialhjelp.filformat.digisos.soker.Dokumentasjonkrav
import no.nav.sosialhjelp.filformat.digisos.soker.ForelopigSvar
import no.nav.sosialhjelp.filformat.digisos.soker.Rammevedtak
import no.nav.sosialhjelp.filformat.digisos.soker.SaksStatus
import no.nav.sosialhjelp.filformat.digisos.soker.SoknadsStatus
import no.nav.sosialhjelp.filformat.digisos.soker.TildeltNavKontor
import no.nav.sosialhjelp.filformat.digisos.soker.UkjentHendelse
import no.nav.sosialhjelp.filformat.digisos.soker.Utbetaling
import no.nav.sosialhjelp.filformat.digisos.soker.VedtakFattet
import no.nav.sosialhjelp.filformat.digisos.soker.Vilkar
import no.nav.sosialhjelp.filformat.vedlegg.Vedlegg
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName
import no.nav.sosialhjelp.digisos.hendelser.domain.SoknadsStatus as DomainSoknadsStatus

/**
 * Combined output of the fold.
 * Both the folded aggregate state and the ordered typed event list are included
 * so consumers can use either or both without reimplementing the fold themselves.
 */
@JsExport
class FoldResult(
    val soknad: Soknad,
    /** Ordered (ascending by tidspunkt) typed domain events emitted during the fold. */
    val hendelser: List<SoknadHendelse>,
)

/**
 * Metadata about the søknad that is not part of the hendelse stream.
 * Sourced from the consumer's Fiks API response (DigisosSak / OriginalSoknadNAV).
 */
@JsExport
class SoknadMetadata
    @JsExport.Ignore
    constructor(
        val fiksDigisosId: String,
        val kommunenummer: String,
        val erPapirsoknad: Boolean,
        @property:JsExport.Ignore val sistEndret: Instant,
        /** When the søknad was sent, from OriginalSoknadNAV.timestampSendt; null for paper søknader. */
        @property:JsExport.Ignore val timestampSendt: Instant?,
        val navEksternRefId: String?,
        /** Dokumentlager id of the original søknad document; null if unavailable. */
        val originalSoknadDokumentlagerId: String?,
        /** Dokumentlager id of the vedlegg metadata document */
        val vedleggMetadataDokumentlagerId: String?,
        val fagsystemNavn: String?,
        val fagsystemVersjon: String?,
        /** The initial mottaker enhetsnummer from søknad.mottaker. */
        val mottakerEnhetsnummer: String?,
        /** The initial mottaker enhetsnavn (consumer resolves via NORG if desired). */
        val mottakerEnhetsnavn: String?,
    ) {
        val sistEndretEpochMillis: Double get() = sistEndret.toEpochMilliseconds().toDouble()
        val timestampSendtEpochMillis: Double? get() = timestampSendt?.toEpochMilliseconds()?.toDouble()

        /**
         * JS/TypeScript factory, exported as `SoknadMetadata.create(...)`.
         * Timestamps are epoch milliseconds, as `Instant` cannot cross the JS export boundary.
         */
        @JsName("create")
        constructor(
            fiksDigisosId: String,
            kommunenummer: String,
            erPapirsoknad: Boolean,
            sistEndretEpochMillis: Double,
            timestampSendtEpochMillis: Double?,
            navEksternRefId: String?,
            originalSoknadDokumentlagerId: String?,
            vedleggMetadataDokumentlagerId: String?,
            fagsystemNavn: String?,
            fagsystemVersjon: String?,
            mottakerEnhetsnummer: String?,
            mottakerEnhetsnavn: String?,
        ) : this(
            fiksDigisosId = fiksDigisosId,
            kommunenummer = kommunenummer,
            erPapirsoknad = erPapirsoknad,
            sistEndret = Instant.fromEpochMilliseconds(sistEndretEpochMillis.toLong()),
            timestampSendt = timestampSendtEpochMillis?.let { Instant.fromEpochMilliseconds(it.toLong()) },
            navEksternRefId = navEksternRefId,
            originalSoknadDokumentlagerId = originalSoknadDokumentlagerId,
            vedleggMetadataDokumentlagerId = vedleggMetadataDokumentlagerId,
            fagsystemNavn = fagsystemNavn,
            fagsystemVersjon = fagsystemVersjon,
            mottakerEnhetsnummer = mottakerEnhetsnummer,
            mottakerEnhetsnavn = mottakerEnhetsnavn,
        )
    }

/**
 * Fold a [DigisosSoker] hendelse stream into a [FoldResult].
 *
 * The fold is **pure**
 */
fun fold(
    digisosSoker: DigisosSoker?,
    metadata: SoknadMetadata,
    paakrevdeVedlegg: List<Vedlegg>,
): FoldResult {
    val acc =
        FoldAccumulator(
            fiksDigisosId = metadata.fiksDigisosId,
            kommunenummer = metadata.kommunenummer,
            erPapirsoknad = metadata.erPapirsoknad,
            sistEndret = metadata.sistEndret,
            fagsystem =
                if (metadata.fagsystemNavn != null || metadata.fagsystemVersjon != null) {
                    Fagsystem(metadata.fagsystemNavn, metadata.fagsystemVersjon)
                } else if (digisosSoker?.avsender != null) {
                    Fagsystem(digisosSoker.avsender.systemnavn, digisosSoker.avsender.systemversjon)
                } else {
                    null
                },
        )

    val timestampSendt = metadata.timestampSendt
    if (timestampSendt != null && timestampSendt.toEpochMilliseconds() != 0L) {
        acc.tidspunktSendt = timestampSendt
        acc.navEksternRefId = metadata.navEksternRefId
        acc.status = DomainSoknadsStatus.SENDT

        metadata.originalSoknadDokumentlagerId?.let {
            acc.originalSoknad = DokumentRef.Dokumentlager(it)
        }

        if (metadata.mottakerEnhetsnummer != null) {
            val enhet = NavEnhet(metadata.mottakerEnhetsnummer, metadata.mottakerEnhetsnavn)
            acc.mottaker = enhet
            acc.hendelser.add(
                SoknadSendt(
                    tidspunkt = timestampSendt,
                    mottaker = enhet,
                    soknadDokumentRef = acc.originalSoknad,
                ),
            )
        }
    }

    digisosSoker
        ?.hendelser
        ?.sortedWith(hendelseComparator)
        ?.forEach { hendelse ->
            when (hendelse) {
                is SoknadsStatus -> {
                    acc.apply(hendelse)
                }

                is TildeltNavKontor -> {
                    acc.apply(hendelse)
                }

                is SaksStatus -> {
                    acc.apply(hendelse)
                }

                is VedtakFattet -> {
                    acc.apply(hendelse)
                }

                is DokumentasjonEtterspurt -> {
                    acc.apply(hendelse)
                }

                is ForelopigSvar -> {
                    acc.apply(hendelse)
                }

                is Utbetaling -> {
                    acc.apply(hendelse)
                }

                is Vilkar -> {
                    acc.apply(hendelse)
                }

                is Dokumentasjonkrav -> {
                    acc.apply(hendelse)
                }

                is Rammevedtak -> {
                    acc.apply(hendelse)
                }

                is UkjentHendelse -> { /* forward-compat: new hendelse types are silently ignored */ }
            }
        }

    if (timestampSendt != null &&
        !acc.harDokumentasjonEtterspurt() &&
        soknadSendtForMindreEnn30DagerSiden(timestampSendt)
    ) {
        acc.applySoknadKrav(paakrevdeVedlegg, timestampSendt)
    }

    return acc.toFoldResult()
}

private val hendelseComparator: Comparator<no.nav.sosialhjelp.filformat.digisos.soker.Hendelse> =
    compareBy<no.nav.sosialhjelp.filformat.digisos.soker.Hendelse> {
        it.hendelsestidspunkt.toInstant()
    }.thenComparator { a, b -> compareByType(a, b) }
        .thenComparator { a, b ->
            if (a is SoknadsStatus && b is SoknadsStatus) mottattBeforeUnderBehandling(a, b) else 0
        }

private fun compareByType(
    a: no.nav.sosialhjelp.filformat.digisos.soker.Hendelse,
    b: no.nav.sosialhjelp.filformat.digisos.soker.Hendelse,
): Int =
    when {
        a is Utbetaling && (b is Vilkar || b is Dokumentasjonkrav) -> -1
        b is Utbetaling && (a is Vilkar || a is Dokumentasjonkrav) -> 1
        else -> 0
    }

private fun mottattBeforeUnderBehandling(
    a: SoknadsStatus,
    b: SoknadsStatus,
): Int =
    when {
        a.status == SoknadsStatus.Status.MOTTATT && b.status == SoknadsStatus.Status.UNDER_BEHANDLING -> -1
        b.status == SoknadsStatus.Status.MOTTATT && a.status == SoknadsStatus.Status.UNDER_BEHANDLING -> 1
        else -> 0
    }

private fun soknadSendtForMindreEnn30DagerSiden(timestampSendt: Instant): Boolean {
    val now =
        kotlinx.datetime.Clock.System
            .now()
    val thirtyDays = 30L * 24 * 60 * 60 * 1000
    return (now.toEpochMilliseconds() - timestampSendt.toEpochMilliseconds()) < thirtyDays
}

// ---------------------------------------------------------------------------
// JSON entrypoint
// ---------------------------------------------------------------------------

private val foldJsonParser =
    Json {
        ignoreUnknownKeys = true
    }

/**
 * [fold], but taking the Fiks documents as raw JSON.
 *
 * Both [DigisosSoker] and [Vedlegg] come from `filformat-kmp` and are not `@JsExport`-annotated,
 * so they cannot be constructed from JS/TypeScript. Taking JSON sidesteps that — and matches how
 * the data actually arrives, since consumers fetch these documents as JSON from Fiks anyway.
 *
 * This is the entrypoint the npm package exposes; JVM consumers that already hold parsed objects
 * should call [fold] directly.
 *
 * @param digisosSokerJson raw `DigisosSoker` JSON, or null when the søknad has no hendelse stream yet.
 * @param paakrevdeVedleggJson raw JSON array of `Vedlegg`, or null for none.
 */
@JsExport
fun foldJson(
    digisosSokerJson: String?,
    metadata: SoknadMetadata,
    paakrevdeVedleggJson: String?,
): FoldResult =
    fold(
        digisosSoker = digisosSokerJson?.let { foldJsonParser.decodeFromString(DigisosSoker.serializer(), it) },
        metadata = metadata,
        paakrevdeVedlegg =
            paakrevdeVedleggJson
                ?.let { foldJsonParser.decodeFromString(ListSerializer(Vedlegg.serializer()), it) }
                ?: emptyList(),
    )
