package no.nav.sosialhjelp.kotlin.utils.pdf.util

import java.io.File

object ExampleFileRepository {

    private const val WORD_FILE = "sample_word.docx"
    private const val EXCEL_KONTOUTSKRIFT = "sample_kontoutskrift.xlsx"
    private const val EXCEL_KONTOUTSKRIFT_LANG = "sample_kontoutskrift_lang.xlsx"
    private const val EXCEL_KONTOUTSKRIFT_BRED = "sample_kontoutskrift_bred.xlsx"

    fun getKontoUtskrift(): File { return getFile(EXCEL_KONTOUTSKRIFT) }
    fun getKontoUtskriftLang(): File { return getFile(EXCEL_KONTOUTSKRIFT_LANG) }

    fun getKontoUtskriftBred(): File { return getFile(EXCEL_KONTOUTSKRIFT_BRED) }

    private fun getFile(filename: String): File {
        val url = this.javaClass.classLoader.getResource("eksempelfiler/$filename")?.file
        return File(url!!)
    }
}