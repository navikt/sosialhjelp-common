package no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering

import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.word.WordToPdfConverter
import no.nav.sosialhjelp.kotlin.utils.pdf.util.ExampleFileRepository
import org.apache.commons.io.FileUtils
import org.apache.pdfbox.pdmodel.PDDocument
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

class WordToPdfConverterTest {

    // For å kunne se på output ved utvikling/testing
    private val lagPdfFilHvisTrue = false

    @Test
    fun `Test konverter word til pdf`() {
        val source = ExampleFileRepository.getWordExample()
        val pdfBytes = WordToPdfConverter.konverterTilPdf(source.readBytes())

        PDDocument.load(pdfBytes).use {
            Assertions.assertThat(it.pages.count).isEqualTo(3)
        }
        lagPdfFilHvis(pdfBytes)
    }

    fun lagPdfFilHvis(byteArray: ByteArray) {
        if (lagPdfFilHvisTrue) {
            val resultFile = File("konvertert_word.pdf")
            FileUtils.writeByteArrayToFile(resultFile, byteArray)
        }
    }
}
