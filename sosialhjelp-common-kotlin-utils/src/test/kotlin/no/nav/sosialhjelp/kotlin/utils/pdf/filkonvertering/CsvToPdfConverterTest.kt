package no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering

import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.csv.CsvToPdfConverter.konverterTilPdf
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.csv.CsvToPdfConverter.konverterTilPdfWithOptions
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.exception.CsvKonverteringException
import no.nav.sosialhjelp.kotlin.utils.pdf.util.ExampleFileRepository.CSV_FILE
import no.nav.sosialhjelp.kotlin.utils.pdf.util.ExampleFileRepository.CSV_FILE_LONG
import no.nav.sosialhjelp.kotlin.utils.pdf.util.ExampleFileRepository.CSV_FILE_WIDE
import org.apache.commons.csv.CSVFormat
import org.apache.commons.io.FileUtils
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileReader

class CsvToPdfConverterTest {

    // For å kunne se på output ved utvikling/testing
    private val lagPdfFilHvisTrue = false
    @Test
    fun `Test konvertere csv til pdf og finne igjen all tekst`() {
        val pdfBytes = konverterTilPdf(CSV_FILE.readBytes())

        PDDocument.load(pdfBytes).use {

            val radTekstListePdf = PDFTextStripper().getText(it).split("\r\n")
            val radTekstListeCsv = parseCsvFile(CSV_FILE)

            radTekstListePdf.forEachIndexed { index, tekstForRadPdf ->
                if (tekstForRadPdf.isNotBlank()) {
                    radTekstListeCsv[index].forEach { tekstForRadCsv ->
                        assertThat(tekstForRadPdf).contains(tekstForRadCsv)
                    }
                }
            }
            assertThat(it.pages.count).isEqualTo(1)
        }
        lagPdfFilHvis(pdfBytes)
    }

    @Test
    fun `Test konvertere csv til pdf med kolonnetilpasning`() {
        // strippe tekst fra dette dokumentet gjenspeiler ikke "kolonnetilpasningen"
        // er derfor vanskelig å asserte at dette var vellykket visuelt
        val pdfBytes = konverterTilPdfWithOptions(CSV_FILE.readBytes(), WritePdfPageOptions(tilpassKolonner = true))

        PDDocument.load(pdfBytes).use {
            assertThat(it.pages.count).isEqualTo(1)
        }
        lagPdfFilHvis(pdfBytes)
    }

    @Test
    fun `Test konvertere csv med for bred tekst`() {
        assertThatThrownBy { konverterTilPdf(CSV_FILE_WIDE.readBytes()) }
            .isInstanceOf(CsvKonverteringException::class.java)
            .hasCauseInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `Test konvertere csv med kolonnetilpasning som blir for bred`() {
        assertThatThrownBy {
            konverterTilPdfWithOptions(
                CSV_FILE_WIDE.readBytes(),
                WritePdfPageOptions(tilpassKolonner = true)
            )
        }
            .isInstanceOf(CsvKonverteringException::class.java)
            .hasCauseInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `Test konvertere lang csv blir 2 sider`() {
        val pdfBytes = konverterTilPdf(CSV_FILE_LONG.readBytes())

        PDDocument.load(pdfBytes).use {
            assertThat(it.pages.count).isEqualTo(2)
        }
        lagPdfFilHvis(pdfBytes)
    }

    fun lagPdfFilHvis(byteArray: ByteArray) {
        if (lagPdfFilHvisTrue) {
            val resultFile = File("konvertert_csv.pdf")
            FileUtils.writeByteArrayToFile(resultFile, byteArray)
        }
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
