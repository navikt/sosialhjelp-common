package no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.excel

import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.FilTilPdfConverter
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.PdfPageOptions
import org.apache.pdfbox.pdmodel.PDDocument
import java.io.File

object ExcelToPdfConverter: FilTilPdfConverter {
    override fun konverterTilPdf(source: File, destination: File) {
        konverterTilPdfWithOptions(source, destination, PdfPageOptions())
    }

    fun konverterTilPdfWithOptions(source: File, destination: File, options: PdfPageOptions) {
        PDDocument().run {
            val workbookWrapper = ExcelFileHandler.hentDataFraSource(source)

            workbookWrapper.sheets.forEach { sheetWrapper ->
                SheetToPageHandler(sheetWrapper, this, options).skrivSheetTilDokument()
            }

            save(destination)
            close()
        }
    }
}
