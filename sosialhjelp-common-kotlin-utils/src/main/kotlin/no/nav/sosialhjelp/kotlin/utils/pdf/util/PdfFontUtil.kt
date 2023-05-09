package no.nav.sosialhjelp.kotlin.utils.pdf.util

import org.apache.pdfbox.pdmodel.font.PDFont
import java.io.File
import java.net.URL

object PdfFontUtil {

    private const val FONTS_PATH = "fonts"
    private const val DEFAULT_FAMILY = "calibri"
    private const val DEFAULT_FONT = "calibri.ttf"

    fun getDefaultFontFile(): File {
        return getResource("$DEFAULT_FAMILY/$DEFAULT_FONT")?.let {
            File(it.toURI())
        } ?: throw IllegalStateException("Default font calibri finnes ikke")
    }

    fun getFontFile(fontname: String): File {
        return getResource("/$fontname/$fontname.ttf")?.let {
            File(it.toURI())
        } ?: getDefaultFontFile()
    }

    private fun getResource(path: String): URL? = javaClass.classLoader.getResource("$FONTS_PATH/$path")

    fun PDFont.breddeIPunkter(data: String, fontsize: Short): Float = getStringWidth(data) / 1000 * fontsize
}
