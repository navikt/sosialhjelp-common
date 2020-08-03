package no.nav.sosialhjelp.maskinporten.client

import java.io.File
import java.io.FileNotFoundException

fun getenv(key: String, default: String): String {
    return try {
        System.getenv(key)
    } catch (e: Exception) {
        default
    }
}

fun String.readFile(): String? =
        try {
            File(this).readText(Charsets.UTF_8)
        } catch (err: FileNotFoundException) {
            null
        }
