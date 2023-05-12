package no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.csv

import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.ExceptionMsg
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.KolonneInfo
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.PageSpec
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.PdfPageOptions
import no.nav.sosialhjelp.kotlin.utils.pdf.util.PdfFontUtil.breddeIPunkter
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType0Font
import java.io.ByteArrayInputStream

class RecordsToPageHandler(
    private val rader: List<List<String>>,
    private val dokument: PDDocument,
    private val options: PdfPageOptions
) {
    private var currentPageSpec = PageSpec(initX = options.start_x)
    private var currentContentStream = PDPageContentStream(dokument, currentPageSpec.page)

    private val pdFont = PDType0Font.load(dokument, ByteArrayInputStream(options.fontByteArray))
    private val kolonneInfo = opprettKolonneInfoFraRader()

    fun skrivRecordsTilDokument() {
        validerBredde()

        dokument.addPage(currentPageSpec.page)

        rader.forEach { rad ->
            if (currentPageSpec.initY < getRadHoyde()) leggTilSide()
            currentPageSpec.initY -= getRadHoyde()
            behandleRad(rad)
        }
        currentContentStream.close()
    }

    private fun validerBredde() {
        if (options.tilpassKolonner && (kolonneInfo.breddeAlleKolonner() > currentPageSpec.width)) {
            throw IllegalArgumentException(ExceptionMsg.csvColumnTooWide)
        } else {
            rader.forEach { rad ->
                val radTekst = rad.joinToString(separator = ";")
                if (pdFont.breddeIPunkter(radTekst, options.fontSize) > currentPageSpec.width) {
                    throw IllegalArgumentException(ExceptionMsg.csvRowTextTooWide)
                }
            }
        }
    }

    private fun getRadHoyde() = (pdFont.boundingBox.height / 1000 * options.fontSize) + options.margin_y

    private fun leggTilSide() {
        currentPageSpec = PageSpec(initX = options.start_x)
        dokument.addPage(currentPageSpec.page)

        currentContentStream.close()
        currentContentStream = PDPageContentStream(dokument, currentPageSpec.page)
    }

    private fun behandleRad(rad: List<String>) {
        if (!options.tilpassKolonner) { skrivTekstTilSide(rad.joinToString(separator = ";")) } else {
            rad.forEachIndexed { index, tekst ->
                skrivTekstTilSide(tekst)
                currentPageSpec.initX += kolonneInfo.kolonneBredde(index) + options.margin_x
            }
            currentPageSpec.initX = options.start_x
        }
    }

    private fun skrivTekstTilSide(tekst: String) {
        with(currentContentStream) {
            beginText()
            newLineAtOffset(currentPageSpec.initX, currentPageSpec.initY)
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
