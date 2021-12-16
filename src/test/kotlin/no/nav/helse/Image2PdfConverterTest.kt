package no.nav.helse

import no.nav.helse.dusseldorf.ktor.core.fromResources
import no.nav.helse.journalforing.converter.ImageScaler
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.File
import java.util.*
import kotlin.test.Ignore
import kotlin.test.Test

class Image2PdfConverterTest {

    @Test
    fun `Generert skalerte bilder`() {
        scale(resourceName = "hoyopplost.jpg", name = "hoyopplost")
        scale(resourceName = "widescreen.jpg", name = "widescreen")
        scale(resourceName = "legeerklaering_iPhone_XS.jpg", name = "legeerklaering")
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
        val image = ImageScaler.downToA4(resourceName.fromResources().readBytes(), format)
        val path = "${System.getProperty("user.dir")}/scaled-image-$name-${UUID.randomUUID()}.$format"
        val file = File(path)
        file.writeBytes(image)
    }
}
