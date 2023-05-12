package no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering

import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.excel.ExcelFileHandler
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.excel.ExcelToPdfConverter
import no.nav.sosialhjelp.kotlin.utils.pdf.util.ExampleFileRepository.getKontoUtskrift
import no.nav.sosialhjelp.kotlin.utils.pdf.util.ExampleFileRepository.getKontoUtskriftBred
import no.nav.sosialhjelp.kotlin.utils.pdf.util.ExampleFileRepository.getKontoUtskriftLang
import org.apache.commons.io.FileUtils
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.io.File

class ExcelToPdfConverterTest {
    // For å kunne se på output ved utvikling/testing
    private val SKRIV_TIL_FIL = false

    @Test
    fun `Test konverter excel`() {
        val pdfBytes = ExcelToPdfConverter.konverterTilPdf(getKontoUtskrift().readBytes())
        PDDocument.load(pdfBytes).use {
            assertThat(it.pages.count).isEqualTo(1)
        }
        lagPdfFilHvis(pdfBytes)
    }

    @Test
    fun `Test langt excelark genererer flere sider`() {
        val pdfBytes = ExcelToPdfConverter.konverterTilPdf(getKontoUtskriftLang().readBytes())
        PDDocument.load(pdfBytes).use {
            assertThat(it.pages.count).isEqualTo(2)
        }
        lagPdfFilHvis(pdfBytes)
    }

    @Test
    fun `Test for bredt excelark kaster exception`() {
        assertThatThrownBy { ExcelToPdfConverter.konverterTilPdf(getKontoUtskriftBred().readBytes()) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("for bredt")
    }

    @Test
    fun `Finner alt innhold fra cellene i excel-arket i pdf-dokumentet`() {
        val workbookWrapper = ExcelFileHandler.hentDataFraSource(getKontoUtskrift().readBytes())
        val allCellContent = workbookWrapper.sheets
            .flatMap { it.rows }
            .flatMap { it.cells }
            .map { it.data }

        val pdfBytes = ExcelToPdfConverter.konverterTilPdf(getKontoUtskrift().readBytes())

        PDDocument.load(pdfBytes).use {
            val textFromDocument = PDFTextStripper().getText(it)
            allCellContent.forEach { cellContent ->
                assertThat(cutTextToCompare(textFromDocument, cellContent)).isTrue()
            }
        }
        lagPdfFilHvis(pdfBytes)
    }

    fun lagPdfFilHvis(byteArray: ByteArray) {
        if (SKRIV_TIL_FIL) {
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
