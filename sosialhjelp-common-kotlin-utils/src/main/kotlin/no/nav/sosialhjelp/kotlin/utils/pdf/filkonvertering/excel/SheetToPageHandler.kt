package no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.excel

import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.*
import no.nav.sosialhjelp.kotlin.utils.pdf.util.PdfFontUtil.breddeIPunkter
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.util.Units

internal class SheetToPageHandler (
    private val sheetWrapper: SheetWrapper,
    private val dokument: PDDocument,
    private val options: PdfPageOptions
) {
    private var currentPageSpec = PageSpec(initX = options.start_x)
    private var currentContentStream = PDPageContentStream(dokument, currentPageSpec.page)

    private val pdFont = PDType0Font.load(dokument, options.fontFile)
    private val kolonneInfo: KolonneInfo = kalkulerKolonneInfoFraSheet(sheetWrapper)

    fun skrivSheetTilDokument() {
        if (kolonneInfo.breddeAlleKolonner() > currentPageSpec.width)
            throw IllegalArgumentException(ExceptionMsg.excelSheetToWide)

        dokument.addPage(currentPageSpec.page)
        sheetWrapper.rows.forEach {
            if (currentPageSpec.initY < it.row.heightInPoints) { currentPageSpec = leggTilSide(it.row.heightInPoints) }
            behandleRad(it)
        }
        currentContentStream.close()
    }

    private fun leggTilSide(rowHeight: Float) = PageSpec(initX = options.start_x).apply {
        initY -= rowHeight
        dokument.addPage(page)

        currentContentStream.close()
        currentContentStream = PDPageContentStream(dokument, page)
    }

    private fun behandleRad(rowWrapper: RowWrapper) {
        with (currentPageSpec) {
            initY -= rowWrapper.row.heightInPoints
            rowWrapper.cells.forEach { behandleCelle(it) }
            initX = options.start_x
        }
    }

    private fun behandleCelle(cellWrapper: CellWrapper) {
        val startLinjeX = if (cellWrapper.cell.cellType != CellType.NUMERIC) options.margin_x
        else hoyreJusterNumerisk(cellWrapper)

        val data = evaluerTekstLengde(cellWrapper)

        with (currentContentStream) {
            beginText()
            newLineAtOffset(currentPageSpec.initX + startLinjeX, currentPageSpec.initY)
            setFont(pdFont, options.fontSize.toFloat())
            showText(data)
            endText()
        }
        currentPageSpec.initX += kolonneInfo.kolonneBredde(cellWrapper.cell.columnIndex)
    }
    private fun hoyreJusterNumerisk(cellWrapper: CellWrapper): Float {
        val stringWidth = pdFont.breddeIPunkter(cellWrapper.data, options.fontSize)
        val columnWidth = kolonneInfo.kolonneBredde(cellWrapper.cell.columnIndex)

        if (stringWidth < (columnWidth - options.margin_x)) return columnWidth - stringWidth
        return options.margin_x
    }
    private fun evaluerTekstLengde(cellWrapper: CellWrapper): String {
        val stringWidth = pdFont.breddeIPunkter(cellWrapper.data, options.fontSize)
        val columnWidth = kolonneInfo.kolonneBredde(cellWrapper.cell.columnIndex) - options.margin_x

        return if (columnWidth < stringWidth) tilpassBredde(cellWrapper.data, columnWidth/stringWidth)
        else cellWrapper.data
    }

    private fun tilpassBredde(data: String, forhold: Float): String =
        (data.length * forhold).toInt().let { data.substring(0, it) }
}

private fun kalkulerKolonneInfoFraSheet(sheetWrapper: SheetWrapper): KolonneInfo {
    val map = sheetWrapper.rows[0].cells.associate {
        val kolonneBredde = sheetWrapper.sheet.getColumnWidth(it.cell.columnIndex)
        it.cell.columnIndex to unitsToPoints(kolonneBredde)
    }
    return KolonneInfo(map)
}

private fun unitsToPoints(breddeIUnits: Int): Float {
    val breddeIPiksler = breddeIUnits / 256 * Units.DEFAULT_CHARACTER_WIDTH.toDouble()
    return Units.pixelToPoints(breddeIPiksler).toFloat()
}
