package no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.csv

import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.ExceptionMsg
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.KolonneInfo
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.PdfPageSpec
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.WritePdfPageOptions
import no.nav.sosialhjelp.kotlin.utils.pdf.util.PdfFontUtil.breddeIPunkter
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType0Font
import java.io.ByteArrayInputStream

class RecordsToPageHandler(
    private val rader: List<List<String>>,
    private val dokument: PDDocument,
    private val options: WritePdfPageOptions
) {
    private var currentPdfPageSpec = PdfPageSpec(currentXLocation = options.lineStartFromEdge)
    private var currentContentStream = PDPageContentStream(dokument, currentPdfPageSpec.page)

    private val pdFont = PDType0Font.load(dokument, ByteArrayInputStream(options.fontByteArray))
    private val kolonneInfo = opprettKolonneInfoFraRader()

    fun skrivRecordsTilDokument() {
        validerBredde()

        dokument.addPage(currentPdfPageSpec.page)

        rader.forEach { rad ->
            if (currentPdfPageSpec.currentYLocation < getRadHoyde()) leggTilSide()
            currentPdfPageSpec.currentYLocation -= getRadHoyde()
            behandleRad(rad)
        }
        currentContentStream.close()
    }

    private fun validerBredde() {
        if (options.tilpassKolonner && (kolonneInfo.breddeAlleKolonner() > currentPdfPageSpec.width)) {
            throw IllegalArgumentException(ExceptionMsg.csvColumnTooWide)
        } else {
            rader.forEach { rad ->
                val radTekst = rad.joinToString(separator = ";")
                if (pdFont.breddeIPunkter(radTekst, options.fontSize) > currentPdfPageSpec.width) {
                    throw IllegalArgumentException(ExceptionMsg.csvRowTextTooWide)
                }
            }
        }
    }

    private fun getRadHoyde() = (pdFont.boundingBox.height / 1000 * options.fontSize) + options.rowMargin

    private fun leggTilSide() {
        currentPdfPageSpec = PdfPageSpec(currentXLocation = options.lineStartFromEdge)
        dokument.addPage(currentPdfPageSpec.page)

        currentContentStream.close()
        currentContentStream = PDPageContentStream(dokument, currentPdfPageSpec.page)
    }

    private fun behandleRad(rad: List<String>) {
        if (!options.tilpassKolonner) { skrivTekstTilSide(rad.joinToString(separator = ";")) } else {
            rad.forEachIndexed { index, tekst ->
                skrivTekstTilSide(tekst)
                currentPdfPageSpec.currentXLocation += kolonneInfo.kolonneBredde(index) + options.columnMargin
            }
            currentPdfPageSpec.currentXLocation = options.lineStartFromEdge
        }
    }

    private fun skrivTekstTilSide(tekst: String) {
        with(currentContentStream) {
            beginText()
            newLineAtOffset(currentPdfPageSpec.currentXLocation, currentPdfPageSpec.currentYLocation)
            setFont(pdFont, 11f)
            showText(tekst)
            endText()
        }
    }

    private fun opprettKolonneInfoFraRader(): KolonneInfo {
        val tempMap: MutableMap<Int, Float> = HashMap()

        rader.forEach { rad ->
            rad.forEachIndexed { kolonneIndex, tekst ->

                val kolonneBredde = tempMap[kolonneIndex]
                pdFont.breddeIPunkter(tekst, options.fontSize).let {
                    if (kolonneBredde == null || it > kolonneBredde) { tempMap[kolonneIndex] = it }
                }
            }
        }
        return KolonneInfo(tempMap)
    }
}
