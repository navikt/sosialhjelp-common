package no.nav.sosialhjelp.digisos.hendelser.domain

/** Platform-specific SHA-256 hex digest of [input]. */
expect fun sha256(input: String): String
