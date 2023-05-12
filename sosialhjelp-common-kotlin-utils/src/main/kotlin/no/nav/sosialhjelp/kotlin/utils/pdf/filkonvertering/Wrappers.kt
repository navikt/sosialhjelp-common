package no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering

import no.nav.sosialhjelp.kotlin.utils.pdf.util.PdfFontUtil.getDefaultFontBytes
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class PdfPageOptions(
    var fontByteArray: ByteArray = getDefaultFontBytes(),
    var fontSize: Short = 11,
    val start_x: Float = 1f,
    val margin_x: Float = 3f,
    val margin_y: Float = 3f,
    val tilpassKolonner: Boolean = false,
)

data class WorkbookWrapper(
    val sheets: List<SheetWrapper>,
    val workbook: XSSFWorkbook
)

data class SheetWrapper(
    val rows: List<RowWrapper>,
    val sheet: XSSFSheet
)

data class RowWrapper(
    val cells: List<CellWrapper>,
    val row: XSSFRow
)
data class CellWrapper(
    val data: String,
    val cell: XSSFCell
)

data class PageSpec(
    val page: PDPage = PDPage(PDRectangle.A4),
    val width: Float = page.trimBox.width,
    var initX: Float,
    var initY: Float = page.trimBox.height
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
