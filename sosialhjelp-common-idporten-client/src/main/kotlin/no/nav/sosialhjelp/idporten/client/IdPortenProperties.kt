package no.nav.sosialhjelp.idporten.client

data class IdPortenProperties(
    val tokenUrl: String,
    val clientId: String,
    val scope: String,
    val configUrl: String,
    val truststoreType: String,
    val truststoreFilepath: String,
    val virksomhetSertifikatPath: String
)