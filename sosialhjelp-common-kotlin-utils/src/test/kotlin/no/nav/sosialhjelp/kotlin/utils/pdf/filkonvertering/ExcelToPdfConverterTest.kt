package no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering

import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.excel.ExcelFileHandler
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.excel.ExcelToPdfConverter
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.exception.ExcelKonverteringException
import no.nav.sosialhjelp.kotlin.utils.pdf.util.ExampleFileRepository.EXCEL_KONTOUTSKRIFT
import no.nav.sosialhjelp.kotlin.utils.pdf.util.ExampleFileRepository.EXCEL_KONTOUTSKRIFT_BRED
import no.nav.sosialhjelp.kotlin.utils.pdf.util.ExampleFileRepository.EXCEL_KONTOUTSKRIFT_LANG
import no.nav.sosialhjelp.kotlin.utils.pdf.util.ExampleFileRepository.PROBLEM_EXCEL
import org.apache.commons.io.FileUtils
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.io.File

class ExcelToPdfConverterTest {
    // For å kunne se på output ved utvikling/testing
    private val lagPdfFilHvisTrue = false

    @Test
    fun `Test konverter excel`() {
        val pdfBytes = ExcelToPdfConverter.konverterTilPdf(EXCEL_KONTOUTSKRIFT.readBytes())
        Loader.loadPDF(pdfBytes).use {
            assertThat(it.pages.count).isEqualTo(1)
        }
        lagPdfFilHvis(pdfBytes)
    }

    @Test
    fun `Test langt excelark genererer flere sider`() {
        val pdfBytes = ExcelToPdfConverter.konverterTilPdf(EXCEL_KONTOUTSKRIFT_LANG.readBytes())
        Loader.loadPDF(pdfBytes).use {
            assertThat(it.pages.count).isEqualTo(2)
        }
        lagPdfFilHvis(pdfBytes)
    }

    @Test
    fun `Test for bredt excelark kaster exception`() {
        assertThatThrownBy { ExcelToPdfConverter.konverterTilPdf(EXCEL_KONTOUTSKRIFT_BRED.readBytes()) }
            .isInstanceOf(ExcelKonverteringException::class.java)
            .hasCauseInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `Finner alt innhold fra cellene i excel-arket i pdf-dokumentet`() {
        val workbookWrapper = ExcelFileHandler.hentDataFraSource(EXCEL_KONTOUTSKRIFT.readBytes())
        val allCellContent = workbookWrapper.sheets
            .flatMap { it.rows }
            .flatMap { it.cells }
            .map { it.data }

        val pdfBytes = ExcelToPdfConverter.konverterTilPdf(EXCEL_KONTOUTSKRIFT.readBytes())

        Loader.loadPDF(pdfBytes).use {
            val textFromDocument = PDFTextStripper().getText(it)
            allCellContent.forEach { cellContent ->
                assertThat(cutTextToCompare(textFromDocument, cellContent)).isTrue()
            }
        }
        lagPdfFilHvis(pdfBytes)
    }

    @Test
    fun `Konvertere excel-fil med sheet uten rader håndteres`() {
        ExcelToPdfConverter.konverterTilPdf(PROBLEM_EXCEL.readBytes())
    }

    fun lagPdfFilHvis(byteArray: ByteArray) {
        if (lagPdfFilHvisTrue) {
            val resultFile = File("konvertert_excel.pdf")
            FileUtils.writeByteArrayToFile(resultFile, byteArray)
        }
    }
    // tekst kan være croppet i pdf pga celle-bredde.
    private fun cutTextToCompare(allText: String, lineToCompare: String): Boolean {
        if (lineToCompare.isBlank()) return true
        var dynamicLine = lineToCompare

        while (dynamicLine.length > 1) {
            if (allText.contains(dynamicLine)) return true
            dynamicLine = dynamicLine.substring(0, dynamicLine.length - 1)
        }
        return false
    }
}
