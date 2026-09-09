@file:OptIn(ExperimentalJsExport::class)

package no.nav.sosialhjelp.digisos.hendelser.domain.hendelse

import kotlinx.datetime.Instant
import no.nav.sosialhjelp.digisos.hendelser.domain.DokumentRef
import no.nav.sosialhjelp.digisos.hendelser.domain.NavEnhet
import no.nav.sosialhjelp.digisos.hendelser.domain.SaksStatus
import no.nav.sosialhjelp.digisos.hendelser.domain.Soknad
import no.nav.sosialhjelp.digisos.hendelser.domain.SoknadsStatus
import no.nav.sosialhjelp.digisos.hendelser.domain.UtbetalingsStatus
import no.nav.sosialhjelp.digisos.hendelser.domain.UtfallVedtak
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName

/**
 * Sealed hierarchy of typed domain events emitted during the fold of a søknad's hendelse stream.
 *
 * Design principle: pure facts only — NO display text, NO i18n keys, NO NORG lookups.
 * Consumers map events to their own representation:
 *  - innsyn-api: maps to HendelseTekstType / i18n keys for the frontend
 *  - modia-api: maps to Norwegian strings in Titler.kt
 *  - adminpanel: whatever the debug view needs
 *
 * Key load-bearing facts preserved for consumers:
 *  - [SoknadsStatusEndret.mottakerNavn] lets innsyn reconstruct SOKNAD_MOTTATT_MED/UTEN_KOMMUNENAVN
 *  - [TildeltNavKontor.erForsteTildeling] and [Soknad.erPapirsoknad] reconstruct the four
 *    SOKNAD_VIDERESENDT_* variants
 *  - [NavKontorTildeling] in [Soknad.navKontorHistorikk] gives modia the full routing history
 *
 * JS/TypeScript consumers discriminate with `instanceof` on the subtypes and read
 * [tidspunktEpochMillis]; see the note in `domain/Soknad.kt` on why [tidspunkt] is hidden.
 */
@JsExport
sealed interface SoknadHendelse {
    @JsExport.Ignore
    val tidspunkt: Instant

    val tidspunktEpochMillis: Double
}

@JsExport
class SoknadSendt
    @JsExport.Ignore
    constructor(
        @property:JsExport.Ignore override val tidspunkt: Instant,
        val mottaker: NavEnhet?,
        val soknadDokumentRef: DokumentRef?,
    ) : SoknadHendelse {
        override val tidspunktEpochMillis: Double get() = tidspunkt.toEpochMilliseconds().toDouble()
    }

@JsExport
class SoknadsStatusEndret
    @JsExport.Ignore
    constructor(
        @property:JsExport.Ignore override val tidspunkt: Instant,
        val status: SoknadsStatus,
        /**
         * The name of the Nav-enhet that received the application at the time the MOTTATT
         * hendelse was processed, stripped of " kommune" suffix.
         * null means the name was not available.
         * Used by innsyn to choose SOKNAD_MOTTATT_MED_KOMMUNENAVN vs SOKNAD_MOTTATT_UTEN_KOMMUNENAVN.
         */
        val mottakerNavn: String?,
    ) : SoknadHendelse {
        override val tidspunktEpochMillis: Double get() = tidspunkt.toEpochMilliseconds().toDouble()
    }

@JsExport
class TildeltNavKontor
    @JsExport.Ignore
    constructor(
        @property:JsExport.Ignore override val tidspunkt: Instant,
        val fraEnhetsnummer: String?,
        val tilEnhetsnummer: String,
        /**
         * true = the first tildeling that differs from the original mottaker.
         * Used by innsyn to choose SOKNAD_VIDERESENDT_MED_NAVKONTOR vs SOKNAD_VIDERESENDT_UTEN_NAVKONTOR.
         */
        val erForsteTildeling: Boolean,
    ) : SoknadHendelse {
        override val tidspunktEpochMillis: Double get() = tidspunkt.toEpochMilliseconds().toDouble()
    }

@JsExport
class SaksStatusEndret
    @JsExport.Ignore
    constructor(
        @property:JsExport.Ignore override val tidspunkt: Instant,
        val saksReferanse: String,
        val tittel: String?,
        val status: SaksStatus,
        val erNyeSak: Boolean,
    ) : SoknadHendelse {
        override val tidspunktEpochMillis: Double get() = tidspunkt.toEpochMilliseconds().toDouble()
    }

@JsExport
class VedtakFattet
    @JsExport.Ignore
    constructor(
        @property:JsExport.Ignore override val tidspunkt: Instant,
        val saksReferanse: String?,
        val saksTittel: String?,
        val utfall: UtfallVedtak?,
        val vedtakRef: DokumentRef,
    ) : SoknadHendelse {
        override val tidspunktEpochMillis: Double get() = tidspunkt.toEpochMilliseconds().toDouble()
    }

/**
 * Exported to JS as `DokumentasjonEtterspurtHendelse` to avoid clashing with the domain type of the
 * same name in `domain/Soknad.kt` — ES module exports are flat, so Kotlin packages don't separate them.
 */
@JsExport
@JsName("DokumentasjonEtterspurtHendelse")
class DokumentasjonEtterspurt
    @JsExport.Ignore
    constructor(
        @property:JsExport.Ignore override val tidspunkt: Instant,
        val harDokumenter: Boolean,
        val forvaltningsbrevRef: DokumentRef?,
    ) : SoknadHendelse {
        override val tidspunktEpochMillis: Double get() = tidspunkt.toEpochMilliseconds().toDouble()
    }

@JsExport
class ForelopigSvarMottatt
    @JsExport.Ignore
    constructor(
        @property:JsExport.Ignore override val tidspunkt: Instant,
        val brevRef: DokumentRef,
    ) : SoknadHendelse {
        override val tidspunktEpochMillis: Double get() = tidspunkt.toEpochMilliseconds().toDouble()
    }

@JsExport
class UtbetalingEndret
    @JsExport.Ignore
    constructor(
        @property:JsExport.Ignore override val tidspunkt: Instant,
        val utbetalingsReferanse: String,
        val status: UtbetalingsStatus,
    ) : SoknadHendelse {
        override val tidspunktEpochMillis: Double get() = tidspunkt.toEpochMilliseconds().toDouble()
    }

@JsExport
class KravEndret
    @JsExport.Ignore
    constructor(
        @property:JsExport.Ignore override val tidspunkt: Instant,
        val kravReferanse: String,
        val kravType: KravType,
    ) : SoknadHendelse {
        override val tidspunktEpochMillis: Double get() = tidspunkt.toEpochMilliseconds().toDouble()
    }

@JsExport
enum class KravType { DOKUMENTASJONKRAV, VILKAR }

@JsExport
class SoknadKravLagtTil
    @JsExport.Ignore
    constructor(
        @property:JsExport.Ignore override val tidspunkt: Instant,
        val antallKrav: Int,
    ) : SoknadHendelse {
        override val tidspunktEpochMillis: Double get() = tidspunkt.toEpochMilliseconds().toDouble()
    }

/** Fired when a DokumentasjonEtterspurt empties the oppgave-list and status != BEHANDLES_IKKE. */
@JsExport
class OppgaverTrukket
    @JsExport.Ignore
    constructor(
        @property:JsExport.Ignore override val tidspunkt: Instant,
    ) : SoknadHendelse {
        override val tidspunktEpochMillis: Double get() = tidspunkt.toEpochMilliseconds().toDouble()
    }
