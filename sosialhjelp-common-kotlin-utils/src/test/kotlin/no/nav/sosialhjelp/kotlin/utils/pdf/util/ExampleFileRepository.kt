package no.nav.sosialhjelp.kotlin.utils.pdf.util

import java.io.File

object ExampleFileRepository {

    val WORD_FILE = getFile("sample_word.docx")
    val EXCEL_KONTOUTSKRIFT = getFile("sample_kontoutskrift.xlsx")
    val EXCEL_KONTOUTSKRIFT_LANG = getFile("sample_kontoutskrift_lang.xlsx")
    val EXCEL_KONTOUTSKRIFT_BRED = getFile("sample_kontoutskrift_bred.xlsx")
    val CSV_FILE = getFile("sample_csv.csv")
    val CSV_FILE_WIDE = getFile("sample_csv_wide.csv")
    val CSV_FILE_LONG = getFile("sample_csv_long.csv")

    val PROBLEM_WORD = getFile("problem_word.docx")
    val PROBLEM_EXCEL = getFile("problem_excel.xlsx")

    fun getFile(filename: String): File {
        val url = this.javaClass.classLoader.getResource("eksempelfiler/$filename")?.file
        return File(url!!)
    }
}
