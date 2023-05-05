package no.nav.sosialhjelp.kotlin.utils.pdf.convert.excel

import org.apache.poi.util.Units
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook

data class WorkbookWrapper(
    val sheets: List<SheetWrapper>,
    val workbook: XSSFWorkbook
)

data class SheetWrapper(
    val rows: List<RowWrapper>,
    val sheet: XSSFSheet
)

data class RowWrapper (
    val cells: List<CellWrapper>,
    val row: XSSFRow
)
data class CellWrapper (
    val data: String,
    val cell: XSSFCell
)

internal class KolonneInfo (sheetWrapper: SheetWrapper ) {

    private val breddeMap: Map<Int, Int> = sheetWrapper.rows[0].cells.associate {
        val indeks = it.cell.columnIndex
        indeks to sheetWrapper.sheet.getColumnWidth(indeks)
    }

    fun breddeIPunkter(indeks: Int): Float {
        val breddeIUnits = breddeMap.getOrElse(indeks) { throw IllegalStateException("Kolonne $indeks finnes ikke.") }
        val breddeIPiksler = breddeIUnits.toFloat() / 256 * Units.DEFAULT_CHARACTER_WIDTH.toDouble()
        return Units.pixelToPoints(breddeIPiksler).toFloat()
    }

    fun breddeAlleKolonner(): Float {
        return breddeMap.keys
            .map { breddeIPunkter(it) }
            .sum()
    }
}
