package no.nav.sosialhjelp.kotlin.utils.pdf.convert

import no.nav.sosialhjelp.kotlin.utils.pdf.convert.word.WordToPdfConverter
import no.nav.sosialhjelp.kotlin.utils.pdf.util.ExampleFileRepository
import org.junit.jupiter.api.Test
import java.io.File

class WordToPdfConverterTest {

    @Test
    fun `Test konverter word til pdf`() {

        val source = ExampleFileRepository.getWordExample()
        val dest = File("testWord.pdf")

        val dokument = WordToPdfConverter.konverterTilPdf(source, dest)





        dest.delete()
    }
}
