package no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering

import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.excel.ExcelFileHandler
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.excel.ExcelToPdfConverter
import no.nav.sosialhjelp.kotlin.utils.pdf.util.ExampleFileRepository.getKontoUtskrift
import no.nav.sosialhjelp.kotlin.utils.pdf.util.ExampleFileRepository.getKontoUtskriftBred
import no.nav.sosialhjelp.kotlin.utils.pdf.util.ExampleFileRepository.getKontoUtskriftLang
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.io.File

class ExcelToPdfConverterTest {
    @Test
    fun `Test konverter excel`() {
        val destination = File("testExcel.pdf")

        ExcelToPdfConverter.konverterTilPdf(getKontoUtskrift(), destination)
        val document = PDDocument.load(destination)
        assertThat(document.pages.count).isEqualTo(1)

        destination.delete()
    }

    @Test
    fun `Test langt excelark genererer flere sider`() {
        val destination = File("testExcel.pdf")

        ExcelToPdfConverter.konverterTilPdf(getKontoUtskriftLang(), destination)
        val document = PDDocument.load(destination)
        assertThat(document.pages.count).isEqualTo(2)

        destination.delete()
    }

    @Test
    fun `Test for bredt excelark genererer flere sider`() {
        val destination = File("testExcel.pdf")

        assertThatThrownBy { ExcelToPdfConverter.konverterTilPdf(getKontoUtskriftBred(), destination) }
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
        ExcelToPdfConverter.konverterTilPdf(getKontoUtskrift(), resultFile)

        val document = PDDocument.load(resultFile)
        val textFromDocument = PDFTextStripper().getText(document)

        allCellContent.forEach { cellContent ->
            assertThat(cutTextToCompare(textFromDocument, cellContent)).isTrue()
        }
    }

    // tekst kan være croppet i pdf pga celle-bredde.
    private fun cutTextToCompare(allText: String, lineToCompare: String): Boolean {
        if (lineToCompare.isBlank()) return true
        var dynamicLine = lineToCompare

        while (dynamicLine.length > 1) {
            if (allText.contains(dynamicLine)) return true
            dynamicLine = dynamicLine.substring(0, dynamicLine.length-1)
        }
        return false
    }
}
