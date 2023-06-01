package no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.excel

import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.ExceptionMsg
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.KolonneInfo
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.PdfPageSpec
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.WritePdfPageOptions
import no.nav.sosialhjelp.kotlin.utils.pdf.util.PdfFontUtil.breddeIPunkter
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.util.Units
import java.io.ByteArrayInputStream

internal class SheetToPageHandler(
    private val sheetWrapper: SheetWrapper,
    private val dokument: PDDocument,
    private val options: WritePdfPageOptions
) {
    private var currentPdfPageSpec = PdfPageSpec(currentXLocation = options.lineStartFromEdge)
    private var currentContentStream = PDPageContentStream(dokument, currentPdfPageSpec.page)

    private val pdFont = PDType0Font.load(dokument, ByteArrayInputStream(options.fontByteArray))
    private val kolonneInfo: KolonneInfo = kalkulerKolonneInfoFraSheet(sheetWrapper)

    fun skrivSheetTilDokument() {
        if (kolonneInfo.breddeAlleKolonner() > currentPdfPageSpec.width)
            throw IllegalArgumentException(ExceptionMsg.excelSheetToWide)

        dokument.addPage(currentPdfPageSpec.page)
        sheetWrapper.rows.forEach {
            if (currentPdfPageSpec.currentYLocation < it.row.heightInPoints) {
                currentPdfPageSpec = leggTilSide(it.row.heightInPoints)
            }
            behandleRad(it)
        }
        currentContentStream.close()
    }

    private fun leggTilSide(rowHeight: Float) = PdfPageSpec(currentXLocation = options.lineStartFromEdge).apply {
        currentYLocation -= rowHeight
        dokument.addPage(page)

        currentContentStream.close()
        currentContentStream = PDPageContentStream(dokument, page)
    }

    private fun behandleRad(rowWrapper: RowWrapper) {
        with(currentPdfPageSpec) {
            currentYLocation -= rowWrapper.row.heightInPoints
            rowWrapper.cells.forEach { behandleCelle(it) }
            currentXLocation = options.lineStartFromEdge
        }
    }

    private fun behandleCelle(cellWrapper: CellWrapper) {
        val startLineX = if (cellWrapper.cell.cellType != CellType.NUMERIC) options.columnMargin
        else hoyreJusterNumerisk(cellWrapper)

        val data = evaluerTekstLengde(cellWrapper)

        with(currentContentStream) {
            beginText()
            newLineAtOffset(currentPdfPageSpec.currentXLocation + startLineX, currentPdfPageSpec.currentYLocation)
            setFont(pdFont, options.fontSize.toFloat())
            showText(data)
            endText()
        }
        currentPdfPageSpec.currentXLocation += kolonneInfo.kolonneBredde(cellWrapper.cell.columnIndex)
    }
    private fun hoyreJusterNumerisk(cellWrapper: CellWrapper): Float {
        val stringWidth = pdFont.breddeIPunkter(cellWrapper.data, options.fontSize)
        val columnWidth = kolonneInfo.kolonneBredde(cellWrapper.cell.columnIndex)

        if (stringWidth < (columnWidth - options.columnMargin)) return columnWidth - stringWidth
        return options.columnMargin
    }
    private fun evaluerTekstLengde(cellWrapper: CellWrapper): String {
        val stringWidth = pdFont.breddeIPunkter(cellWrapper.data, options.fontSize)
        val columnWidth = kolonneInfo.kolonneBredde(cellWrapper.cell.columnIndex) - options.columnMargin

        return if (columnWidth < stringWidth) tilpassBredde(cellWrapper.data, columnWidth / stringWidth)
        else cellWrapper.data
    }

    private fun tilpassBredde(data: String, forhold: Float): String =
        (data.length * forhold).toInt().let { data.substring(0, it) }

    private fun kalkulerKolonneInfoFraSheet(sheetWrapper: SheetWrapper): KolonneInfo {
        val map = sheetWrapper.rows[0].cells.associate {
            val kolonneBredde = sheetWrapper.sheet.getColumnWidth(it.cell.columnIndex)
            it.cell.columnIndex to unitsToPoints(kolonneBredde)
        }
        return KolonneInfo(map)
    }

    private fun unitsToPoints(breddeIUnits: Int): Float {
        val numberOfExcelUnitsInCharacter = 256f
        val breddeIPiksler = breddeIUnits / numberOfExcelUnitsInCharacter * Units.DEFAULT_CHARACTER_WIDTH.toDouble()
        return Units.pixelToPoints(breddeIPiksler).toFloat()
    }
}
