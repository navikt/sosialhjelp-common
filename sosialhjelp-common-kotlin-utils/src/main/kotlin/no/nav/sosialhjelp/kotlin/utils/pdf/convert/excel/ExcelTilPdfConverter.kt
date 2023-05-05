package no.nav.sosialhjelp.kotlin.utils.pdf.convert.excel

import no.nav.sosialhjelp.kotlin.utils.pdf.convert.FilTilPdfConverter
import no.nav.sosialhjelp.kotlin.utils.pdf.util.FontResolver
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.poi.xssf.usermodel.XSSFFont
import java.io.File

object ExcelTilPdfConverter: FilTilPdfConverter {
    override fun konverterTilPdf(source: File, destination: File): PDDocument {

        val dokument = PDDocument()

        val workbookWrapper = ExcelFileHandler.hentDataFraSource(source)
        val firstFont = workbookWrapper.workbook.getFontAt(0)
        val (font, fontSize) = finnFont(firstFont, dokument)

        workbookWrapper.sheets.forEach {
            val sheetToPageAdapter = SheetToPageAdapter(it, dokument, font, fontSize)
            sheetToPageAdapter.skrivSheetTilDokument()
        }

        return dokument.apply {
            save(destination)
            close()
        }
    }
    private fun finnFont(font: XSSFFont, document: PDDocument): Pair<PDFont, Short> {
        return FontResolver.getFontFile(font.fontName)?.let {
            Pair(PDType0Font.load(document, it), font.fontHeightInPoints)
        }
            ?: Pair(PDType1Font.TIMES_ROMAN, font.fontHeightInPoints)
    }
}
