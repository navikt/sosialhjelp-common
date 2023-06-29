package no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.csv

import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.FilTilPdfConverter
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.WritePdfPageOptions
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.exception.CsvKonverteringException
import org.apache.commons.csv.CSVFormat
import org.apache.pdfbox.pdmodel.PDDocument
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader

object CsvToPdfConverter : FilTilPdfConverter {
    override fun konverterTilPdf(source: ByteArray) = konverterTilPdfWithOptions(source, WritePdfPageOptions())

    fun konverterTilPdfWithOptions(source: ByteArray, options: WritePdfPageOptions): ByteArray {
        try {
            val doc = PDDocument()
            RecordsToPageHandler(streamRecordsToList(source), doc, options)
                .skrivRecordsTilDokument()

            return ByteArrayOutputStream().use {
                doc.save(it)
                doc.close()
                it.toByteArray()
            }
        } catch (e: Exception) {
            throw CsvKonverteringException("Konvertering av CSV-fil feilet", e)
        }
    }

    private fun streamRecordsToList(csvFile: ByteArray): List<List<String>> {

        val inputStream = ByteArrayInputStream(csvFile)
        val inputStreamReader = InputStreamReader(inputStream)

        val csvFormat = CSVFormat.Builder
            .create()
            .setDelimiter(";")
            .build()

        return csvFormat.parse(inputStreamReader)
            .map { rad ->
                rad.map { tekstForKolonne -> tekstForKolonne }.toList()
            }.toList()
    }
}
