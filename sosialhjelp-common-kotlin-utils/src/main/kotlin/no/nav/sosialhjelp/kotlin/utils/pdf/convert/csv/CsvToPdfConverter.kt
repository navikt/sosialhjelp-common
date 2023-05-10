package no.nav.sosialhjelp.kotlin.utils.pdf.convert.csv

import no.nav.sosialhjelp.kotlin.utils.pdf.convert.FilTilPdfConverter
import no.nav.sosialhjelp.kotlin.utils.pdf.convert.PdfPageOptions
import org.apache.commons.csv.CSVFormat
import org.apache.pdfbox.pdmodel.PDDocument
import java.io.File
import java.io.FileReader

object CsvToPdfConverter: FilTilPdfConverter {
    override fun konverterTilPdf(source: File, destination: File) {
        konverterTilPdfWithOptions(source, destination, PdfPageOptions())
    }

    fun konverterTilPdfWithOptions(source: File, destination: File, options: PdfPageOptions) {
        PDDocument().run {
            RecordsToPageHandler(streamRecordsToList(source), this, options)
                .skrivRecordsTilDokument()

            save(destination)
            close()
        }
    }

    private fun streamRecordsToList(csvFile: File): List<List<String>> = FileReader(csvFile).let { fileReader ->
        val csvFormat = CSVFormat.Builder
            .create()
            .setDelimiter(";")
            .build()

        csvFormat.parse(fileReader)
            .map { rad ->
                rad.map { tekstForKolonne -> tekstForKolonne }.toList()
            }.toList()
    }
}
