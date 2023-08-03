package no.nav.helse

import no.nav.helse.dusseldorf.ktor.core.fromResources
import no.nav.helse.journalforing.converter.Image2PDFConverter
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.File
import java.util.*
import kotlin.test.Test

class Image2PdfConverterTest {

    @Test
    fun `Generert skalerte bilder`() {
        scale(resourceName = "hoyopplost.jpg", name = "hoyopplost", format = "jpg")
        scale(resourceName = "widescreen.jpg", name = "widescreen", format = "jpg")
        scale(resourceName = "legeerklaering_iPhone_XS.jpg", name = "legeerklaering", format = "jpg")
        scale(resourceName = "grafana-board.png", name = "grafana-board", format = "png")
    }

    @Test
    fun `skalering av gray-scale dokument skal ikke feile`() {
        assertDoesNotThrow {
            scale(resourceName = "imageMedFeil.jpeg", name = "feilende")
        }
    }

    private fun scale(
        resourceName : String,
        format : String = "jpeg",
        name : String
    ) {
        val image = Image2PDFConverter().convertToPDF(resourceName.fromResources().readBytes(), format)
        val path = "${System.getProperty("user.dir")}/scaled-image-$name.pdf"
        val file = File(path)
        file.writeBytes(image)
    }
}
