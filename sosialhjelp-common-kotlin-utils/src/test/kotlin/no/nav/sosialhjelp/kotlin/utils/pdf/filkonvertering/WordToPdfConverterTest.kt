package no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering

import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.word.WordToPdfConverter
import no.nav.sosialhjelp.kotlin.utils.pdf.util.ExampleFileRepository
import org.apache.pdfbox.pdmodel.PDDocument
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

class WordToPdfConverterTest {

    @Test
    fun `Test konverter word til pdf`() {

        val source = ExampleFileRepository.getWordExample()
        val dest = File("testWord.pdf")

        WordToPdfConverter.konverterTilPdf(source, dest)

        PDDocument.load(dest).also {
            Assertions.assertThat(it.pages.count).isEqualTo(3)
            it.close()
        }

        dest.delete()
    }
}
