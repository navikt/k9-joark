package no.nav.helse.journalforing.converter

import org.apache.pdfbox.io.IOUtils
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import org.apache.pdfbox.util.Matrix
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.geom.AffineTransform
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

private val logger: Logger = LoggerFactory.getLogger(Image2PDFConverter::class.java)

class Image2PDFConverter {
    fun convertToPDF(bytes: ByteArray, contentType: String): ByteArray {
        runCatching {
            logger.trace("Konverterer fra $contentType til PDF.")
            PDDocument(IOUtils.createTempFileOnlyStreamCache()).use { doc: PDDocument ->
                ByteArrayOutputStream().use { os ->
                    pdfFraBilde(doc, bytes)
                    doc.save(os)
                    return os.toByteArray()
                }
            }
        }.getOrElse { cause: Throwable ->
            throw  IllegalStateException("Klarte ikke å gjøre om $contentType bilde til PDF", cause)
        }
    }

    companion object {

        private val A4 = PDRectangle.A4
        private fun pdfFraBilde(doc: PDDocument, bilde: ByteArray) {
            val pdPage = PDPage(A4)
            doc.addPage(pdPage)
            try {
                val bufferedImage = ImageIO.read(ByteArrayInputStream(bilde))

                val roteres = bufferedImage.height < bufferedImage.width

                val (width, height) = skalertDimensjon(
                    MyPair(bufferedImage.width.toFloat(), bufferedImage.height.toFloat()).roterHvis(roteres),
                    MyPair(pdPage.mediaBox.width, pdPage.mediaBox.height),
                )

                val transform =
                    AffineTransform(width, 0f, 0f, height, PDRectangle.A4.lowerLeftX, PDRectangle.A4.lowerLeftY)
                val matrix = Matrix(transform)

                if (roteres) {
                    //Flytt bildet 1 gang (width) til høyre på x-aksen
                    matrix.translate(1f, 0f)

                    matrix.rotate(Math.toRadians(90.0))
                    pdPage.rotation = 90
                }

                val pdImg = LosslessFactory.createFromImage(doc, bufferedImage)

                PDPageContentStream(doc, pdPage).use { pdPageContentStream ->
                    pdPageContentStream.drawImage(pdImg, matrix)
                }
            } catch (e: Throwable) {
                throw IllegalStateException(
                    "Konvertering av bilde med størrelse ${
                        bilde.size.toLong().div(1024)
                    } kB feilet", e
                )
            }
        }

        private data class MyPair(val width: Float, val height: Float) {
            fun roterHvis(roteres: Boolean): MyPair {
                return if (roteres) {
                    MyPair(height, width)
                } else {
                    this
                }
            }
        }

        private fun skalertDimensjon(imgSize: MyPair, a4: MyPair): MyPair {
            var (width, height) = imgSize
            if (width > a4.width) {
                width = a4.width
                height = width * imgSize.height / imgSize.width
            }
            if (height > a4.height) {
                height = a4.height
                width = height * imgSize.width / imgSize.height
            }
            return MyPair(width, height)
        }
    }
}
