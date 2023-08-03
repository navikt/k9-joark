package no.nav.helse.dokument

import io.prometheus.client.Counter
import no.nav.helse.CorrelationId
import no.nav.helse.dokument.mellomlagring.K9MellomlagringGateway
import no.nav.helse.journalforing.Fodselsnummer
import no.nav.helse.journalforing.converter.Image2PDFConverter
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger(DokumentService::class.java)

private val dokumentContentTypeCounter = Counter.build()
    .name("dokument_content_type_counter")
    .labelNames("content_type")
    .help("Teller for dokumenttyper som journalføres.")
    .register()

class DokumentService(
    private val k9MellomlagringGateway: K9MellomlagringGateway,
    private val image2PDFConverter: Image2PDFConverter,
    private val contentTypeService: ContentTypeService
) {

    suspend fun hentDokumenterMedDokumentId(
        dokumentId: List<String>,
        fodselsnummer: Fodselsnummer,
        correlationId: CorrelationId
    ): List<Dokument> {
        val alleDokumenter = k9MellomlagringGateway.hentDokumenterMedDokumentId(
            dokumentId = dokumentId,
            eiersFodselsnummer = fodselsnummer,
            correlationId = correlationId
        )

        return håndterDokumenter(alleDokumenter)
    }

    fun håndterDokumenter(dokumenter: List<Dokument>): List<Dokument> {
        dokumenter.tellContentType()

        logger.trace("Alle dokumenter hentet.")
        val bildeDokumenter = dokumenter.filter { contentTypeService.isSupportedImage(it.contentType) }
        logger.trace("${bildeDokumenter.size} bilder.")
        val applicationDokumenter = dokumenter.filter { contentTypeService.isSupportedApplication(it.contentType) }
        logger.trace("${applicationDokumenter.size} andre støttede dokumenter.")
        val ikkeSupporterteDokumenter = dokumenter.filter { !contentTypeService.isSupported(it.contentType) }
        if (ikkeSupporterteDokumenter.isNotEmpty()) {
            logger.warn("${ikkeSupporterteDokumenter.size} dokumenter som ikke støttes. Disse vil utelates fra journalføring.")
        }

        val supporterteDokumenter = applicationDokumenter.toMutableList()

        logger.trace("Gjør om de ${bildeDokumenter.size} bildene til PDF.")
        bildeDokumenter.forEach {
            supporterteDokumenter.add(
                Dokument(
                    title = it.title,
                    contentType = "application/pdf",
                    content = image2PDFConverter.convertToPDF(bytes = it.content, contentType = it.contentType)
                )
            )
        }

        logger.trace("Endringer fra bilde til PDF gjennomført.")

        return supporterteDokumenter
    }
}

private fun List<Dokument>.tellContentType() {
    forEach {
        dokumentContentTypeCounter.labels(it.contentType).inc()
    }
}
