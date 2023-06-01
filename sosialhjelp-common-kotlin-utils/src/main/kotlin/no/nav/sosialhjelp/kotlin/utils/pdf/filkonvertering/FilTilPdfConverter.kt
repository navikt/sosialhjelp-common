package no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering

interface FilTilPdfConverter {

    /**
     * Returnerer et åpent PDDocument som er lagret til destination.
     * Dokumentet bør lukkes.
     */
    fun konverterTilPdf(source: ByteArray): ByteArray
}
