package no.nav.sosialhjelp.kotlin.utils.pdf.util

import java.io.File
import java.net.URL

object FontResolver {

    private const val FONTS_PATH = "fonts"

    fun getFontFile(fontname: String): File? {
        val fontUrl = supportedFamilys(fontname)?.let { getResource("/$it/$fontname.ttf") }
        return fontUrl?.let { File(it.toURI()) }
    }

    private fun supportedFamilys(family: String): String? = when (family) {
        "calibri" -> "calibri"
        "arial" -> "arial"
        "modus" -> "modus"
        "source_sans_pro" -> "source_sans_pro"
        else -> null
    }

    private fun getResource(path: String): URL? = javaClass.classLoader.getResource("$FONTS_PATH/$path")
}
