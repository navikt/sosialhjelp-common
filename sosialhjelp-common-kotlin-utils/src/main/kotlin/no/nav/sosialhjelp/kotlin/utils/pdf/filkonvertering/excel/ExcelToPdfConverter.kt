package no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.excel

import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.FilTilPdfConverter
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.WritePdfPageOptions
import org.apache.pdfbox.pdmodel.PDDocument
import java.io.ByteArrayOutputStream

object ExcelToPdfConverter : FilTilPdfConverter {
    override fun konverterTilPdf(source: ByteArray) = konverterTilPdfWithOptions(source, WritePdfPageOptions())

    fun konverterTilPdfWithOptions(source: ByteArray, options: WritePdfPageOptions): ByteArray {
        val doc = PDDocument()
        val workbookWrapper = ExcelFileHandler.hentDataFraSource(source)

        workbookWrapper.sheets.forEach { sheetWrapper ->
            SheetToPageHandler(sheetWrapper, doc, options).skrivSheetTilDokument()
        }

        return ByteArrayOutputStream().run {
            doc.save(this)
            doc.close()
            toByteArray()
        }
    }
}
