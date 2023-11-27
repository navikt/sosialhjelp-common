package no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering

import no.nav.sosialhjelp.kotlin.utils.pdf.util.PdfFontUtil.getDefaultFontBytes
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.common.PDRectangle

class WritePdfPageOptions(
    var fontByteArray: ByteArray = getDefaultFontBytes(),
    var fontSize: Short = 11,
    val lineStartFromEdge: Float = 1f,
    val columnMargin: Float = 3f,
    val rowMargin: Float = 3f,
    val tilpassKolonner: Boolean = false,
)

data class PdfPageSpec(
    val page: PDPage = PDPage(PDRectangle.A4),
    val width: Float = page.mediaBox.width,
    var currentXLocation: Float,
    var currentYLocation: Float = page.mediaBox.height
)

object ExceptionMsg {
    val csvRowTextTooWide = "Tekst fra rad er bredere enn pdf-siden"
    val csvColumnTooWide = "Kan ikke tilpasse kolonner. Bredere enn pdf-siden"
    val excelSheetToWide = "Excel-ark er for bredt for siden"
}

internal class KolonneInfo(private val breddeMap: Map<Int, Float>) {
    fun kolonneBredde(indeks: Int): Float {
        return breddeMap.getOrElse(indeks) { throw IllegalStateException("Kolonne $indeks finnes ikke.") }
    }

    fun breddeAlleKolonner(): Float {
        return breddeMap.keys
            .map { kolonneBredde(it) }
            .sum()
    }
}
