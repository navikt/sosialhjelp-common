package no.nav.sosialhjelp.digisos.hendelser.event

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import no.nav.sosialhjelp.filformat.digisos.soker.Avsender
import no.nav.sosialhjelp.filformat.digisos.soker.DigisosSoker
import no.nav.sosialhjelp.filformat.digisos.soker.DokumentasjonEtterspurt
import no.nav.sosialhjelp.filformat.digisos.soker.Dokumentasjonkrav
import no.nav.sosialhjelp.filformat.digisos.soker.DokumentlagerFilreferanse
import no.nav.sosialhjelp.filformat.digisos.soker.ForelopigSvar
import no.nav.sosialhjelp.filformat.digisos.soker.Forvaltningsbrev
import no.nav.sosialhjelp.filformat.digisos.soker.Hendelse
import no.nav.sosialhjelp.filformat.digisos.soker.SaksStatus
import no.nav.sosialhjelp.filformat.digisos.soker.SoknadsStatus
import no.nav.sosialhjelp.filformat.digisos.soker.SvarUtFilreferanse
import no.nav.sosialhjelp.filformat.digisos.soker.TildeltNavKontor
import no.nav.sosialhjelp.filformat.digisos.soker.Utbetaling
import no.nav.sosialhjelp.filformat.digisos.soker.VedtakFattet
import no.nav.sosialhjelp.filformat.digisos.soker.Vilkar
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

// ---------------------------------------------------------------------------
// Shared test constants
// ---------------------------------------------------------------------------

const val DOKUMENTLAGERID_1 = "dok-1"
const val DOKUMENTLAGERID_2 = "dok-2"
const val SVARUTID = "svut-42"
const val SVARUT_NR = 42

const val NAVKONTOR = "1337"
const val NAVKONTOR2 = "2244"

const val TITTEL_1 = "tittelen din"
const val TITTEL_2 = "tittel2"

const val REFERANSE_1 = "sak1"
const val REFERANSE_2 = "sak2"

const val UTBETALING_REF_1 = "utbetaling-1"
const val VILKAR_REF_1 = "vilkar-1"
const val DOKUMENTASJONKRAV_REF_1 = "dokrav-1"
const val DOKUMENTTYPE = "faktura"
const val TILLEGGSINFO = "ekstra info"

private val now: Instant = Clock.System.now()

val tidspunkt_1: String = (now - 10.hours).toString()
val tidspunkt_2: String = (now - 9.hours).toString()
val tidspunkt_3: String = (now - 8.hours).toString()
val tidspunkt_4: String = (now - 7.hours).toString()
val tidspunkt_5: String = (now - 6.hours).toString()
val innsendelsesfrist: String = (now + 7.days).toString()

val avsender = Avsender(systemnavn = "testSystem", systemversjon = "1.0")

val DOKUMENTLAGER_1 = DokumentlagerFilreferanse(id = DOKUMENTLAGERID_1)
val DOKUMENTLAGER_2 = DokumentlagerFilreferanse(id = DOKUMENTLAGERID_2)
val SVARUT_1 = SvarUtFilreferanse(id = SVARUTID, nr = SVARUT_NR)

// ---------------------------------------------------------------------------
// Helpers for building hendelser
// ---------------------------------------------------------------------------

fun soknadsStatus(
    status: SoknadsStatus.Status,
    tidspunkt: String = tidspunkt_1,
): SoknadsStatus = SoknadsStatus(hendelsestidspunkt = tidspunkt, status = status)

fun tildeltNavKontor(
    navKontor: String = NAVKONTOR,
    tidspunkt: String = tidspunkt_1,
): TildeltNavKontor = TildeltNavKontor(hendelsestidspunkt = tidspunkt, navKontor = navKontor)

fun saksStatus(
    referanse: String = REFERANSE_1,
    status: SaksStatus.Status = SaksStatus.Status.UNDER_BEHANDLING,
    tittel: String? = TITTEL_1,
    tidspunkt: String = tidspunkt_1,
): SaksStatus =
    SaksStatus(
        hendelsestidspunkt = tidspunkt,
        referanse = referanse,
        status = status,
        tittel = tittel,
    )

