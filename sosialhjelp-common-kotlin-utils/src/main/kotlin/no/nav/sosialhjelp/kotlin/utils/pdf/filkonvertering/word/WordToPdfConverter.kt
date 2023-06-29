package no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.word

import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.FilTilPdfConverter
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.exception.WordKonverteringException
import org.docx4j.Docx4J
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

object WordToPdfConverter : FilTilPdfConverter {
    override fun konverterTilPdf(source: ByteArray): ByteArray {
        try {
            val wordFile = ByteArrayInputStream(source).use {
                WordprocessingMLPackage.load(it)
            }
            return ByteArrayOutputStream().use {
                Docx4J.toPDF(wordFile, it)
                it.flush()
                it.toByteArray()
            }
        } catch (e: Exception) {
            throw WordKonverteringException("Konvertering av Word-fil feilet", e)
        }
    }
}
