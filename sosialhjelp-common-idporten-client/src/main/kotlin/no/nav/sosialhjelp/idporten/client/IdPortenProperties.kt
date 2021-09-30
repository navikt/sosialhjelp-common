package no.nav.sosialhjelp.idporten.client

data class IdPortenProperties(
    val tokenUrl: String,
    val clientId: String,
    val scope: String,
    val configUrl: String,
    val truststoreFilename: String,
    val virksomhetSertifikatPath: String
)
