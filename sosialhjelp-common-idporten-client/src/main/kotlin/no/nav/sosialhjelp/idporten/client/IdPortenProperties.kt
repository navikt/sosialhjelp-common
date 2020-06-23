package no.nav.sosialhjelp.idporten.client

data class IdPortenProperties(
    val idPortenTokenUrl: String,
    val idPortenClientId: String,
    val idPortenScope: String,
    val idPortenConfigUrl: String,
    val virksomhetSertifikatPath: String
)