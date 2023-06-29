package no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.excel

import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.FilTilPdfConverter
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.WritePdfPageOptions
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.exception.ExcelKonverteringException
import org.apache.pdfbox.pdmodel.PDDocument
import java.io.ByteArrayOutputStream

object ExcelToPdfConverter : FilTilPdfConverter {
    override fun konverterTilPdf(source: ByteArray) = konverterTilPdfWithOptions(source, WritePdfPageOptions())

    fun konverterTilPdfWithOptions(source: ByteArray, options: WritePdfPageOptions): ByteArray {
        try {
            val doc = PDDocument()
            val workbookWrapper = ExcelFileHandler.hentDataFraSource(source)

            workbookWrapper.sheets.forEach { sheetWrapper ->
                if (sheetWrapper.rows.isNotEmpty()) {
                    SheetToPageHandler(sheetWrapper, doc, options).skrivSheetTilDokument()
                }
            }

            return ByteArrayOutputStream().run {
                doc.save(this)
                doc.close()
                toByteArray()
            }
        } catch (e: Exception) {
            throw ExcelKonverteringException("Konvertering av excel-fil feilet", e)
        }
    }
}
