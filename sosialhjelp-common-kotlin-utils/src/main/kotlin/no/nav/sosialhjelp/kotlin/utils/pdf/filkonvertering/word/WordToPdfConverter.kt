package no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.word

import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.FilTilPdfConverter
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.exception.WordKonverteringException
import org.docx4j.Docx4J
import org.docx4j.fonts.PhysicalFonts
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
                // For å unngå feil med font på Mac slik at tester kan kjøre lokalt
                if (System.getProperty("os.name").equals("Mac OS X", ignoreCase = true)) {
                    PhysicalFonts.setRegex(".*(Courier New|Arial|Times New Roman|Comic Sans|Georgia|Impact|Lucida Console|Lucida Sans Unicode|Palatino Linotype|Tahoma|Trebuchet|Verdana|Symbol|Webdings|Wingdings|MS Sans Serif|MS Serif).*")
                }
                Docx4J.toPDF(wordFile, it)
                it.flush()
                it.toByteArray()
            }
        } catch (e: Exception) {
            throw WordKonverteringException("Konvertering av Word-fil feilet", e)
        }
    }
}
