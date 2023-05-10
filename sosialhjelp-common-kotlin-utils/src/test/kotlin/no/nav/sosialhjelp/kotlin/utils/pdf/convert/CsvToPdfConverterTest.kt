package no.nav.sosialhjelp.kotlin.utils.pdf.convert

import no.nav.sosialhjelp.kotlin.utils.pdf.convert.csv.CsvToPdfConverter.konverterTilPdf
import no.nav.sosialhjelp.kotlin.utils.pdf.convert.csv.CsvToPdfConverter.konverterTilPdfWithOptions
import no.nav.sosialhjelp.kotlin.utils.pdf.util.ExampleFileRepository.getCsvExample
import no.nav.sosialhjelp.kotlin.utils.pdf.util.ExampleFileRepository.getCsvExampleLong
import no.nav.sosialhjelp.kotlin.utils.pdf.util.ExampleFileRepository.getCsvExampleWide
import org.apache.commons.csv.CSVFormat
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileReader

class CsvToPdfConverterTest {
    @Test
    fun `Test konvertere csv til pdf og finne igjen all tekst`() {

        val resultFile = File("resultFile.pdf")
        konverterTilPdf(getCsvExample(), resultFile)

        val document = PDDocument.load(resultFile)
        val radTekstListePdf = PDFTextStripper().getText(document).split("\r\n")
        val radTekstListeCsv = parseCsvFile(getCsvExample())

        radTekstListePdf.forEachIndexed { index, tekstForRadPdf ->
            if (tekstForRadPdf.isNotBlank()) {
                radTekstListeCsv[index].forEach { tekstForRadCsv ->
                    assertThat(tekstForRadPdf).contains(tekstForRadCsv)
                }
            }
        }
        document.also {
            assertThat(it.pages.count).isEqualTo(1)
            it.close()
        }
        resultFile.delete()
    }

    @Test
    fun `Test konvertere csv til pdf med kolonnetilpasning`() {
        // strippe tekst fra dette dokumentet gjenspeiler ikke "kolonnetilpasningen"
        // er derfor vanskelig å asserte at dette var vellykket
        val resultFile = File("resultFile.pdf")
        konverterTilPdfWithOptions(getCsvExample(), resultFile, PdfPageOptions(tilpassKolonner = true))
        PDDocument.load(resultFile).also {
            assertThat(it.pages.count).isEqualTo(1)
            it.close()
        }
        resultFile.delete()
    }

    @Test
    fun `Test konvertere csv med for bred tekst`() {
        assertThatThrownBy { konverterTilPdf(getCsvExampleWide(), File("resultFile.pdf")) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining(ExceptionMsg.csvRowTextTooWide)
    }

    @Test
    fun `Test konvertere csv med kolonnetilpasning som blir for bred`() {
        assertThatThrownBy {
            konverterTilPdfWithOptions(
                getCsvExampleWide(),
                File("resultFile.pdf"),
                PdfPageOptions(tilpassKolonner = true)
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining(ExceptionMsg.csvColumnTooWide)
    }

    @Test
    fun `Test konvertere lang csv blir 2 sider`() {
        val resultFile = File("resultFile.pdf")
        konverterTilPdf(getCsvExampleLong(), resultFile)
        PDDocument.load(resultFile).also {
            assertThat(it.pages.count).isEqualTo(2)
            it.close()
        }
        resultFile.delete()
    }

    private fun parseCsvFile(file: File): List<List<String>> {
        val csvFormat = CSVFormat.Builder
            .create()
            .setDelimiter(";")
            .build()

        return csvFormat.parse(FileReader(file))
            .map { rad ->
                rad.map { tekstForKolonne -> tekstForKolonne }.toList()
            }.toList()
    }
}
