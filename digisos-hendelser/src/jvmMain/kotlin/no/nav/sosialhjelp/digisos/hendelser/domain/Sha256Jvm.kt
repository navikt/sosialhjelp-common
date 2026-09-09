package no.nav.sosialhjelp.digisos.hendelser.domain

import java.security.MessageDigest

actual fun sha256(input: String): String =
    MessageDigest
        .getInstance("SHA-256")
        .digest(input.toByteArray())
        .fold("") { acc, byte -> acc + "%02x".format(byte) }
