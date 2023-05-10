package no.nav.sosialhjelp.kotlin.utils.pdf.convert

import java.io.File

interface FilTilPdfConverter {

    /**
     * Returnerer et åpent PDDocument som er lagret til destination.
     * Dokumentet bør lukkes.
     */
    fun konverterTilPdf(source: File, destination: File)
}