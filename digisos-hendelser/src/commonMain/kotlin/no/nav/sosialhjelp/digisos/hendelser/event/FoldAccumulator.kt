package no.nav.sosialhjelp.digisos.hendelser.event

import kotlinx.datetime.Instant
import no.nav.sosialhjelp.digisos.hendelser.domain.DatertDokument
import no.nav.sosialhjelp.digisos.hendelser.domain.DokumentRef
import no.nav.sosialhjelp.digisos.hendelser.domain.DokumentasjonEtterspurt
import no.nav.sosialhjelp.digisos.hendelser.domain.Dokumentasjonkrav
import no.nav.sosialhjelp.digisos.hendelser.domain.Fagsystem
import no.nav.sosialhjelp.digisos.hendelser.domain.NavEnhet
import no.nav.sosialhjelp.digisos.hendelser.domain.NavKontorTildeling
import no.nav.sosialhjelp.digisos.hendelser.domain.Sak
import no.nav.sosialhjelp.digisos.hendelser.domain.SaksStatus
import no.nav.sosialhjelp.digisos.hendelser.domain.Soknad
import no.nav.sosialhjelp.digisos.hendelser.domain.SoknadsStatus
import no.nav.sosialhjelp.digisos.hendelser.domain.Utbetaling
import no.nav.sosialhjelp.digisos.hendelser.domain.Vedtak
import no.nav.sosialhjelp.digisos.hendelser.domain.Vilkar
import no.nav.sosialhjelp.digisos.hendelser.domain.hendelse.SoknadHendelse
import no.nav.sosialhjelp.digisos.hendelser.fold.FoldResult

/**
 * Mutable accumulator used during the fold of a søknad's hendelse stream.
 * After all hendelser have been applied, call [toFoldResult] to produce the immutable output.
 *
 * The typed event list is collected in [hendelser] as a side output during the fold.
 *
 * Internally the accumulator stays flat — vedtak, utbetalinger and krav are tracked alongside
 * their (possibly unresolved) saksreferanse. [toFoldResult] is the only place the nested tree
 * on [Soknad] is assembled, since consumer-facing shape (nested under [Sak]) and mutation-time
 * shape (keyed lookup by string referanse) want different representations.
 */
