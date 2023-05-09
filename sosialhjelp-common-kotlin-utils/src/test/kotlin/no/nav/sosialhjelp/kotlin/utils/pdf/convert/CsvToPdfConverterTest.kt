package no.nav.sosialhjelp.kotlin.utils.pdf.convert

import no.nav.sosialhjelp.kotlin.utils.pdf.convert.csv.CsvToPdfConverter.konverterTilPdf
import no.nav.sosialhjelp.kotlin.utils.pdf.convert.csv.CsvToPdfConverter.konverterTilPdfWithOptions
import no.nav.sosialhjelp.kotlin.utils.pdf.util.ExampleFileRepository.getCsvExample
import no.nav.sosialhjelp.kotlin.utils.pdf.util.ExampleFileRepository.getCsvExampleWide
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

class CsvToPdfConverterTest {
    @Test
    fun `Test konvertere csv til pdf`() {

        val resultFile = File("resultFile.pdf")
        konverterTilPdf(getCsvExample(), resultFile)
        resultFile.delete()
    }

    @Test
    fun `Test konvertere csv til pdf med kolonnetilpasning`() {

        val resultFile = File("resultFile.pdf")
        konverterTilPdfWithOptions(getCsvExample(), resultFile, PdfPageOptions(tilpassKolonner = true))
        resultFile.delete()
    }

    @Test
    fun `Test konvertere csv med for bred tekst`() {
        Assertions.assertThatThrownBy { konverterTilPdf(getCsvExampleWide(), File("resultFile.pdf")) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining(ExceptionMsg.csvRowTextTooWide)
    }

    @Test
    fun `Test konvertere csv med kolonnetilpasning som blir for bred`() {
        Assertions.assertThatThrownBy {
            konverterTilPdfWithOptions(
                getCsvExampleWide(),
                File("resultFile.pdf"),
                PdfPageOptions(tilpassKolonner = true)
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining(ExceptionMsg.csvColumnTooWide)
    }
}
