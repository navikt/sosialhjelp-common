package no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering

import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.exception.WordKonverteringException
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.word.WordToPdfConverter
import no.nav.sosialhjelp.kotlin.utils.pdf.util.ExampleFileRepository.PROBLEM_WORD
import no.nav.sosialhjelp.kotlin.utils.pdf.util.ExampleFileRepository.WORD_FILE
import org.apache.commons.io.FileUtils
import org.apache.pdfbox.pdmodel.PDDocument
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.io.File

class WordToPdfConverterTest {

    // For å kunne se på output ved utvikling/testing
    private val lagPdfFilHvisTrue = false

    @Test
    fun `Test konverter word til pdf`() {
        val pdfBytes = WordToPdfConverter.konverterTilPdf(WORD_FILE.readBytes())

        PDDocument.load(pdfBytes).use {
            assertThat(it.pages.count).isEqualTo(3)
        }
        lagPdfFilHvis(pdfBytes)
    }

    @Test
    fun `Word-dokument med problemer`() {
        assertThatThrownBy {
            WordToPdfConverter.konverterTilPdf(PROBLEM_WORD.readBytes())
        }.isInstanceOf(WordKonverteringException::class.java)
    }

    fun lagPdfFilHvis(byteArray: ByteArray) {
        if (lagPdfFilHvisTrue) {
            val resultFile = File("konvertert_word.pdf")
            FileUtils.writeByteArrayToFile(resultFile, byteArray)
        }
    }
}