internal data class FoldAccumulator(
    var fagsystem: Fagsystem? = null,
    var navEksternRefId: String? = null,
    var fiksDigisosId: String = "",
    var kommunenummer: String = "",
    var erPapirsoknad: Boolean = false,
    var tidspunktSendt: Instant? = null,
    var sistEndret: Instant = Instant.DISTANT_PAST,
    var status: SoknadsStatus = SoknadsStatus.SENDT,
    var mottaker: NavEnhet? = null,
    val navKontorHistorikk: MutableList<NavKontorTildeling> = mutableListOf(),
    /** Track kontor enhetsnummer for idempotency checks in TildeltNavKontor.apply. */
    var tildeltNavKontor: String? = null,
    val saker: MutableList<Sak> = mutableListOf(),
    val vedtak: MutableList<FlatVedtak> = mutableListOf(),
    val utbetalinger: MutableList<FlatUtbetaling> = mutableListOf(),
    val dokumentasjonkrav: MutableList<FlatDokumentasjonkrav> = mutableListOf(),
    val vilkar: MutableList<FlatVilkar> = mutableListOf(),
    val dokumentasjonEtterspurt: MutableList<DokumentasjonEtterspurt> = mutableListOf(),
    val forvaltningsbrev: MutableList<DatertDokument> = mutableListOf(),
    var forelopigSvar: DatertDokument? = null,
    var originalSoknad: DokumentRef? = null,
    /** Events emitted during the fold, in emission order (sorted by tidspunkt in toFoldResult). */
    val hendelser: MutableList<SoknadHendelse> = mutableListOf(),
) {
    fun toFoldResult(): FoldResult {
        val vedtakBySak = vedtak.groupBy { it.saksReferanse }
        val utbetalingerBySak = utbetalinger.groupBy { it.saksReferanse }
        val dokkravBySak = dokumentasjonkrav.groupBy { it.saksReferanse }
        val vilkarBySak = vilkar.groupBy { it.saksReferanse }

        val kjenteReferanser = saker.mapTo(mutableSetOf()) { it.referanse }
        // Dokumentasjonkrav/vilkår on a sak we've never heard of get a synthesized placeholder,
        // matching what VedtakFattet.kt already does for vedtak with a real saksreferanse.
        val syntetiskeReferanser =
            (dokkravBySak.keys + vilkarBySak.keys)
                .filterNotNull()
                .filter { it !in kjenteReferanser }
                .distinct()

        val alleSaker =
            saker.map { it.referanse } +
                syntetiskeReferanser

        val nestedSaker =
            alleSaker.distinct().map { referanse ->
                val eksisterende = saker.firstOrNull { it.referanse == referanse }
                Sak(
                    referanse = referanse,
                    saksStatus = eksisterende?.saksStatus,
                    tittel = eksisterende?.tittel,
                    vedtak = vedtakBySak[referanse].orEmpty().map { it.vedtak },
                    utbetalinger = utbetalingerBySak[referanse].orEmpty().map { it.utbetaling },
                    dokumentasjonkrav = dokkravBySak[referanse].orEmpty().map { it.krav },
                    vilkar = vilkarBySak[referanse].orEmpty().map { it.vilkar },
                )
            }

        return FoldResult(
            soknad =
                Soknad(
                    fiksDigisosId = fiksDigisosId,
                    navEksternRefId = navEksternRefId,
                    kommunenummer = kommunenummer,
                    fagsystem = fagsystem,
                    erPapirsoknad = erPapirsoknad,
                    tidspunktSendt = tidspunktSendt,
                    sistEndret = sistEndret,
                    status = status,
                    mottaker = mottaker,
                    navKontorHistorikk = navKontorHistorikk.toList(),
                    saker = nestedSaker,
                    vedtakUtenSak = vedtakBySak[null].orEmpty().map { it.vedtak },
                    utbetalingerUtenSak =
                        utbetalinger
                            .filter { it.saksReferanse == null || it.saksReferanse !in kjenteReferanser + syntetiskeReferanser }
                            .map { it.utbetaling },
                    dokumentasjonEtterspurt = dokumentasjonEtterspurt.toList(),
                    forvaltningsbrev = forvaltningsbrev.toList(),
                    forelopigSvar = forelopigSvar,
                    originalSoknad = originalSoknad,
                ),
            hendelser = hendelser.sortedBy { it.tidspunkt },
        )
    }

    fun upsertSak(
        referanse: String,
        saksStatus: SaksStatus?,
        tittel: String?,
    ): Sak {
        val existing = saker.indexOfFirst { it.referanse == referanse }
        val sak =
            Sak(
                referanse = referanse,
                saksStatus = saksStatus,
                tittel = tittel,
                vedtak = emptyList(),
                utbetalinger = emptyList(),
                dokumentasjonkrav = emptyList(),
                vilkar = emptyList(),
            )
        if (existing >= 0) saker[existing] = sak else saker.add(sak)
        return sak
    }

    fun getSak(referanse: String?): Sak? = saker.firstOrNull { it.referanse == referanse }

    fun upsertUtbetaling(utbetaling: FlatUtbetaling) {
        utbetalinger.removeAll { it.utbetaling.referanse == utbetaling.utbetaling.referanse }
        utbetalinger.add(utbetaling)
    }

    fun getUtbetaling(referanse: String): Utbetaling? = utbetalinger.firstOrNull { it.utbetaling.referanse == referanse }?.utbetaling

    fun upsertDokumentasjonkrav(krav: FlatDokumentasjonkrav) {
        dokumentasjonkrav.removeAll { it.krav.referanse == krav.krav.referanse }
        dokumentasjonkrav.add(krav)
    }

    fun upsertVilkar(vilkar: FlatVilkar) {
        this.vilkar.removeAll { it.vilkar.referanse == vilkar.vilkar.referanse }
        this.vilkar.add(vilkar)
    }

    fun harDokumentasjonEtterspurt(): Boolean =
        dokumentasjonEtterspurt.any { it.kilde == DokumentasjonEtterspurt.Kilde.DOKUMENTASJON_ETTERSPURT }

    fun clearDokumentasjonEtterspurt() {
        dokumentasjonEtterspurt.clear()
    }
}

/** [Vedtak] paired with its (possibly blank/absent) saksreferanse, as seen during the fold. */
internal data class FlatVedtak(
    val vedtak: Vedtak,
    val saksReferanse: String?,
)

/** [Utbetaling] paired with its (possibly blank/unresolved) saksreferanse, as seen during the fold. */
internal data class FlatUtbetaling(
    val utbetaling: Utbetaling,
    val saksReferanse: String?,
)

/** [Dokumentasjonkrav] paired with its saksreferanse, as seen during the fold. */
internal data class FlatDokumentasjonkrav(
    val krav: Dokumentasjonkrav,
    val saksReferanse: String?,
)

/** [Vilkar] paired with its saksreferanse, as seen during the fold. */
internal data class FlatVilkar(
    val vilkar: Vilkar,
    val saksReferanse: String?,
)
