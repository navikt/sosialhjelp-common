@file:OptIn(ExperimentalJsExport::class)

package no.nav.sosialhjelp.digisos.hendelser.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Immutable folded state of a søknad.
 *
 * [status] is the raw folded value from the hendelse stream. [avledetStatus] applies the
 * "aktive saker" override on top of it.
 *
 * ### A note on the `@JsExport` annotations in this file
 *
 * `kotlinx.datetime.Instant` and `LocalDate` are not annotated `@JsExport` by their library
 * (see https://github.com/Kotlin/kotlinx-datetime/issues/72 — open since 2020, won't be fixed),
 * so they cannot appear in a signature exported to JS/TypeScript. They are therefore hidden with
 * `@JsExport.Ignore` and mirrored by `...EpochMillis` / ISO-string getters alongside them.
 *
 * Primary constructors are `@JsExport.Ignore`d too: these types are fold *output*, JS only ever
 * reads them. JVM consumers are unaffected — the `Instant`/`LocalDate` properties remain the
 * primary API on that side.
 */
@JsExport
class Soknad
    @JsExport.Ignore
    constructor(
        val fiksDigisosId: String,
        val navEksternRefId: String?,
        val kommunenummer: String,
        val fagsystem: Fagsystem?,
        val erPapirsoknad: Boolean,
        @property:JsExport.Ignore val tidspunktSendt: Instant?,
        @property:JsExport.Ignore val sistEndret: Instant,
        val status: SoknadsStatus,
        val mottaker: NavEnhet?,
        /** Full tildeling history — each entry is one TildeltNavKontor hendelse. */
        val navKontorHistorikk: List<NavKontorTildeling>,
        val saker: List<Sak>,
        /**
         * Vedtak from a VedtakFattet with a blank saksreferanse.
         */
        val vedtakUtenSak: List<Vedtak>,
        /** Utbetalinger whose saksreferanse is blank or matches no known sak. */
        val utbetalingerUtenSak: List<Utbetaling>,
        /**
         * Dokumentasjon etterspurt på søknadsnivå. From JsonDokumentasjonEtterspurt or the original
         * søknad's VedleggKreves fallback.
         */
        val dokumentasjonEtterspurt: List<DokumentasjonEtterspurt>,
        val forvaltningsbrev: List<DatertDokument>,
        val forelopigSvar: DatertDokument?,
        /** Reference to the original søknad document in Fiks Dokumentlager. */
        val originalSoknad: DokumentRef?,
    ) {
        /**
         * "aktive saker" override: a FERDIGBEHANDLET søknad with a sak that has no vedtak and is
         * still UNDER_BEHANDLING is reported as UNDER_BEHANDLING, so oppgaver from that sak stay
         * visible.
         */
        val avledetStatus: SoknadsStatus
            get() =
                if (status == SoknadsStatus.FERDIGBEHANDLET &&
                    saker.any { it.vedtak.isEmpty() && it.saksStatus == SaksStatus.UNDER_BEHANDLING }
                ) {
                    SoknadsStatus.UNDER_BEHANDLING
                } else {
                    status
                }

        val tidspunktSendtEpochMillis: Double? get() = tidspunktSendt?.toEpochMilliseconds()?.toDouble()
        val sistEndretEpochMillis: Double get() = sistEndret.toEpochMilliseconds().toDouble()
    }

@JsExport
class Fagsystem(
    val systemnavn: String?,
    val systemversjon: String?,
)

/**
 * A Nav enhet identified by its enhetsnummer.
 * [navn] is resolved by the consumer via NORG.
 */
@JsExport
class NavEnhet(
    val enhetsnummer: String,
    val navn: String?,
)

@JsExport
class NavKontorTildeling
    @JsExport.Ignore
    constructor(
        @property:JsExport.Ignore val tidspunkt: Instant,
        val enhetsnummer: String,
        /** true on the first tildeling that differs from the original mottaker. */
        val erForsteTildeling: Boolean,
    ) {
        val tidspunktEpochMillis: Double get() = tidspunkt.toEpochMilliseconds().toDouble()
    }

@JsExport
class Sak(
    val referanse: String,
    /** null when the sak was synthesized from a hendelse referencing an unknown sak */
    val saksStatus: SaksStatus?,
    /** null for a synthesized sak */
    val tittel: String?,
    val vedtak: List<Vedtak>,
    val utbetalinger: List<Utbetaling>,
    val dokumentasjonkrav: List<Dokumentasjonkrav>,
    val vilkar: List<Vilkar>,
)

@JsExport
class Vedtak
    @JsExport.Ignore
    constructor(
        val dokument: DokumentRef,
        val utfall: UtfallVedtak?,
        @property:JsExport.Ignore val dato: LocalDate?,
    ) {
        /** ISO-8601 date, e.g. "2024-03-01". */
        val datoIso: String? get() = dato?.toString()
    }

@JsExport
class Utbetaling
    @JsExport.Ignore
    constructor(
        val referanse: String,
        val status: UtbetalingsStatus,
        /**
         * Stored as String to remain platform-neutral.
         * JVM consumers: call [belopAsBigDecimal] (jvmMain extension).
         * JS consumers: use [belopAsDouble].
         */
        val belopString: String,
        val beskrivelse: String?,
        @property:JsExport.Ignore val forfallsDato: LocalDate?,
        @property:JsExport.Ignore val utbetalingsDato: LocalDate?,
        @property:JsExport.Ignore val stoppetDato: LocalDate?,
        @property:JsExport.Ignore val fom: LocalDate?,
        @property:JsExport.Ignore val tom: LocalDate?,
        val mottaker: String?,
        /** true when payment goes to someone other than søker */
        val annenMottaker: Boolean,
        /** Null when [annenMottaker] is true */
        val kontonummer: String?,
        val utbetalingsmetode: String?,
        @property:JsExport.Ignore val sistEndret: Instant,
    ) {
        val belopAsDouble: Double get() = belopString.toDoubleOrNull() ?: 0.0

        val forfallsDatoIso: String? get() = forfallsDato?.toString()
        val utbetalingsDatoIso: String? get() = utbetalingsDato?.toString()
        val stoppetDatoIso: String? get() = stoppetDato?.toString()
        val fomIso: String? get() = fom?.toString()
        val tomIso: String? get() = tom?.toString()
        val sistEndretEpochMillis: Double get() = sistEndret.toEpochMilliseconds().toDouble()
    }

/** From a JsonDokumentasjonEtterspurt hendelse, or the original søknad's VedleggKreves entries. */
@JsExport
class DokumentasjonEtterspurt
    @JsExport.Ignore
    constructor(
        val referanse: String,
        val tittel: String?,
        val beskrivelse: String?,
        val status: Oppgavestatus,
        /** null for [Kilde.SOKNAD_VEDLEGG_KREVES]. */
        @property:JsExport.Ignore val frist: LocalDate?,
        /** [gruppeIdForFrist] of [frist]. Stable grouping key */
        val gruppeId: String,
        @property:JsExport.Ignore val tidspunktForKrav: Instant,
        /** null for [Kilde.SOKNAD_VEDLEGG_KREVES]. */
        val forvaltningsbrevRef: DokumentRef?,
        val kilde: Kilde,
    ) {
        val fristIso: String? get() = frist?.toString()
        val tidspunktForKravEpochMillis: Double get() = tidspunktForKrav.toEpochMilliseconds().toDouble()

        enum class Kilde { DOKUMENTASJON_ETTERSPURT, SOKNAD_VEDLEGG_KREVES }
    }

/** From a JsonDokumentasjonkrav hendelse. Always belongs to a [Sak]. */
@JsExport
class Dokumentasjonkrav
    @JsExport.Ignore
    constructor(
        val referanse: String,
        val tittel: String?,
        val beskrivelse: String?,
        val status: Oppgavestatus,
        @property:JsExport.Ignore val frist: LocalDate?,
        val gruppeId: String,
        /** References into the parent [Sak.utbetalinger]. Many-to-many */
        val utbetalingsReferanser: List<String>,
        @property:JsExport.Ignore val datoLagtTil: Instant,
    ) {
        val fristIso: String? get() = frist?.toString()
        val datoLagtTilEpochMillis: Double get() = datoLagtTil.toEpochMilliseconds().toDouble()
    }

/** From a JsonVilkar hendelse. Always belongs to a [Sak]. */
@JsExport
class Vilkar
    @JsExport.Ignore
    constructor(
        val referanse: String,
        val tittel: String?,
        val beskrivelse: String?,
        val status: Oppgavestatus,
        /** References into the parent [Sak.utbetalinger]. Many-to-many */
        val utbetalingsReferanser: List<String>,
        @property:JsExport.Ignore val datoLagtTil: Instant,
        @property:JsExport.Ignore val datoSistEndret: Instant,
    ) {
        val datoLagtTilEpochMillis: Double get() = datoLagtTil.toEpochMilliseconds().toDouble()
        val datoSistEndretEpochMillis: Double get() = datoSistEndret.toEpochMilliseconds().toDouble()
    }

/**
 * Stable grouping key for krav sharing a frist. sha256 of the frist, or of the literal string
 * "null" when absent.
 */
fun gruppeIdForFrist(frist: LocalDate?): String = sha256(frist.toString())

/** Opaque reference to a document in Fiks */
@JsExport
sealed interface DokumentRef {
    class Dokumentlager(
        val id: String,
    ) : DokumentRef

    class SvarUt(
        val id: String,
        val nr: Int,
    ) : DokumentRef
}

@JsExport
class DatertDokument
    @JsExport.Ignore
    constructor(
        val dokumentRef: DokumentRef,
        @property:JsExport.Ignore val tidspunkt: Instant,
    ) {
        val tidspunktEpochMillis: Double get() = tidspunkt.toEpochMilliseconds().toDouble()
    }

@JsExport
enum class SoknadsStatus { SENDT, MOTTATT, UNDER_BEHANDLING, FERDIGBEHANDLET, BEHANDLES_IKKE }

@JsExport
enum class SaksStatus { UNDER_BEHANDLING, IKKE_INNSYN, FERDIGBEHANDLET, BEHANDLES_IKKE, FEILREGISTRERT }

@JsExport
enum class UtbetalingsStatus { PLANLAGT_UTBETALING, UTBETALT, STOPPET, ANNULLERT }

@JsExport
enum class UtfallVedtak { INNVILGET, DELVIS_INNVILGET, AVSLATT, AVVIST }

@JsExport
enum class Oppgavestatus { RELEVANT, ANNULLERT, OPPFYLT, IKKE_OPPFYLT, LEVERT_TIDLIGERE }
