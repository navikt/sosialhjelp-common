package no.nav.sosialhjelp.kotlin.utils.pdf.convert.excel

import org.apache.commons.collections4.IteratorUtils
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object ExcelFileHandler {
    fun hentDataFraSource(file: File): WorkbookWrapper {

        val fileInputStream = FileInputStream(file)
        val workbook = XSSFWorkbook(fileInputStream)

        val sheets = behandleSheets(workbook.sheetIterator())
        return WorkbookWrapper(sheets, workbook)
    }

    private fun behandleSheets(sheetIterator: Iterator<Sheet>): List<SheetWrapper> {
        return IteratorUtils.toList(sheetIterator)
            .map { SheetWrapper(behandleRows(it.rowIterator()), it as XSSFSheet) }
            .toList()
    }

    private fun behandleRows(rowIterator: Iterator<Row>): List<RowWrapper> {
        return IteratorUtils.toList(rowIterator)
            .map { RowWrapper(behandleCeller(it.cellIterator()), it as XSSFRow) }
            .toList()
    }

    private fun behandleCeller(cellIterator: Iterator<Cell>): List<CellWrapper> {
        return IteratorUtils.toList(cellIterator)
            .map { createCellWrapper(it as XSSFCell) }
            .toList()
    }

    private fun createCellWrapper(cell: XSSFCell): CellWrapper {
        return CellWrapper(
            hentDataFraCelle(cell),
            cell
        )
    }

    private fun hentDataFraCelle(cell: Cell): String {
        return when (cell.cellType) {
            CellType.NUMERIC -> sjekkNumeriskErDato(cell)
            CellType.BLANK -> ""
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> cell.cellFormula
            CellType.STRING -> cell.stringCellValue
            CellType.ERROR -> "error"
            else -> throw IllegalArgumentException("Unknown cell type")
        }
    }

    private fun sjekkNumeriskErDato(cell: Cell): String =
        DateUtil.isADateFormat(cell.cellStyle.dataFormat.toInt(), cell.cellStyle.dataFormatString).let {
            return if (!it) { leggTilDesimal(cell.numericCellValue.toString()) }
            else {
                val toLocalDate = cell.localDateTimeCellValue.toLocalDate()
                toLocalDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
            }
        }

    private fun leggTilDesimal(number: String): String = number.let {
        return if (it.substring(it.indexOf(".")).length == 2) it+"0" else it
    }
}