fun vedtakFattet(
    saksreferanse: String = REFERANSE_1,
    utfall: VedtakFattet.Utfall = VedtakFattet.Utfall.INNVILGET,
    tidspunkt: String = tidspunkt_1,
): VedtakFattet =
    VedtakFattet(
        hendelsestidspunkt = tidspunkt,
        saksreferanse = saksreferanse,
        vedtaksfil = VedtakFattet.Vedtaksfil(referanse = DOKUMENTLAGER_1),
        utfall = utfall,
    )

fun dokumentasjonEtterspurt(
    dokumenttype: String = DOKUMENTTYPE,
    innsendelsesfrist: String = no.nav.sosialhjelp.digisos.hendelser.event.innsendelsesfrist,
    forvaltningsbrevId: String? = DOKUMENTLAGERID_1,
    tidspunkt: String = tidspunkt_1,
): DokumentasjonEtterspurt =
    DokumentasjonEtterspurt(
        hendelsestidspunkt = tidspunkt,
        dokumenter = listOf(DokumentasjonEtterspurt.Dokument(dokumenttype = dokumenttype, innsendelsesfrist = innsendelsesfrist)),
        forvaltningsbrev = forvaltningsbrevId?.let { Forvaltningsbrev(DokumentlagerFilreferanse(it)) },
    )

fun utbetaling(
    referanse: String = UTBETALING_REF_1,
    status: Utbetaling.Status = Utbetaling.Status.UTBETALT,
    belop: Double = 1000.0,
    saksreferanse: String? = REFERANSE_1,
    annenMottaker: Boolean? = null,
    tidspunkt: String = tidspunkt_1,
): Utbetaling =
    Utbetaling(
        hendelsestidspunkt = tidspunkt,
        utbetalingsreferanse = referanse,
        status = status,
        belop = belop,
        saksreferanse = saksreferanse,
        annenMottaker = annenMottaker,
    )

fun vilkar(
    referanse: String = VILKAR_REF_1,
    status: Vilkar.Status = Vilkar.Status.RELEVANT,
    tittel: String? = TITTEL_1,
    saksreferanse: String? = REFERANSE_1,
    utbetalingsreferanse: List<String>? = null,
    tidspunkt: String = tidspunkt_1,
): Vilkar =
    Vilkar(
        hendelsestidspunkt = tidspunkt,
        vilkarreferanse = referanse,
        status = status,
        tittel = tittel,
        saksreferanse = saksreferanse,
        utbetalingsreferanse = utbetalingsreferanse,
    )

fun dokumentasjonkrav(
    referanse: String = DOKUMENTASJONKRAV_REF_1,
    status: Dokumentasjonkrav.Status = Dokumentasjonkrav.Status.RELEVANT,
    tittel: String? = TITTEL_1,
    frist: String? = innsendelsesfrist,
    saksreferanse: String? = REFERANSE_1,
    tidspunkt: String = tidspunkt_1,
): Dokumentasjonkrav =
    Dokumentasjonkrav(
        hendelsestidspunkt = tidspunkt,
        dokumentasjonkravreferanse = referanse,
        status = status,
        tittel = tittel,
        frist = frist,
        saksreferanse = saksreferanse,
    )

fun forelopigSvar(tidspunkt: String = tidspunkt_1): ForelopigSvar =
    ForelopigSvar(
        hendelsestidspunkt = tidspunkt,
        forvaltningsbrev = Forvaltningsbrev(referanse = DOKUMENTLAGER_1),
    )

fun digisosSoker(vararg hendelser: Hendelse): DigisosSoker =
    DigisosSoker(version = "1.0.0", avsender = avsender, hendelser = hendelser.toList())

/** Build a fresh FoldAccumulator with sensible defaults for testing. */
internal fun testAccumulator(erPapirsoknad: Boolean = false): FoldAccumulator =
    FoldAccumulator(
        fiksDigisosId = "test-id",
        kommunenummer = "1234",
        erPapirsoknad = erPapirsoknad,
    )
