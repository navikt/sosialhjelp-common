package no.nav.sosialhjelp.idporten.client

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "no.nav.sosialhjelp.idporten")
data class IdPortenProperties(
    val tokenUrl: String,
    val clientId: String,
    val scope: String,
    val configUrl: String,
    val truststoreType: String,
    val truststoreFilepath: String
)