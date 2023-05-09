package no.nav.sosialhjelp.kotlin.utils.pdf.convert.excel

import no.nav.sosialhjelp.kotlin.utils.pdf.convert.FilTilPdfConverter
import no.nav.sosialhjelp.kotlin.utils.pdf.convert.PdfPageOptions
import org.apache.pdfbox.pdmodel.PDDocument
import java.io.File

object ExcelToPdfConverter: FilTilPdfConverter {
    override fun konverterTilPdf(source: File, destination: File): PDDocument = konverterTilPdfWithOptions(source, destination, PdfPageOptions())

    fun konverterTilPdfWithOptions(source: File, destination: File, options: PdfPageOptions) = PDDocument().apply {

        val workbookWrapper = ExcelFileHandler.hentDataFraSource(source)

        workbookWrapper.sheets.forEach { sheetWrapper ->
            SheetToPageHandler(sheetWrapper, this, options).skrivSheetTilDokument()
        }

        save(destination)
        close()
    }
}
