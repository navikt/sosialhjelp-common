package no.nav.sosialhjelp.kotlin.utils.pdf.util

import java.io.File

object ExampleFileRepository {

    private const val WORD_FILE = "sample_word.docx"
    private const val EXCEL_KONTOUTSKRIFT = "sample_kontoutskrift.xlsx"
    private const val EXCEL_KONTOUTSKRIFT_LANG = "sample_kontoutskrift_lang.xlsx"
    private const val EXCEL_KONTOUTSKRIFT_BRED = "sample_kontoutskrift_bred.xlsx"
    private const val CSV_FILE = "sample_csv.csv"
    private const val CSV_FILE_WIDE = "sample_csv_wide.csv"
    private const val CSV_FILE_LONG = "sample_csv_long.csv"


    fun getWordExample() = getFile(WORD_FILE)
    fun getKontoUtskrift() = getFile(EXCEL_KONTOUTSKRIFT)
    fun getKontoUtskriftLang() = getFile(EXCEL_KONTOUTSKRIFT_LANG)
    fun getKontoUtskriftBred() = getFile(EXCEL_KONTOUTSKRIFT_BRED)
    fun getCsvExample() = getFile(CSV_FILE)
    fun getCsvExampleWide() = getFile(CSV_FILE_WIDE)
    fun getCsvExampleLong() = getFile(CSV_FILE_LONG)


    private fun getFile(filename: String): File {
        val url = this.javaClass.classLoader.getResource("eksempelfiler/$filename")?.file
        return File(url!!)
    }
}