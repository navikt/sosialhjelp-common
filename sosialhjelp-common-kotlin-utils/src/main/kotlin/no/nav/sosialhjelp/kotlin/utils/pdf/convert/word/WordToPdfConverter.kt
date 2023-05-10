package no.nav.sosialhjelp.kotlin.utils.pdf.convert.word

import no.nav.sosialhjelp.kotlin.utils.pdf.convert.FilTilPdfConverter
import org.docx4j.Docx4J
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object WordToPdfConverter: FilTilPdfConverter {
    override fun konverterTilPdf(source: File, destination: File) {
        val wordFile = FileInputStream(source).use {
            WordprocessingMLPackage.load(it)
        }
        FileOutputStream(destination).use {
            Docx4J.toPDF(wordFile, it)
            it.flush()
        }
    }
}
