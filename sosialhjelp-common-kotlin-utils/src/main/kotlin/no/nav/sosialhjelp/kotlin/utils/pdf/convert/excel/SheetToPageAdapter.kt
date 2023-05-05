package no.nav.sosialhjelp.kotlin.utils.pdf.convert.excel

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.poi.ss.usermodel.CellType

internal class SheetToPageAdapter (
    private val sheetWrapper: SheetWrapper,
    private val dokument: PDDocument,
    private val font: PDFont,
    private val fontSize: Short
) {
    companion object PageConstants {
        private const val START_X = 1f
        private const val MARGIN = 2f
    }

    private val kolonneInfo = KolonneInfo(sheetWrapper)
    private var currentPageSpec = PageSpecs()
    private var currentContentStream = PDPageContentStream(dokument, currentPageSpec.page)

    fun skrivSheetTilDokument() {
        if (kolonneInfo.breddeAlleKolonner() > currentPageSpec.width)
            throw IllegalArgumentException("Excel-ark er for bredt for siden")

        dokument.addPage(currentPageSpec.page)
        sheetWrapper.rows.forEach {
            if (currentPageSpec.initY < it.row.heightInPoints) { currentPageSpec = leggTilSide(it.row.heightInPoints) }
            behandleRad(it)
        }
        currentContentStream.close()
    }

    private fun leggTilSide(rowHeight: Float) = PageSpecs().apply {
        initY -= rowHeight
        dokument.addPage(page)

        currentContentStream.close()
        currentContentStream = PDPageContentStream(dokument, page)
    }

    private fun behandleRad(rowWrapper: RowWrapper) {
        with (currentPageSpec) {
            initY -= rowWrapper.row.heightInPoints
            rowWrapper.cells.forEach { behandleCelle(it) }
            initX = START_X
        }
    }

    private fun behandleCelle(cellWrapper: CellWrapper) {
        val startLinjeX = if (cellWrapper.cell.cellType != CellType.NUMERIC) MARGIN
        else hoyreJusterNumerisk(cellWrapper, font, fontSize)
        val data = evaluerTekstLengde(cellWrapper, font, fontSize)

        with (currentContentStream) {
            beginText()
            newLineAtOffset(currentPageSpec.initX + startLinjeX, currentPageSpec.initY)
            setFont(font, fontSize.toFloat())
            showText(data)
            endText()
        }
        currentPageSpec.initX += kolonneInfo.breddeIPunkter(cellWrapper.cell.columnIndex)
    }
    private fun hoyreJusterNumerisk(cellWrapper: CellWrapper, font: PDFont, fontSize: Short): Float {
        val stringWidth = font.breddeIPunkter(cellWrapper.data, fontSize)
        val columnWidth = kolonneInfo.breddeIPunkter(cellWrapper.cell.columnIndex)

        if (stringWidth < (columnWidth - MARGIN)) return columnWidth - stringWidth
        return MARGIN
    }
    private fun evaluerTekstLengde(cellWrapper: CellWrapper, font: PDFont, fontSize: Short): String {
        val stringWidth = font.breddeIPunkter(cellWrapper.data, fontSize)
        val columnWidth = kolonneInfo.breddeIPunkter(cellWrapper.cell.columnIndex) - MARGIN

        return if (columnWidth < stringWidth) tilpassBredde(cellWrapper.data, columnWidth/stringWidth)
        else cellWrapper.data
    }

    private fun tilpassBredde(data: String, forhold: Float): String =
        (data.length * forhold).toInt().let { data.substring(0, it) }

    private fun PDFont.breddeIPunkter(data: String, fontsize: Short): Float = getStringWidth(data) / 1000 * fontsize

    private data class PageSpecs(
        val page: PDPage = PDPage(PDRectangle.A4),
        val width: Float = page.trimBox.width,
        var initX: Float = START_X,
        var initY: Float = page.trimBox.height
    )
}
