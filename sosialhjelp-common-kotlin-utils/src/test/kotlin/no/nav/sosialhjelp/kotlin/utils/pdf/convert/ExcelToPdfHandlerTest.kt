package no.nav.sosialhjelp.kotlin.utils.pdf.convert

import no.nav.sosialhjelp.kotlin.utils.pdf.convert.excel.ExcelFileHandler
import no.nav.sosialhjelp.kotlin.utils.pdf.convert.excel.ExcelTilPdfConverter
import no.nav.sosialhjelp.kotlin.utils.pdf.util.ExampleFileRepository.getKontoUtskrift
import no.nav.sosialhjelp.kotlin.utils.pdf.util.ExampleFileRepository.getKontoUtskriftBred
import no.nav.sosialhjelp.kotlin.utils.pdf.util.ExampleFileRepository.getKontoUtskriftLang
import org.apache.pdfbox.text.PDFTextStripper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.io.File

class ExcelToPdfHandlerTest {
    @Test
    fun `Test konverter excel`() {
        val destination = File("testExcel.pdf")

        val document = ExcelTilPdfConverter.konverterTilPdf(getKontoUtskrift(), destination)
        assertThat(document.pages.count).isEqualTo(1)

        destination.delete()
    }

    @Test
    fun `Test langt excelark genererer flere sider`() {
        val destination = File("testExcel.pdf")

        val document = ExcelTilPdfConverter.konverterTilPdf(getKontoUtskriftLang(), destination)
        assertThat(document.pages.count).isEqualTo(2)

        destination.delete()
    }

    @Test
    fun `Test for bredt excelark genererer flere sider`() {
        val destination = File("testExcel.pdf")

        assertThatThrownBy { ExcelTilPdfConverter.konverterTilPdf(getKontoUtskriftBred(), destination) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("for bredt")

        destination.delete()
    }

    @Test
    fun `Finner alt innhold fra cellene i excel-arket i pdf-dokumentet`() {

        val workbookWrapper = ExcelFileHandler.hentDataFraSource(getKontoUtskrift())
        val allCellContent = workbookWrapper.sheets
            .flatMap { it.rows }
            .flatMap { it.cells }
            .map { it.data }

        val resultFile = File("resultFile.pdf")
        val document = ExcelTilPdfConverter.konverterTilPdf(getKontoUtskrift(), resultFile)

        val textFromDocument = PDFTextStripper().getText(document)

        allCellContent.forEach { cellContent ->
            assertThat(cutTextToCompare(textFromDocument, cellContent)).isTrue()
        }
    }

    // tekst kan være croppet i pdf pga celle-bredde.
    private fun cutTextToCompare(allText: String, lineToCompare: String): Boolean {
        if (lineToCompare.isBlank()) return true
        var dynamicLine = lineToCompare

        while (dynamicLine.length > 3) {
            if (allText.contains(dynamicLine)) return true
            dynamicLine = dynamicLine.substring(0, dynamicLine.length-3)
        }
        return false
    }
}
