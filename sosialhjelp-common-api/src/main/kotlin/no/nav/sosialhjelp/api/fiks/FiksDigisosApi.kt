package no.nav.sosialhjelp.api.fiks

import java.io.Serializable

data class DigisosSak(
    val fiksDigisosId: String,
    val sokerFnr: String,
    val fiksOrgId: String,
    val kommunenummer: String,
    val sistEndret: Long,
    val originalSoknadNAV: OriginalSoknadNAV?,
    val ettersendtInfoNAV: EttersendtInfoNAV?,
    val digisosSoker: DigisosSoker?,
    val tilleggsinformasjon: Tilleggsinformasjon?
) : Serializable

data class OriginalSoknadNAV(
    val navEksternRefId: String,
    val metadata: String,
    val vedleggMetadata: String,
    val soknadDokument: DokumentInfo,
    val vedlegg: List<DokumentInfo>,
    val timestampSendt: Long
) : Serializable

data class DokumentInfo(
    val filnavn: String,
    val dokumentlagerDokumentId: String,
    val storrelse: Long
) : Serializable

data class EttersendtInfoNAV(
    val ettersendelser: List<Ettersendelse>
) : Serializable

data class Ettersendelse(
    val navEksternRefId: String,
    val vedleggMetadata: String,
    val vedlegg: List<DokumentInfo>,
    val timestampSendt: Long
) : Serializable

data class DigisosSoker(
    val metadata: String,
    val dokumenter: List<DokumentInfo>,
    val timestampSistOppdatert: Long
) : Serializable

data class Tilleggsinformasjon(
    val enhetsnummer: String?
) : Serializable

data class KommuneInfo(
    val kommunenummer: String,
    val kanMottaSoknader: Boolean,
    val kanOppdatereStatus: Boolean,
    val harMidlertidigDeaktivertMottak: Boolean,
    val harMidlertidigDeaktivertOppdateringer: Boolean,
    val kontaktpersoner: Kontaktpersoner?,
    val harNksTilgang: Boolean,
    val behandlingsansvarlig: String?
) : Serializable

data class Kontaktpersoner(
    val fagansvarligEpost: List<String>,
    val tekniskAnsvarligEpost: List<String>
) : Serializable

data class ErrorMessage(
    val error: String?,
    val errorCode: Any?,
    val errorId: String?,
    val errorJson: Any?,
    val message: String?,
    val originalPath: String?,
    val path: String?,
    val status: Int?,
    val timestamp: Long?
) : Serializable
