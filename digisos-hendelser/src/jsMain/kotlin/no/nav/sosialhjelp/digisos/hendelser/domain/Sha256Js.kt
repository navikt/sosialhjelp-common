package no.nav.sosialhjelp.digisos.hendelser.domain

actual fun sha256(input: String): String {
    // Node.js crypto module — available in all server-side JS runtimes
    @Suppress("UnsafeCastFromDynamic")
    val crypto = js("require('crypto')")
    @Suppress("UnsafeCastFromDynamic")
    return crypto.createHash("sha256").update(input).digest("hex") as String
}
